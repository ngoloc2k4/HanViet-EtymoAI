package com.hanviet.etymoai.domain.search

/**
 * Các tầng khớp trong quy trình tra cứu 5 tầng (5-Tier Search Pipeline)
 */
enum class SearchMatchTier(val priority: Int, val defaultConfidence: Float) {
    EXACT_HEADWORD(1, 1.0f),       // Tầng 1: Khớp chính xác Headword (NFC Exact)
    CHOSUNG_ALIAS(2, 0.95f),       // Tầng 2: Khớp Chosung gõ tắt (ㄱㅂ -> 기분)
    PRECOMPUTED_FORM(3, 0.92f),    // Tầng 3: Khớp biến thể ngữ pháp tính sẵn (갔어요 -> 가다)
    MORPHOLOGY(4, 0.88f),          // Tầng 4: Bộ máy giải phân tích hình thái
    FTS_FULLTEXT(5, 0.75f)         // Tầng 5: Toàn văn FTS5 (Tiếng Việt không dấu / Prefix)
}

/**
 * Kết quả tra cứu từ vựng chuẩn hóa trả về cho giao diện UI
 */
data class SearchResult(
    val lexemeId: Long,
    val stableKey: String,
    val headwordNfc: String,
    val originType: String,
    val phoneticVi: String? = null,
    val topikLevel: Int? = null,
    val primaryMeaningVi: String,
    val matchTier: SearchMatchTier,
    val confidenceScore: Float = matchTier.defaultConfidence,
    val matchNote: String? = null
)

/**
 * Dữ liệu chi tiết của một nét nghĩa
 */
data class SearchSense(
    val senseId: Long,
    val pos: String?,
    val meaningVi: String,
    val definitionKo: String?
)
