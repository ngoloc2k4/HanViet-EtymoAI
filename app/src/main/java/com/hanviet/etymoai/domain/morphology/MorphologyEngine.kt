package com.hanviet.etymoai.domain.morphology

import android.database.sqlite.SQLiteDatabase
import com.hanviet.etymoai.data.db.core.CoreDatabaseHelper
import com.hanviet.etymoai.domain.search.MorphologyCandidate
import com.hanviet.etymoai.domain.search.MorphologyDeconjugator
import java.text.Normalizer

/**
 * Bộ máy Phân tích Hình thái & Xếp hạng Ứng viên (Morphology Rule Engine & Candidate Ranking).
 *
 * Nhiệm vụ:
 * 1. Tiếp nhận từ đã chia đuôi ngữ pháp (surface form, e.g. "들었어요", "추워서", "갑니다").
 * 2. Phân tích qua 7 nhóm động/tính từ bất quy tắc (ㅂ, ㄷ, ㅅ, ㅎ, 르, 으, ㄹ) từ [IrregularVerbRules].
 * 3. Phân tích qua các quy tắc chia đuôi thường (thì quá khứ, kính ngữ, liên từ, điều kiện).
 * 4. Đối chiếu từng ứng viên với cơ sở dữ liệu `dictionary_core.db`:
 *    - Tăng điểm tin cậy nếu từ gốc tồn tại trong từ điển (+0.40) và thuộc từ vựng sơ cấp TOPIK 1-3 (+0.10).
 *    - Giảm điểm hoặc loại bỏ các ứng viên suy diễn ảo không có trong từ điển.
 * 5. Xếp hạng và trả về danh sách [CandidateLemma] hoặc [MorphologyCandidate] cho [SearchPipeline].
 */
class MorphologyEngine(
    private val dbProvider: (() -> SQLiteDatabase)? = null
) : MorphologyDeconjugator {

    constructor(helper: CoreDatabaseHelper) : this(dbProvider = { helper.getReadableDatabase() })

    private val db: SQLiteDatabase?
        get() = try {
            dbProvider?.invoke()
        } catch (_: Exception) {
            null
        }

    /**
     * Phân tích hình thái và xếp hạng ứng viên từ gốc cho từ bề mặt.
     */
    fun analyze(surfaceForm: String): List<CandidateLemma> {
        val clean = Normalizer.normalize(surfaceForm.trim(), Normalizer.Form.NFC)
        if (clean.isBlank()) return emptyList()

        val rawCandidates = mutableListOf<CandidateLemma>()

        // 1. Chạy qua 7 nhóm bất quy tắc (Irregular Verb Rules)
        val irregulars = IrregularVerbRules.deconjugate(clean)
        rawCandidates.addAll(irregulars)

        // 2. Chạy qua các quy tắc chia đuôi động từ thông dụng (Regular Conjugation Rules)
        val regulars = extractRegularCandidates(clean)
        rawCandidates.addAll(regulars)

        // 3. Khử trùng lặp sơ bộ và loại bỏ chính surfaceForm nếu bị trùng
        val candidateMap = mutableMapOf<String, CandidateLemma>()
        for (cand in rawCandidates) {
            val lemma = cand.lemma.trim()
            if (lemma.isNotEmpty() && lemma != clean && isValidKoreanVerb(lemma)) {
                val existing = candidateMap[lemma]
                if (existing == null || cand.confidenceScore > existing.confidenceScore) {
                    candidateMap[lemma] = cand
                }
            }
        }

        // 4. Đối chiếu từ điển Core (nếu có database) để xếp hạng trọng số
        val database = db
        val verifiedCandidates = candidateMap.values.map { cand ->
            if (database != null) {
                verifyCandidateInDb(database, cand)
            } else {
                cand
            }
        }

        // 5. Lọc và sắp xếp ứng viên
        return verifiedCandidates
            .filter { cand ->
                // Nếu có DB, ưu tiên tuyệt đối từ có trong từ điển
                if (database != null) {
                    cand.isInDictionary || cand.confidenceScore >= 0.85f
                } else {
                    true
                }
            }
            .sortedWith(
                compareByDescending<CandidateLemma> { it.confidenceScore }
                    .thenByDescending { it.isInDictionary }
                    .thenBy { it.lemma.length }
            )
    }

    /**
     * Triển khai interface MorphologyDeconjugator để cắm trực tiếp vào SearchPipeline (Tầng 4).
     */
    override fun deconjugate(word: String): List<MorphologyCandidate> {
        val candidates = analyze(word)
        return candidates.map { cand ->
            MorphologyCandidate(
                lemma = cand.lemma,
                ruleApplied = cand.ruleId,
                grammarExplanation = cand.explanationVi,
                confidence = cand.confidenceScore
            )
        }
    }

    /**
     * Trích xuất các ứng viên từ gốc dựa trên các đuôi ngữ pháp tiếng Hàn thông dụng.
     */
    private fun extractRegularCandidates(clean: String): List<CandidateLemma> {
        val list = mutableListOf<CandidateLemma>()

        // A. Thì quá khứ (-았/었/였/했-)
        // Dạng 1: Tiếp vĩ ngữ quá khứ rõ ràng (-았어요, -었어요, -았습니다, -었습니다, -았다, -었다, -았어, -었어)
        val pastEndings = listOf(
            "았어요", "었어요", "았습니다", "었습니다",
            "았다", "었다", "았어", "었어"
        )
        for (ending in pastEndings) {
            if (clean.endsWith(ending) && clean.length > ending.length) {
                val stem = clean.substring(0, clean.length - ending.length)
                list.add(
                    CandidateLemma(
                        lemma = stem + "다",
                        ruleId = "REG_PAST_TENSE",
                        group = null,
                        confidenceScore = 0.92f,
                        explanationVi = "Thì quá khứ -았/었-"
                    )
                )
            }
        }

        // Dạng 2: Động từ đuôi -하다 quá khứ (-했어요, -했습니다, -했다, -했어)
        val hadaPastEndings = listOf("했어요", "했습니다", "했다", "했어")
        for (ending in hadaPastEndings) {
            if (clean.endsWith(ending) && clean.length >= ending.length) {
                val prefix = clean.substring(0, clean.length - ending.length)
                list.add(
                    CandidateLemma(
                        lemma = prefix + "하다",
                        ruleId = "REG_PAST_HADA",
                        group = null,
                        confidenceScore = 0.95f,
                        explanationVi = "Động từ đuôi -하다 (thì quá khứ -했-)"
                    )
                )
            }
        }

        // Dạng 3: Co nguyên âm thì quá khứ chứa patchim ㅆ (e.g. 갔다 -> 가다, 봤다 -> 보다, 줬다 -> 주다)
        for (pastEnd in listOf("어요", "습니다", "다", "어")) {
            if (clean.endsWith(pastEnd) && clean.length > pastEnd.length) {
                val rem = clean.substring(0, clean.length - pastEnd.length)
                val lastChar = rem.last()
                val dec = HangulUtil.decompose(lastChar)
                if (dec != null && dec.jong == 20) { // 20 = JONG_SSANGSIOT (ㅆ)
                    val stemPrefix = rem.dropLast(1)
                    when (dec.jung) {
                        HangulUtil.JUNG_A -> { // ㅏ: 가다, 자다, 사다
                            val restored = HangulUtil.withoutPatchim(lastChar)
                            list.add(
                                CandidateLemma(
                                    lemma = stemPrefix + restored + "다",
                                    ruleId = "REG_PAST_CONTRACTED_A",
                                    group = null,
                                    confidenceScore = 0.93f,
                                    explanationVi = "Thì quá khứ co nguyên âm (ㅏ + 았 -> 았)"
                                )
                            )
                        }
                        HangulUtil.JUNG_WA -> { // ㅘ: 보다, 오다
                            val restored = HangulUtil.compose(dec.cho, HangulUtil.JUNG_O, HangulUtil.JONG_NONE)
                            list.add(
                                CandidateLemma(
                                    lemma = stemPrefix + restored + "다",
                                    ruleId = "REG_PAST_CONTRACTED_WA",
                                    group = null,
                                    confidenceScore = 0.93f,
                                    explanationVi = "Thì quá khứ co nguyên âm (ㅗ + 아 -> ㅘ)"
                                )
                            )
                        }
                        HangulUtil.JUNG_WO -> { // ㅝ: 주다, 배우다, 키우다
                            val restored = HangulUtil.compose(dec.cho, HangulUtil.JUNG_U, HangulUtil.JONG_NONE)
                            list.add(
                                CandidateLemma(
                                    lemma = stemPrefix + restored + "다",
                                    ruleId = "REG_PAST_CONTRACTED_WO",
                                    group = null,
                                    confidenceScore = 0.93f,
                                    explanationVi = "Thì quá khứ co nguyên âm (ㅜ + 어 -> ㅝ)"
                                )
                            )
                        }
                        HangulUtil.JUNG_AE -> { // ㅐ: 보내다, 내다
                            val restored = HangulUtil.withoutPatchim(lastChar)
                            list.add(
                                CandidateLemma(
                                    lemma = stemPrefix + restored + "다",
                                    ruleId = "REG_PAST_CONTRACTED_AE",
                                    group = null,
                                    confidenceScore = 0.93f,
                                    explanationVi = "Thì quá khứ co nguyên âm (ㅐ + 어 -> ㅐ)"
                                )
                            )
                        }
                    }
                }
            }
        }

        // B. Hiện tại thân mật (-아요 / -어요 / -해요)
        for (ending in listOf("아요", "어요")) {
            if (clean.endsWith(ending) && clean.length > ending.length) {
                val stem = clean.substring(0, clean.length - ending.length)
                list.add(
                    CandidateLemma(
                        lemma = stem + "다",
                        ruleId = "REG_PRES_POLITE",
                        group = null,
                        confidenceScore = 0.90f,
                        explanationVi = "Hiện tại thân mật -$ending"
                    )
                )
            }
        }
        if (clean.endsWith("해요") && clean.length >= 2) {
            val prefix = clean.substring(0, clean.length - 2)
            list.add(
                CandidateLemma(
                    lemma = prefix + "하다",
                    ruleId = "REG_PRES_HADA",
                    group = null,
                    confidenceScore = 0.94f,
                    explanationVi = "Động từ đuôi -하다 (thì hiện tại -해요)"
                )
            )
        }

        // C. Liên từ & thứ tự (-아서 / -어서 / -해서 / -고 / -지만)
        for (ending in listOf("아서", "어서")) {
            if (clean.endsWith(ending) && clean.length > ending.length) {
                val stem = clean.substring(0, clean.length - ending.length)
                list.add(
                    CandidateLemma(
                        lemma = stem + "다",
                        ruleId = "REG_CONN_SEO",
                        group = null,
                        confidenceScore = 0.90f,
                        explanationVi = "Liên từ chỉ nguyên nhân/thứ tự -$ending"
                    )
                )
            }
        }
        if (clean.endsWith("해서") && clean.length >= 2) {
            val prefix = clean.substring(0, clean.length - 2)
            list.add(
                CandidateLemma(
                    lemma = prefix + "하다",
                    ruleId = "REG_CONN_HADA",
                    group = null,
                    confidenceScore = 0.93f,
                    explanationVi = "Liên từ chỉ nguyên nhân đuôi -하다 (-해서)"
                )
            )
        }
        if (clean.endsWith("서") && clean.length >= 2) {
            val prevChar = clean[clean.length - 2]
            val dec = HangulUtil.decompose(prevChar)
            if (dec != null && dec.jong == HangulUtil.JONG_NONE && dec.jung == HangulUtil.JUNG_A) {
                // e.g. 가서 -> 가다
                val stem = clean.substring(0, clean.length - 1)
                list.add(
                    CandidateLemma(
                        lemma = stem + "다",
                        ruleId = "REG_CONN_SEO_VOWEL",
                        group = null,
                        confidenceScore = 0.90f,
                        explanationVi = "Liên từ chỉ nguyên nhân/thứ tự -서"
                    )
                )
            }
        }
        if (clean.endsWith("고") && clean.length >= 2) {
            val stem = clean.dropLast(1)
            list.add(
                CandidateLemma(
                    lemma = stem + "다",
                    ruleId = "REG_CONN_GO",
                    group = null,
                    confidenceScore = 0.89f,
                    explanationVi = "Liên từ liên kết -고"
                )
            )
        }
        if (clean.endsWith("지만") && clean.length >= 3) {
            val stem = clean.dropLast(2)
            list.add(
                CandidateLemma(
                    lemma = stem + "다",
                    ruleId = "REG_CONN_JIMAN",
                    group = null,
                    confidenceScore = 0.89f,
                    explanationVi = "Liên từ tương phản -지만"
                )
            )
        }

        // D. Kính ngữ trang trọng (-습니다 / -ㅂ니다)
        if (clean.endsWith("습니다") && clean.length > 3) {
            val stem = clean.substring(0, clean.length - 3)
            list.add(
                CandidateLemma(
                    lemma = stem + "다",
                    ruleId = "REG_FORMAL_SEUMNIDA",
                    group = null,
                    confidenceScore = 0.94f,
                    explanationVi = "Đuôi kính ngữ trang trọng -습니다"
                )
            )
        }
        if (clean.endsWith("니다") && clean.length > 2) {
            val prevChar = clean[clean.length - 3]
            val dec = HangulUtil.decompose(prevChar)
            if (dec != null && dec.jong == HangulUtil.JONG_BIEUP) {
                val restored = HangulUtil.compose(dec.cho, dec.jung, HangulUtil.JONG_NONE)
                val stemPrefix = clean.substring(0, clean.length - 3)
                list.add(
                    CandidateLemma(
                        lemma = stemPrefix + restored + "다",
                        ruleId = "REG_FORMAL_BIEUPNIDA",
                        group = null,
                        confidenceScore = 0.93f,
                        explanationVi = "Đuôi kính ngữ trang trọng -ㅂ니다"
                    )
                )
            }
        }

        // E. Điều kiện & Căn cứ (-으면 / -면 / -으니까 / -니까)
        if (clean.endsWith("으면") && clean.length > 2) {
            val stem = clean.substring(0, clean.length - 2)
            list.add(
                CandidateLemma(
                    lemma = stem + "다",
                    ruleId = "REG_COND_EUMYEON",
                    group = null,
                    confidenceScore = 0.90f,
                    explanationVi = "Đuôi điều kiện giả định -으면"
                )
            )
        } else if (clean.endsWith("면") && clean.length > 1) {
            val stem = clean.substring(0, clean.length - 1)
            list.add(
                CandidateLemma(
                    lemma = stem + "다",
                    ruleId = "REG_COND_MYEON",
                    group = null,
                    confidenceScore = 0.88f,
                    explanationVi = "Đuôi điều kiện giả định -면"
                )
            )
        }
        if (clean.endsWith("으니까") && clean.length > 3) {
            val stem = clean.substring(0, clean.length - 3)
            list.add(
                CandidateLemma(
                    lemma = stem + "다",
                    ruleId = "REG_CAUSAL_EU_NIKKA",
                    group = null,
                    confidenceScore = 0.90f,
                    explanationVi = "Đuôi chỉ lý do/căn cứ -으니까"
                )
            )
        } else if (clean.endsWith("니까") && clean.length > 2) {
            val stem = clean.substring(0, clean.length - 2)
            list.add(
                CandidateLemma(
                    lemma = stem + "다",
                    ruleId = "REG_CAUSAL_NIKKA",
                    group = null,
                    confidenceScore = 0.88f,
                    explanationVi = "Đuôi chỉ lý do/căn cứ -니까"
                )
            )
        }

        // F. Đuôi mệnh lệnh tôn kính (-으세요 / -세요)
        if (clean.endsWith("으세요") && clean.length > 3) {
            val stem = clean.substring(0, clean.length - 3)
            list.add(
                CandidateLemma(
                    lemma = stem + "다",
                    ruleId = "REG_HONORIFIC_EUSEYO",
                    group = null,
                    confidenceScore = 0.91f,
                    explanationVi = "Đuôi mệnh lệnh/yêu cầu tôn kính -으세요"
                )
            )
        } else if (clean.endsWith("세요") && clean.length > 2) {
            val stem = clean.substring(0, clean.length - 2)
            list.add(
                CandidateLemma(
                    lemma = stem + "다",
                    ruleId = "REG_HONORIFIC_SEYO",
                    group = null,
                    confidenceScore = 0.89f,
                    explanationVi = "Đuôi mệnh lệnh/yêu cầu tôn kính -세요"
                )
            )
        }

        return list
    }

    /**
     * Xác thực sự tồn tại của từ gốc trong cơ sở dữ liệu `dictionary_core.db`.
     * Tăng điểm tin cậy nếu có trong từ điển, giảm điểm nếu không tồn tại.
     */
    private fun verifyCandidateInDb(db: SQLiteDatabase, candidate: CandidateLemma): CandidateLemma {
        return try {
            val cursor = db.rawQuery(
                """
                SELECT l.lexeme_id, l.topik_level, s.part_of_speech
                FROM lexemes l
                LEFT JOIN senses s ON l.lexeme_id = s.lexeme_id
                WHERE l.headword_nfc = ?
                LIMIT 1
                """.trimIndent(),
                arrayOf(candidate.lemma)
            )

            cursor.use { c ->
                if (c.moveToFirst()) {
                    val topik = if (!c.isNull(1)) c.getInt(1) else null
                    val pos = if (!c.isNull(2)) c.getString(2) else null

                    // Thưởng điểm khi từ có thực trong CSDL
                    var boostedScore = candidate.confidenceScore + 0.35f
                    if (topik != null && topik in 1..3) {
                        boostedScore += 0.10f // Ưu tiên từ vựng phổ dụng sơ-trung cấp
                    }

                    candidate.copy(
                        confidenceScore = minOf(1.0f, boostedScore),
                        isInDictionary = true,
                        topikLevel = topik,
                        pos = pos
                    )
                } else {
                    // Không tìm thấy trong CSDL -> Hạ điểm
                    candidate.copy(
                        confidenceScore = candidate.confidenceScore * 0.40f,
                        isInDictionary = false
                    )
                }
            }
        } catch (_: Exception) {
            candidate
        }
    }

    private fun isValidKoreanVerb(lemma: String): Boolean {
        if (!lemma.endsWith("다") || lemma.length < 2) return false
        val stem = lemma.dropLast(1)
        return stem.all { HangulUtil.isHangulSyllable(it) }
    }
}
