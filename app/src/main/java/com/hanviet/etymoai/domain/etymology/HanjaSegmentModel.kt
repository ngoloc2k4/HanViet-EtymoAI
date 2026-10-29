package com.hanviet.etymoai.domain.etymology

/**
 * Phân loại nguồn gốc từ vựng tiếng Hàn (Etymological Origin Type).
 */
enum class OriginType(val titleVi: String, val code: String) {
    SINO_KOREAN("Hán-Hàn", "SINO_KOREAN"),
    NATIVE_KOREAN("Thuần Hàn", "NATIVE_KOREAN"),
    LOANWORD("Từ ngoại lai", "LOANWORD"),
    MIXED("Hỗn hợp", "MIXED");

    companion object {
        fun fromString(str: String?): OriginType {
            return when (str?.uppercase()) {
                "SINO_KOREAN" -> SINO_KOREAN
                "NATIVE_KOREAN" -> NATIVE_KOREAN
                "LOANWORD" -> LOANWORD
                "MIXED" -> MIXED
                else -> NATIVE_KOREAN
            }
        }
    }
}

/**
 * Mức độ kiểm chứng học thuật của từ nguyên (Academic Verification Status).
 * Tuân thủ nguyên tắc "Không bịa nguồn gốc".
 */
enum class VerificationStatus(val titleVi: String, val badgeLabel: String) {
    VERIFIED_PRIMARY("Đã kiểm chứng chuẩn quốc gia (NIKL)", "Từ điển chuẩn quốc gia"),
    CURATED("Đã kiểm duyệt bởi biên tập viên", "Biên tập viên"),
    INFERRED("Suy luận theo quy tắc chuyển âm", "Quy tắc ngữ âm"),
    AI_SUGGESTED("Gợi ý từ AI (Chưa duyệt)", "AI đề xuất (Chưa duyệt)");

    companion object {
        fun fromString(str: String?): VerificationStatus {
            return when (str?.uppercase()) {
                "VERIFIED_PRIMARY" -> VERIFIED_PRIMARY
                "CURATED" -> CURATED
                "INFERRED" -> INFERRED
                "AI_SUGGESTED" -> AI_SUGGESTED
                else -> INFERRED
            }
        }
    }
}

/**
 * Dữ liệu chi tiết của một Hán tự gốc (Hanja Master Character).
 */
data class HanjaMaster(
    val hanjaChar: String,
    val radical: String? = null,
    val strokeCount: Int? = null,
    val meaningVi: String? = null,
    val generalLevel: String? = null
)

/**
 * Phân đoạn chiết tự của một âm tiết tiếng Hàn tương ứng với chữ Hán và âm Hán-Việt.
 */
data class HanjaSegment(
    val segmentIndex: Int,
    val syllableKo: String,
    val hanjaChar: String? = null,
    val readingVi: String? = null,
    val meaningVi: String? = null
)

/**
 * Tóm tắt một từ vựng có chung gốc chữ Hán trong đồ thị từ nguyên (Etymology Graph).
 */
data class RelatedWordSummary(
    val lexemeId: Long,
    val headword: String,
    val phoneticVi: String? = null,
    val meaningVi: String,
    val originType: OriginType = OriginType.SINO_KOREAN
)

/**
 * Nút phả hệ từ vựng (Etymology Family Node): Gom nhóm toàn bộ từ vựng có chung một chữ Hán.
 */
data class EtymologyFamilyNode(
    val hanjaChar: String,
    val readingVi: String,
    val meaningVi: String,
    val radical: String? = null,
    val strokeCount: Int? = null,
    val relatedWords: List<RelatedWordSummary> = emptyList()
)

/**
 * Hồ sơ từ nguyên hoàn chỉnh của một mục từ tiếng Hàn.
 */
data class WordEtymology(
    val lexemeId: Long,
    val headword: String,
    val originType: OriginType,
    val verificationStatus: VerificationStatus,
    val phoneticVi: String? = null,
    val segments: List<HanjaSegment> = emptyList(),
    val adaptationNote: String? = null
)
