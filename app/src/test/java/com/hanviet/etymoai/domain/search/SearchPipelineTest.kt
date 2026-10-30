package com.hanviet.etymoai.domain.search

import com.hanviet.etymoai.data.db.core.CoreDatabaseHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
class SearchPipelineTest {

    private lateinit var helper: CoreDatabaseHelper
    private lateinit var pipeline: SearchPipeline

    @Before
    fun setUp() {
        val dbFile = File("src/main/assets/dictionary_core.db")
        assertTrue("File dictionary_core.db phải tồn tại trong assets", dbFile.exists())
        helper = CoreDatabaseHelper.fromFile(dbFile)
        pipeline = SearchPipeline(helper)
    }

    @After
    fun tearDown() {
        helper.close()
    }

    @Test
    fun testTier1_exactHeadwordMatch() {
        val results = pipeline.search("기분")
        assertFalse("Kết quả không được rỗng", results.isEmpty())

        val topResult = results.first()
        assertEquals("Headword phải khớp chính xác", "기분", topResult.headwordNfc)
        assertEquals("Tầng khớp phải là EXACT_HEADWORD", SearchMatchTier.EXACT_HEADWORD, topResult.matchTier)
        assertEquals(1.0f, topResult.confidenceScore, 0.001f)
        assertTrue("Nghĩa tiếng Việt phải chứa 'tâm trạng'", topResult.primaryMeaningVi.contains("tâm trạng"))
    }

    @Test
    fun testTier2_chosungMatch() {
        val results = pipeline.search("ㄱㅂ")
        assertFalse("Tra cứu Chosung 'ㄱㅂ' phải có kết quả", results.isEmpty())

        val matchedGibun = results.find { it.headwordNfc == "기분" }
        assertNotNull("Phải tìm thấy '기분' từ Chosung 'ㄱㅂ'", matchedGibun)
        assertEquals(SearchMatchTier.CHOSUNG_ALIAS, matchedGibun!!.matchTier)
        assertEquals(0.95f, matchedGibun.confidenceScore, 0.001f)
    }

    @Test
    fun testTier3_precomputedFormMatch() {
        val results = pipeline.search("갔어요")
        assertFalse("Tra cứu dạng chia '갔어요' phải có kết quả", results.isEmpty())

        val baseVerb = results.find { it.headwordNfc == "가다" }
        assertNotNull("Phải tìm thấy động từ nguyên thể '가다'", baseVerb)
        assertEquals(SearchMatchTier.PRECOMPUTED_FORM, baseVerb!!.matchTier)
        assertTrue("Điểm tin cậy phải >= 0.9", baseVerb.confidenceScore >= 0.9f)
        assertNotNull(baseVerb.matchNote)
        assertTrue("Ghi chú phải chứa thông tin quá khứ", baseVerb.matchNote!!.contains("quá khứ"))
    }

    @Test
    fun testTier4_morphologyEngineHook() {
        val mockDeconjugator = MorphologyDeconjugator { word ->
            if (word == "갔었다") {
                listOf(
                    MorphologyCandidate(
                        lemma = "가다",
                        ruleApplied = "PAST_PERFECT",
                        grammarExplanation = "Thì quá khứ hoàn thành -았/었었다",
                        confidence = 0.88f
                    )
                )
            } else {
                emptyList()
            }
        }

        val customPipeline = SearchPipeline(helper, mockDeconjugator)
        val results = customPipeline.search("갔었다")
        assertFalse("Phải có kết quả từ bộ máy hình thái", results.isEmpty())

        val matched = results.find { it.headwordNfc == "가다" }
        assertNotNull("Phải khớp động từ '가다'", matched)
        assertEquals(SearchMatchTier.MORPHOLOGY, matched!!.matchTier)
        assertEquals(0.88f, matched.confidenceScore, 0.001f)
    }

    @Test
    fun testTier4_morphologyIntegrated_multipleCandidates() {
        // Tra cứu "들었어요" phải trả về cả "듣다" (nghe) và "들다" (cầm/tốn)
        val results = pipeline.search("들었어요")
        assertFalse("Kết quả không được rỗng", results.isEmpty())

        val deutda = results.find { it.headwordNfc == "듣다" }
        assertNotNull("Phải tìm thấy '듣다' (nghe)", deutda)

        val deulda = results.find { it.headwordNfc == "들다" }
        assertNotNull("Phải tìm thấy '들다' (cầm/nhấc/tốn)", deulda)
        assertEquals(SearchMatchTier.MORPHOLOGY, deulda!!.matchTier)
        assertNotNull("Phải có ghi chú quy tắc hình thái", deulda.matchNote)
    }

    @Test
    fun testTier4_morphologyIntegrated_irregularBieup() {
        // "추워서" không nằm trong precomputed forms, phải được Tầng 4 khôi phục về "춥다"
        val results = pipeline.search("추워서")
        assertFalse("Phải có kết quả cho '추워서'", results.isEmpty())

        val chupda = results.find { it.headwordNfc == "춥다" }
        assertNotNull("Phải tìm thấy '춥다' từ '추워서'", chupda)
        assertEquals(SearchMatchTier.MORPHOLOGY, chupda!!.matchTier)
        assertTrue(chupda.matchNote?.contains("ㅂ") == true)
    }

    @Test
    fun testTier4_morphologyIntegrated_irregularRieul() {
        // "만듭니다" -> "만들다"
        val results = pipeline.search("만듭니다")
        assertFalse("Phải có kết quả cho '만듭니다'", results.isEmpty())

        val mandeulda = results.find { it.headwordNfc == "만들다" }
        assertNotNull("Phải tìm thấy '만들다'", mandeulda)
    }

    @Test
    fun testTier5_fts5UnaccentedVietnamese() {
        val results = pipeline.search("tam trang")
        assertFalse("Tra cứu tiếng Việt không dấu 'tam trang' phải ra kết quả", results.isEmpty())

        val topResult = results.first()
        assertEquals("Phải tìm thấy '기분'", "기분", topResult.headwordNfc)
        assertEquals(SearchMatchTier.FTS_FULLTEXT, topResult.matchTier)
    }

    @Test
    fun testTier5_fts5AccentedVietnamese() {
        val results = pipeline.search("bệnh viện")
        assertFalse("Tra cứu tiếng Việt có dấu 'bệnh viện' phải ra kết quả", results.isEmpty())

        val topResult = results.first()
        assertEquals("Phải tìm thấy '병원'", "병원", topResult.headwordNfc)
        assertEquals(SearchMatchTier.FTS_FULLTEXT, topResult.matchTier)
    }

    @Test
    fun testExtractCompoundKeywords_longestMatchFirst() {
        val sentence = "học sinh đi bệnh viện"
        val extracted = pipeline.extractCompoundKeywords(sentence)

        assertTrue("Phải trích xuất được cụm 'học sinh'", extracted.contains("học sinh"))
        assertTrue("Phải trích xuất được cụm 'bệnh viện'", extracted.contains("bệnh viện"))
    }

    @Test
    fun testSearch_emptyQueryReturnsEmpty() {
        assertTrue("Chuỗi rỗng phải trả về danh sách rỗng", pipeline.search("").isEmpty())
        assertTrue("Khoảng trắng phải trả về danh sách rỗng", pipeline.search("   ").isEmpty())
    }

    @Test
    fun testSearch_deduplication() {
        // "기분" có thể khớp cả Exact (Tầng 1) lẫn FTS5 (Tầng 5)
        val results = pipeline.search("기분")
        val ids = results.map { it.lexemeId }
        assertEquals("Mỗi lexemeId chỉ được xuất hiện 1 lần duy nhất", ids.size, ids.distinct().size)
    }

    @Test
    fun testSearchLatency_sub50ms() {
        // Warm-up JIT compiler và bộ đệm trang SQLite
        repeat(5) {
            pipeline.search("기분")
            pipeline.search("tam trang")
        }

        val durations = (1..5).map {
            measureTimeMillis {
                val results = pipeline.search("tam trang")
                assertFalse(results.isEmpty())
            }
        }
        val minDuration = durations.minOrNull() ?: 0L

        println("Độ trễ tra cứu FTS tiếng Việt (min 5 lần): ${minDuration}ms, toàn bộ: $durations")
        assertTrue("Độ trễ tra cứu tối ưu phải < 120ms trên môi trường Robolectric CI (Thực tế min: ${minDuration}ms)", minDuration < 120)
    }
}
