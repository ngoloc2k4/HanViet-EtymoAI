package com.hanviet.etymoai.domain.morphology

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IrregularVerbRulesTest {

    // ======================================================================
    // 0. KIỂM THỬ TIỆN ÍCH HANGUL (HangulUtil Decomposition & Composition)
    // ======================================================================

    @Test
    fun testHangulUtil_decompositionAndComposition() {
        val testChars = listOf('가', '강', '추', '워', '들', '지', '파', '래', '몰', '라', '바', '빠', '만', '듭')
        for (char in testChars) {
            val dec = HangulUtil.decompose(char)
            assertNotNull("Ký tự $char phải phân tách được", dec)
            val recomposed = HangulUtil.compose(dec!!.cho, dec.jung, dec.jong)
            assertEquals("Ký tự ghép lại phải trùng khớp tuyệt đối với nguyên bản", char, recomposed)
        }
    }

    @Test
    fun testHangulUtil_patchimManipulation() {
        // Thêm patchim ㅂ vào '추' -> '춥'
        val chub = HangulUtil.withPatchim('추', HangulUtil.JONG_BIEUP)
        assertEquals('춥', chub)

        // Bỏ patchim của '몰' -> '모'
        val mo = HangulUtil.withoutPatchim('몰')
        assertEquals('모', mo)

        // Thay patchim ㄹ của '들' thành ㄷ -> '듣'
        val deud = HangulUtil.withPatchim('들', HangulUtil.JONG_DIGEUT)
        assertEquals('듣', deud)
    }

    // ======================================================================
    // 1. NHÓM BIEUP (ㅂ bất quy tắc): 추워 -> 춥다, 도와 -> 돕다
    // ======================================================================

    @Test
    fun testGroupBieup_deconjugation() {
        val testCases = mapOf(
            "추워" to "춥다",
            "더워요" to "덥다",
            "도와" to "돕다",
            "고와요" to "곱다",
            "아름다워" to "아름답다",
            "추웠어요" to "춥다",
            "추우면" to "춥다"
        )

        for ((surface, expectedLemma) in testCases) {
            val candidates = IrregularVerbRules.deconjugate(surface)
            assertFalse("Phải có kết quả cho '$surface'", candidates.isEmpty())
            val matched = candidates.find { it.lemma == expectedLemma }
            assertNotNull("Phải khôi phục '$surface' về '$expectedLemma'", matched)
            assertEquals(IrregularGroup.BIEUP, matched!!.group)
            assertTrue("Độ tin cậy phải >= 0.9", matched.confidenceScore >= 0.9f)
        }
    }

    // ======================================================================
    // 2. NHÓM DIGEUT (ㄷ bất quy tắc): 들어요 -> 듣다, 걸어요 -> 걷다
    // ======================================================================

    @Test
    fun testGroupDigeut_deconjugation() {
        val testCases = mapOf(
            "들어요" to "듣다",
            "들어" to "듣다",
            "걸어요" to "걷다",
            "걸어" to "걷다",
            "물어요" to "묻다",
            "실어요" to "싣다"
        )

        for ((surface, expectedLemma) in testCases) {
            val candidates = IrregularVerbRules.deconjugate(surface)
            assertFalse("Phải có kết quả cho '$surface'", candidates.isEmpty())
            val matched = candidates.find { it.lemma == expectedLemma }
            assertNotNull("Phải khôi phục '$surface' về '$expectedLemma'", matched)
            assertEquals(IrregularGroup.DIGEUT, matched!!.group)
        }
    }

    // ======================================================================
    // 3. NHÓM SIOT (ㅅ bất quy tắc): 지어요 -> 짓다, 나아요 -> 낫다
    // ======================================================================

    @Test
    fun testGroupSiot_deconjugation() {
        val testCases = mapOf(
            "지어요" to "짓다",
            "지어" to "짓다",
            "나아요" to "낫다",
            "나아" to "낫다",
            "부어요" to "붓다",
            "이어요" to "잇다"
        )

        for ((surface, expectedLemma) in testCases) {
            val candidates = IrregularVerbRules.deconjugate(surface)
            assertFalse("Phải có kết quả cho '$surface'", candidates.isEmpty())
            val matched = candidates.find { it.lemma == expectedLemma }
            assertNotNull("Phải khôi phục '$surface' về '$expectedLemma'", matched)
            assertEquals(IrregularGroup.SIOT, matched!!.group)
        }
    }

    // ======================================================================
    // 4. NHÓM HIEUT (ㅎ bất quy tắc): 파래 -> 파랗다, 하얘 -> 하얗다
    // ======================================================================

    @Test
    fun testGroupHieut_deconjugation() {
        val testCases = mapOf(
            "파래" to "파랗다",
            "빨개요" to "빨갛다",
            "하얘" to "하얗다",
            "그래요" to "그렇다",
            "어때" to "어떻다"
        )

        for ((surface, expectedLemma) in testCases) {
            val candidates = IrregularVerbRules.deconjugate(surface)
            assertFalse("Phải có kết quả cho '$surface'", candidates.isEmpty())
            val matched = candidates.find { it.lemma == expectedLemma }
            assertNotNull("Phải khôi phục '$surface' về '$expectedLemma'", matched)
            assertEquals(IrregularGroup.HIEUT, matched!!.group)
        }
    }

    // ======================================================================
    // 5. NHÓM REU (르 bất quy tắc): 몰라요 -> 모르다, 빨라 -> 빠르다
    // ======================================================================

    @Test
    fun testGroupReu_deconjugation() {
        val testCases = mapOf(
            "몰라요" to "모르다",
            "몰라" to "모르다",
            "빨라" to "빠르다",
            "빨라요" to "빠르다",
            "불러요" to "부르다",
            "달라요" to "다르다",
            "올라" to "오르다"
        )

        for ((surface, expectedLemma) in testCases) {
            val candidates = IrregularVerbRules.deconjugate(surface)
            assertFalse("Phải có kết quả cho '$surface'", candidates.isEmpty())
            val matched = candidates.find { it.lemma == expectedLemma }
            assertNotNull("Phải khôi phục '$surface' về '$expectedLemma'", matched)
            assertEquals(IrregularGroup.REU, matched!!.group)
        }
    }

    // ======================================================================
    // 6. NHÓM EUU (으 bất quy tắc): 바빠 -> 바쁘다, 예뻐 -> 예쁘다
    // ======================================================================

    @Test
    fun testGroupEuu_deconjugation() {
        val testCases = mapOf(
            "바빠" to "바쁘다",
            "바빠요" to "바쁘다",
            "예뻐" to "예쁘다",
            "예뻐요" to "예쁘다",
            "아파" to "아프다",
            "슬퍼요" to "슬프다",
            "커요" to "크다"
        )

        for ((surface, expectedLemma) in testCases) {
            val candidates = IrregularVerbRules.deconjugate(surface)
            assertFalse("Phải có kết quả cho '$surface'", candidates.isEmpty())
            val matched = candidates.find { it.lemma == expectedLemma }
            assertNotNull("Phải khôi phục '$surface' về '$expectedLemma'", matched)
            assertEquals(IrregularGroup.EUU, matched!!.group)
        }
    }

    // ======================================================================
    // 7. NHÓM RIEUL (ㄹ rơi rụng): 만듭니다 -> 만들다, 아는 -> 알다
    // ======================================================================

    @Test
    fun testGroupRieul_deconjugation() {
        val testCases = mapOf(
            "만듭니다" to "만들다",
            "삽니다" to "살다",
            "아는" to "알다",
            "만드세요" to "만들다",
            "여는" to "열다"
        )

        for ((surface, expectedLemma) in testCases) {
            val candidates = IrregularVerbRules.deconjugate(surface)
            assertFalse("Phải có kết quả cho '$surface'", candidates.isEmpty())
            val matched = candidates.find { it.lemma == expectedLemma }
            assertNotNull("Phải khôi phục '$surface' về '$expectedLemma'", matched)
            assertEquals(IrregularGroup.RIEUL, matched!!.group)
        }
    }
}
