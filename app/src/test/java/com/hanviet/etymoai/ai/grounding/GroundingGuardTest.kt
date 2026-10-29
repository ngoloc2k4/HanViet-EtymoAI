package com.hanviet.etymoai.ai.grounding

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hanviet.etymoai.ai.provider.AiEtymologyExplanation
import com.hanviet.etymoai.ai.provider.AiProvider
import com.hanviet.etymoai.ai.provider.AiProviderType
import com.hanviet.etymoai.ai.provider.AiSynonymComparison
import com.hanviet.etymoai.ai.provider.GroundedPrompt
import com.hanviet.etymoai.ai.provider.HanjaSegmentPromptInfo
import com.hanviet.etymoai.data.db.workspace.WorkspaceDatabase
import com.hanviet.etymoai.domain.etymology.HanjaSegment
import com.hanviet.etymoai.domain.etymology.OriginType
import com.hanviet.etymoai.domain.etymology.VerificationStatus
import com.hanviet.etymoai.domain.etymology.WordEtymology
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GroundingGuardTest {

    private lateinit var wsDb: WorkspaceDatabase
    private lateinit var guard: GroundingGuard

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        wsDb = WorkspaceDatabase.createInMemory(context)
        guard = GroundingGuard(wsDb.aiCacheDao())
    }

    @After
    fun tearDown() {
        wsDb.close()
    }

    @Test
    fun testExtractHanjaCharacters() {
        val text = "Từ '기분' được cấu tạo từ Khí (氣) mang nghĩa hơi thở và Phân (分) mang nghĩa chia sẻ."
        val hanjaSet = GroundingGuard.extractHanjaCharacters(text)

        assertEquals(2, hanjaSet.size)
        assertTrue(hanjaSet.contains("氣"))
        assertTrue(hanjaSet.contains("分"))
        assertFalse(hanjaSet.contains("기"))
    }

    @Test
    fun testAuditExplanation_legitimateHanja_noHallucination() {
        val prompt = GroundedPrompt(
            headword = "기분",
            primaryMeaning = "Tâm trạng",
            originType = "SINO_KOREAN",
            hanjaSegments = listOf(
                HanjaSegmentPromptInfo("기", "氣", "khí", "khí sắc"),
                HanjaSegmentPromptInfo("분", "分", "phân", "phân định")
            )
        )

        val legitimateExplanation = AiEtymologyExplanation(
            headword = "기분",
            etymologySummaryVi = "Từ Hán-Hàn gồm chữ Khí (氣) và Phân (分).",
            deepNuanceExplanationVi = "Khí (氣) là sinh khí trong cơ thể; Phân (分) là sự phân bổ trạng thái."
        )

        val audited = guard.auditExplanation(legitimateExplanation, prompt)
        assertFalse("Không được đánh dấu ảo giác khi chữ Hán hoàn toàn khớp", audited.hasUnverifiedHanja)
    }

    @Test
    fun testAuditExplanation_unverifiedHanja_flagsHallucination() {
        val prompt = GroundedPrompt(
            headword = "기분",
            primaryMeaning = "Tâm trạng",
            originType = "SINO_KOREAN",
            hanjaSegments = listOf(
                HanjaSegmentPromptInfo("기", "氣", "khí", "khí sắc"),
                HanjaSegmentPromptInfo("분", "分", "phân", "phân định")
            )
        )

        // AI tự ý sáng tác thêm chữ Hán lạ (Tâm: 心) không có trong phân đoạn
        val hallucinatedExplanation = AiEtymologyExplanation(
            headword = "기분",
            etymologySummaryVi = "Từ Hán-Hàn gồm chữ Khí (氣) và Phân (分).",
            deepNuanceExplanationVi = "Xuất phát từ thuyết ngũ hành kết hợp với Tâm (心) để tạo thành tâm trạng."
        )

        val audited = guard.auditExplanation(hallucinatedExplanation, prompt)
        assertTrue("Phải phát hiện chữ Hán lạ '心' và đánh cờ hasUnverifiedHanja", audited.hasUnverifiedHanja)
    }

    @Test
    fun testAuditExplanation_nativeKorean_flagsHanjaHallucination() {
        val prompt = GroundedPrompt(
            headword = "바람",
            primaryMeaning = "Cơn gió",
            originType = "NATIVE_KOREAN",
            hanjaSegments = emptyList()
        )

        // AI cố tình bịa chữ Hán Phong (風) cho từ thuần Hàn
        val hallucinatedExplanation = AiEtymologyExplanation(
            headword = "바람",
            etymologySummaryVi = "Xuất phát từ chữ Phong (風) trong tiếng Hán.",
            deepNuanceExplanationVi = "Phong (風) tượng trưng cho luồng khí tự nhiên."
        )

        val audited = guard.auditExplanation(hallucinatedExplanation, prompt)
        assertTrue("Từ thuần Hàn không được chứa bất kỳ chữ Hán nào", audited.hasUnverifiedHanja)
    }

    @Test
    fun testAuditExplanation_nativeKorean_clean_noHallucination() {
        val prompt = GroundedPrompt(
            headword = "바람",
            primaryMeaning = "Cơn gió",
            originType = "NATIVE_KOREAN",
            hanjaSegments = emptyList()
        )

        val cleanExplanation = AiEtymologyExplanation(
            headword = "바람",
            etymologySummaryVi = "Đây là từ thuần Hàn cổ, chỉ luồng không khí chuyển động.",
            deepNuanceExplanationVi = "Không có chữ Hán tương ứng, từ này xuất hiện trong thi ca dân gian Hàn Quốc."
        )

        val audited = guard.auditExplanation(cleanExplanation, prompt)
        assertFalse(audited.hasUnverifiedHanja)
    }

    @Test
    fun testPromptBuilder_buildAndHash() {
        val etymology = WordEtymology(
            lexemeId = 100L,
            headword = "학교",
            originType = OriginType.SINO_KOREAN,
            verificationStatus = VerificationStatus.VERIFIED_OFFICIAL,
            segments = listOf(
                HanjaSegment(0, "학", "學", "học", "học tập"),
                HanjaSegment(1, "교", "校", "hiệu", "trường học")
            )
        )

        val prompt = EtymologyPromptBuilder.buildEtymologyPrompt(etymology, "Trường học")
        assertEquals("학교", prompt.headword)
        assertEquals("SINO_KOREAN", prompt.originType)
        assertEquals(2, prompt.hanjaSegments.size)

        val hash1 = EtymologyPromptBuilder.computePromptHash(prompt, "gpt-4o-mini")
        val hash2 = EtymologyPromptBuilder.computePromptHash(prompt, "gpt-4o-mini")
        val hashOtherModel = EtymologyPromptBuilder.computePromptHash(prompt, "gemini-1.5-flash")

        assertEquals(64, hash1.length) // SHA-256 hex string
        assertEquals("Hash phải có tính tiền định (deterministic)", hash1, hash2)
        assertNotEquals("Khác model phải sinh ra hash khác", hash1, hashOtherModel)
    }

    @Test
    fun testExecuteGroundedEtymology_caching() = runBlocking {
        var callCount = 0
        val fakeProvider = object : AiProvider {
            override val providerType = AiProviderType.GEMINI
            override suspend fun explainEtymology(prompt: GroundedPrompt): Result<AiEtymologyExplanation> {
                callCount++
                return Result.success(
                    AiEtymologyExplanation(
                        headword = prompt.headword,
                        etymologySummaryVi = "Học sinh (學生)",
                        deepNuanceExplanationVi = "Học (學) và Sinh (生)"
                    )
                )
            }
            override suspend fun compareSynonyms(prompt: GroundedPrompt): Result<AiSynonymComparison> {
                throw NotImplementedError()
            }
        }

        val prompt = GroundedPrompt(
            headword = "학생",
            primaryMeaning = "Học sinh",
            originType = "SINO_KOREAN",
            hanjaSegments = listOf(
                HanjaSegmentPromptInfo("학", "學", "học", "học tập"),
                HanjaSegmentPromptInfo("생", "生", "sinh", "sống")
            )
        )

        // Lần gọi 1: Chưa có cache -> Phải gọi vào provider
        val res1 = guard.executeGroundedEtymology(fakeProvider, prompt)
        assertTrue(res1.isSuccess)
        assertEquals(1, callCount)

        // Lần gọi 2: Đã có cache trong Room -> Không được gọi lại provider
        val res2 = guard.executeGroundedEtymology(fakeProvider, prompt)
        assertTrue(res2.isSuccess)
        assertEquals("Lần 2 phải lấy từ bộ nhớ đệm, không tăng callCount", 1, callCount)
        assertEquals(res1.getOrNull()!!.etymologySummaryVi, res2.getOrNull()!!.etymologySummaryVi)
    }
}
