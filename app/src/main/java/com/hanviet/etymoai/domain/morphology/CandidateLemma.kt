package com.hanviet.etymoai.domain.morphology

/**
 * Đại diện cho một ứng viên từ gốc (Candidate Lemma) sau khi giải phân tích hình thái (Deconjugation).
 *
 * @property lemma Từ nguyên thể dạng từ điển (ví dụ: "듣다", "춥다", "가다").
 * @property ruleId Mã định danh quy tắc áp dụng (ví dụ: "IRR_DIGEUT_RIEUL", "REG_PAST_TENSE").
 * @property group Nhóm quy tắc bất quy tắc hoặc null nếu là quy tắc chia đuôi thông thường.
 * @property confidenceScore Điểm tin cậy (0.0 -> 1.0+), được điều chỉnh sau khi đối chiếu từ điển Core.
 * @property explanationVi Diễn giải ngữ pháp bằng tiếng Việt (ví dụ: "Bất quy tắc ㄷ: ...").
 * @property isInDictionary Đã được kiểm chứng có tồn tại trong cơ sở dữ liệu từ điển offline.
 * @property topikLevel Cấp độ TOPIK (nếu có trong từ điển).
 * @property pos Từ loại (động từ, tính từ,...).
 */
data class CandidateLemma(
    val lemma: String,
    val ruleId: String,
    val group: IrregularGroup? = null,
    var confidenceScore: Float,
    val explanationVi: String,
    val isInDictionary: Boolean = false,
    val topikLevel: Int? = null,
    val pos: String? = null
)
