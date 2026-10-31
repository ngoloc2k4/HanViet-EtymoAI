package com.hanviet.etymoai.util

import org.junit.Assert.assertEquals
import org.junit.Test

class KoreanRomanizerTest {

    @Test
    fun testStandardRevisedRomanization() {
        // Test user's specific example
        assertEquals("an-nyeong-ha-se-yo", KoreanRomanizer.romanize("안녕하세요"))

        // Common core vocabulary
        assertEquals("do-seo-gwan", KoreanRomanizer.romanize("도서관"))
        assertEquals("hak-saeng", KoreanRomanizer.romanize("학생"))
        assertEquals("hak-gyo", KoreanRomanizer.romanize("학교"))
        assertEquals("gi-bun", KoreanRomanizer.romanize("기분"))
        assertEquals("gyeong-je", KoreanRomanizer.romanize("경제"))
        assertEquals("byeong-won", KoreanRomanizer.romanize("병원"))
        assertEquals("ui-sa", KoreanRomanizer.romanize("의사"))
        assertEquals("ga-da", KoreanRomanizer.romanize("가다"))
        assertEquals("deut-da", KoreanRomanizer.romanize("듣다"))
        assertEquals("a-reu-ba-i-teu", KoreanRomanizer.romanize("아르바이트"))
        assertEquals("keo-pi", KoreanRomanizer.romanize("커피"))
        assertEquals("keom-pyu-teo", KoreanRomanizer.romanize("컴퓨터"))
    }

    @Test
    fun testMultiWordRomanization() {
        assertEquals("han-guk-eo gong-bu", KoreanRomanizer.romanize("한국어 공부"))
    }

    @Test
    fun testEmptyAndNullSafety() {
        assertEquals("", KoreanRomanizer.romanize(""))
        assertEquals("", KoreanRomanizer.romanize(null))
        assertEquals("", KoreanRomanizer.romanize("   "))
    }
}
