package com.hanviet.etymoai.domain.morphology

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

@RunWith(RobolectricTestRunner::class)
class MorphologyEngineTest {

    private lateinit var helper: CoreDatabaseHelper
    private lateinit var engine: MorphologyEngine

    @Before
    fun setUp() {
        val dbFile = File("src/main/assets/dictionary_core.db")
        assertTrue("File dictionary_core.db phải tồn tại", dbFile.exists())
        helper = CoreDatabaseHelper.fromFile(dbFile)
        engine = MorphologyEngine(helper)
    }

    @After
    fun tearDown() {
        helper.close()
    }

    @Test
    fun testAmbiguityResolution_deuteoseoyo() {
        // "들었어요" là ví dụ kinh điển về đa nghĩa hình thái trong tiếng Hàn:
        // 1. 듣다 (nghe) qua bất quy tắc ㄷ (ㄹ + 었)
        // 2. 들다 (cầm, nhấc, tốn) qua đuôi quá khứ thông thường (-었-)
        val candidates = engine.analyze("들었어요")
        assertFalse("Phải có danh sách ứng viên cho '들었어요'", candidates.isEmpty())

        val deutda = candidates.find { it.lemma == "듣다" }
        assertNotNull("Phải khôi phục được từ gốc '듣다' (nghe)", deutda)
        assertTrue("Từ '듣다' phải được xác nhận có trong từ điển", deutda!!.isInDictionary)
        assertEquals(IrregularGroup.DIGEUT, deutda.group)

        val deulda = candidates.find { it.lemma == "들다" }
        assertNotNull("Phải khôi phục được từ gốc '들다' (cầm, tốn)", deulda)
        assertTrue("Từ '들다' phải được xác nhận có trong từ điển", deulda!!.isInDictionary)

        // Cả 2 từ đều phải có điểm tin cậy cao >= 0.9 do tồn tại trong từ điển
        assertTrue(deutda.confidenceScore >= 0.90f)
        assertTrue(deulda.confidenceScore >= 0.90f)
    }

    @Test
    fun testIrregularBieup_chuwoseo() {
        val candidates = engine.analyze("추워서")
        assertFalse("Phải có ứng viên cho '추워서'", candidates.isEmpty())

        val chupda = candidates.find { it.lemma == "춥다" }
        assertNotNull("Phải khôi phục '추워서' về '춥다'", chupda)
        assertTrue("'춥다' phải được đánh dấu isInDictionary = true", chupda!!.isInDictionary)
        assertEquals(IrregularGroup.BIEUP, chupda.group)
        assertTrue("Giải thích tiếng Việt phải rõ ràng", chupda.explanationVi.contains("ㅂ"))
    }

    @Test
    fun testIrregularReu_mollayo() {
        val candidates = engine.analyze("몰라요")
        assertFalse("Phải có ứng viên cho '몰라요'", candidates.isEmpty())

        val moruda = candidates.find { it.lemma == "모르다" }
        assertNotNull("Phải khôi phục '몰라요' về '모르다'", moruda)
        assertTrue(moruda!!.isInDictionary)
        assertEquals(IrregularGroup.REU, moruda.group)
    }

    @Test
    fun testIrregularRieul_mandeumnida() {
        val candidates = engine.analyze("만듭니다")
        assertFalse("Phải có ứng viên cho '만듭니다'", candidates.isEmpty())

        val mandeulda = candidates.find { it.lemma == "만들다" }
        assertNotNull("Phải khôi phục '만듭니다' về '만들다'", mandeulda)
        assertTrue(mandeulda!!.isInDictionary)
        assertEquals(IrregularGroup.RIEUL, mandeulda.group)
    }

    @Test
    fun testRegularConjugations() {
        // 1. Quá khứ co nguyên âm: 갔어요 -> 가다
        val candGa = engine.analyze("갔어요")
        val gada = candGa.find { it.lemma == "가다" }
        assertNotNull("Phải khôi phục '갔어요' về '가다'", gada)
        assertTrue(gada!!.isInDictionary)

        // 2. Liên từ nguyên nhân: 가서 -> 가다
        val candGaseo = engine.analyze("가서")
        val gadaFromSeo = candGaseo.find { it.lemma == "가다" }
        assertNotNull("Phải khôi phục '가서' về '가다'", gadaFromSeo)
        assertTrue(gadaFromSeo!!.isInDictionary)

        // 3. Kính ngữ trang trọng: 갑니다 -> 가다
        val candGamnida = engine.analyze("갑니다")
        val gadaFromMnida = candGamnida.find { it.lemma == "가다" }
        assertNotNull("Phải khôi phục '갑니다' về '가다'", gadaFromMnida)
        assertTrue(gadaFromMnida!!.isInDictionary)
    }

    @Test
    fun testDeconjugateInterface() {
        val morphCandidates = engine.deconjugate("추워서")
        assertFalse("Interface deconjugate phải trả về danh sách ứng viên", morphCandidates.isEmpty())

        val top = morphCandidates.first()
        assertEquals("춥다", top.lemma)
        assertTrue("Confidence phải >= 0.9", top.confidence >= 0.90f)
        assertFalse("Giải thích không được rỗng", top.grammarExplanation.isBlank())
    }

    @Test
    fun testEmptyAndBlankInput() {
        assertTrue(engine.analyze("").isEmpty())
        assertTrue(engine.analyze("   ").isEmpty())
        assertTrue(engine.deconjugate("").isEmpty())
    }
}
