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
        // Warm-up query
        pipeline.search("기분")

        val duration = measureTimeMillis {
            val results = pipeline.search("tam trang")
            assertFalse(results.isEmpty())
        }

        println("Độ trễ tra cứu FTS tiếng Việt: ${duration}ms")
        assertTrue("Độ trễ tra cứu phải < 50ms (Thực tế: ${duration}ms)", duration < 50)
    }
}
