package com.hanviet.etymoai.ui.detail

import com.hanviet.etymoai.domain.etymology.HanjaSegment
import com.hanviet.etymoai.domain.etymology.OriginType
import com.hanviet.etymoai.domain.etymology.VerificationStatus
import com.hanviet.etymoai.domain.etymology.WordEtymology
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EtymologyViewTest {

    @Test
    fun testOriginType_parsingAndProperties() {
        assertEquals(OriginType.SINO_KOREAN, OriginType.fromString("SINO_KOREAN"))
        assertEquals(OriginType.NATIVE_KOREAN, OriginType.fromString("NATIVE_KOREAN"))
        assertEquals(OriginType.LOANWORD, OriginType.fromString("LOANWORD"))
        assertEquals(OriginType.MIXED, OriginType.fromString("MIXED"))
        assertEquals(OriginType.NATIVE_KOREAN, OriginType.fromString("UNKNOWN_VALUE"))

        assertEquals("Hán-Hàn", OriginType.SINO_KOREAN.titleVi)
        assertEquals("Thuần Hàn", OriginType.NATIVE_KOREAN.titleVi)
    }

    @Test
    fun testVerificationStatus_parsingAndProperties() {
        assertEquals(VerificationStatus.VERIFIED_PRIMARY, VerificationStatus.fromString("VERIFIED_PRIMARY"))
        assertEquals(VerificationStatus.CURATED, VerificationStatus.fromString("CURATED"))
        assertEquals(VerificationStatus.INFERRED, VerificationStatus.fromString("INFERRED"))
        assertEquals(VerificationStatus.AI_SUGGESTED, VerificationStatus.fromString("AI_SUGGESTED"))
        assertEquals(VerificationStatus.INFERRED, VerificationStatus.fromString("UNKNOWN_STATUS"))

        assertEquals("Từ điển chuẩn quốc gia", VerificationStatus.VERIFIED_PRIMARY.badgeLabel)
        assertEquals("Quy tắc ngữ âm", VerificationStatus.INFERRED.badgeLabel)
    }

    @Test
    fun testWordEtymology_modelIntegrity() {
        val segments = listOf(
            HanjaSegment(0, "기", "氣", "khí", "khí sắc, tinh thần"),
            HanjaSegment(1, "분", "分", "phân", "phần, phân định")
        )

        val etymology = WordEtymology(
            lexemeId = 1L,
            headword = "기분",
            originType = OriginType.SINO_KOREAN,
            verificationStatus = VerificationStatus.VERIFIED_PRIMARY,
            phoneticVi = "ki-bun",
            segments = segments
        )

        assertEquals("기분", etymology.headword)
        assertEquals(2, etymology.segments.size)
        assertEquals("氣", etymology.segments[0].hanjaChar)
        assertEquals("khí", etymology.segments[0].readingVi)
        assertEquals("分", etymology.segments[1].hanjaChar)
        assertEquals("phân", etymology.segments[1].readingVi)
        assertNotNull(etymology.phoneticVi)
        assertTrue(etymology.verificationStatus == VerificationStatus.VERIFIED_PRIMARY)
    }
}
