package com.hanviet.etymoai.util

/**
 * KoreanRomanizer: Chuyển đổi Hangul sang phiên âm Latinh quốc tế (tiếng Anh)
 * theo chuẩn chính thức Revised Romanization of Korean (국어의 로마자 표기법 - Bộ Văn hóa, Thể thao & Du lịch Hàn Quốc).
 *
 * Định dạng đầu ra phân tách âm tiết bằng dấu gạch ngang (hyphen-separated) theo chuẩn sư phạm:
 * Ví dụ:
 *  - 안녕하세요 -> an-nyeong-ha-se-yo
 *  - 도서관 -> do-seo-gwan
 *  - 학생 -> hak-saeng
 *  - 기분 -> gi-bun
 *  - 아르바이트 -> a-reu-ba-i-teu
 */
object KoreanRomanizer {

    // 19 phụ âm đầu (Choseong / 초성)
    private val CHOSEONG = arrayOf(
        "g", "kk", "n", "d", "tt", "r", "m", "b", "pp",
        "s", "ss", "", "j", "jj", "ch", "k", "t", "p", "h"
    )

    // 21 nguyên âm (Jungseong / 중성)
    private val JUNGSEONG = arrayOf(
        "a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o",
        "wa", "wae", "oe", "yo", "u", "wo", "we", "wi", "yu",
        "eu", "ui", "i"
    )

    // 28 phụ âm cuối (Jongseong / 종성 - 받침)
    // 0: không có 받침
    private val JONGSEONG = arrayOf(
        "", "k", "k", "k", "n", "n", "n", "t", "l",
        "k", "m", "l", "l", "l", "p", "l", "m", "p",
        "p", "t", "t", "ng", "t", "t", "k", "t", "p", "t"
    )

    private const val HANGUL_BASE = 0xAC00
    private const val HANGUL_END = 0xD7A3

    /**
     * Chuyển đổi một khối âm tiết Hangul (e.g. '안') sang phiên âm Latinh chuẩn (e.g. "an").
     */
    fun romanizeSyllable(ch: Char): String {
        val code = ch.code
        if (code !in HANGUL_BASE..HANGUL_END) {
            return ch.toString()
        }

        val syllableIndex = code - HANGUL_BASE
        val choseongIdx = syllableIndex / (21 * 28)
        val jungseongIdx = (syllableIndex % (21 * 28)) / 28
        val jongseongIdx = syllableIndex % 28

        val initial = CHOSEONG.getOrElse(choseongIdx) { "" }
        val vowel = JUNGSEONG.getOrElse(jungseongIdx) { "" }
        val final = JONGSEONG.getOrElse(jongseongIdx) { "" }

        return "$initial$vowel$final"
    }

    /**
     * Phiên âm một từ hoặc cụm từ Hangul sang chuẩn Revised Romanization.
     * Các âm tiết trong cùng một từ được nối bằng gạch nối '-', các từ cách nhau bởi khoảng trắng.
     *
     * @param text Chuỗi tiếng Hàn (e.g. "안녕하세요", "도서관", "한국어 공부")
     * @return Chuỗi phiên âm tiếng Anh chuẩn (e.g. "an-nyeong-ha-se-yo", "do-seo-gwan", "han-guk-eo gong-bu")
     */
    fun romanize(text: String?): String {
        if (text.isNullOrBlank()) return ""

        val words = text.trim().split(Regex("\\s+"))
        return words.joinToString(" ") { word ->
            val syllables = mutableListOf<String>()
            val nonHangulBuffer = StringBuilder()

            for (ch in word) {
                if (ch.code in HANGUL_BASE..HANGUL_END) {
                    if (nonHangulBuffer.isNotEmpty()) {
                        syllables.add(nonHangulBuffer.toString())
                        nonHangulBuffer.clear()
                    }
                    syllables.add(romanizeSyllable(ch))
                } else {
                    nonHangulBuffer.append(ch)
                }
            }
            if (nonHangulBuffer.isNotEmpty()) {
                syllables.add(nonHangulBuffer.toString())
            }

            syllables.joinToString("-")
        }
    }
}
