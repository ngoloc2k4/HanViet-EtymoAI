package com.hanviet.etymoai.domain.morphology

/**
 * Tiện ích xử lý âm tiết tiếng Hàn (Hangul Syllable Decomposition & Composition).
 * Thao tác chuẩn xác trên 11.172 âm tiết Hangul (0xAC00 -> 0xD7A3).
 */
object HangulUtil {
    const val HANGUL_BASE = 0xAC00
    const val HANGUL_END = 0xD7A3

    // Danh sách 19 Chosung (Phụ âm đầu)
    val CHOSUNG = listOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
        'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    // Danh sách 21 Jungseong (Nguyên âm)
    val JUNGSEONG = listOf(
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
        'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    )

    // Danh sách 28 Jongseong (Patchim / Phụ âm cuối - index 0 là không có patchim)
    val JONGSEONG = listOf(
        '\u0000', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
        'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    // Các index thông dụng
    const val JONG_NONE = 0
    const val JONG_NIEUN = 4   // ㄴ
    const val JONG_DIGEUT = 7  // ㄷ
    const val JONG_RIEUL = 8   // ㄹ
    const val JONG_BIEUP = 17  // ㅂ
    const val JONG_SIOT = 19   // ㅅ
    const val JONG_HIEUT = 27  // ㅎ

    const val JUNG_A = 0       // ㅏ
    const val JUNG_AE = 1      // ㅐ
    const val JUNG_YAE = 3     // ㅒ
    const val JUNG_EO = 4      // ㅓ
    const val JUNG_E = 5       // ㅔ
    const val JUNG_O = 8       // ㅗ
    const val JUNG_WA = 9      // ㅘ
    const val JUNG_U = 13      // ㅜ
    const val JUNG_WO = 14     // ㅝ
    const val JUNG_EU = 18     // ㅡ
    const val JUNG_I = 20      // ㅣ

    fun isHangulSyllable(c: Char): Boolean {
        return c.code in HANGUL_BASE..HANGUL_END
    }

    data class Decomposed(val cho: Int, val jung: Int, val jong: Int)

    fun decompose(c: Char): Decomposed? {
        if (!isHangulSyllable(c)) return null
        val code = c.code - HANGUL_BASE
        val jong = code % 28
        val jung = ((code - jong) / 28) % 21
        val cho = ((code - jong) / 28) / 21
        return Decomposed(cho, jung, jong)
    }

    fun compose(cho: Int, jung: Int, jong: Int = 0): Char {
        require(cho in 0..18) { "Invalid cho: $cho" }
        require(jung in 0..20) { "Invalid jung: $jung" }
        require(jong in 0..27) { "Invalid jong: $jong" }
        val code = HANGUL_BASE + (cho * 21 + jung) * 28 + jong
        return code.toChar()
    }

    fun hasPatchim(c: Char): Boolean {
        val d = decompose(c) ?: return false
        return d.jong > 0
    }

    fun getPatchim(c: Char): Int {
        return decompose(c)?.jong ?: 0
    }

    fun withPatchim(c: Char, jongIndex: Int): Char {
        val d = decompose(c) ?: return c
        return compose(d.cho, d.jung, jongIndex)
    }

    fun withoutPatchim(c: Char): Char {
        val d = decompose(c) ?: return c
        return compose(d.cho, d.jung, JONG_NONE)
    }

    fun withVowel(c: Char, jungIndex: Int): Char {
        val d = decompose(c) ?: return c
        return compose(d.cho, d.jungIndex(jungIndex), d.jong)
    }

    private fun Decomposed.jungIndex(newJung: Int) = newJung
}

/**
 * 7 nhóm động từ và tính từ bất quy tắc tiếng Hàn
 */
enum class IrregularGroup(val titleVi: String) {
    BIEUP("Bất quy tắc ㅂ (추워 -> 춥다, 도와 -> 돕다)"),
    DIGEUT("Bất quy tắc ㄷ (들어요 -> 듣다, 걸어요 -> 걷다)"),
    SIOT("Bất quy tắc ㅅ (지어요 -> 짓다, 나아요 -> 낫다)"),
    HIEUT("Bất quy tắc ㅎ (파래 -> 파랗다, 하얘 -> 하얗다)"),
    REU("Bất quy tắc 르 (몰라요 -> 모르다, 빨라 -> 빠르다)"),
    EUU("Bất quy tắc 으 (바빠 -> 바쁘다, 예뻐 -> 예쁘다)"),
    RIEUL("Biến đổi rơi ㄹ (만듭니다 -> 만들다, 아는 -> 알다)")
}

/**
 * Cấu trúc đại diện cho một quy tắc phục hồi từ gốc (Deconjugation Rule)
 */
data class MorphologyRule(
    val ruleId: String,
    val group: IrregularGroup,
    val patternDescription: String,
    val confidenceWeight: Float,
    val explanationVi: String,
    val transformFn: (String) -> List<String>
)
