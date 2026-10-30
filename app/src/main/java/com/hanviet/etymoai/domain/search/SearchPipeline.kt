package com.hanviet.etymoai.domain.search

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.hanviet.etymoai.data.db.core.CoreDatabaseHelper
import com.hanviet.etymoai.domain.morphology.MorphologyEngine
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Interface cho Bộ máy Hình thái (Morphology Deconjugation Hook - Tầng 4)
 * Sẽ được triển khai toàn diện tại Phase 2.
 */
fun interface MorphologyDeconjugator {
    fun deconjugate(word: String): List<MorphologyCandidate>
}

data class MorphologyCandidate(
    val lemma: String,
    val ruleApplied: String,
    val grammarExplanation: String,
    val confidence: Float = 0.88f
)

/**
 * Bộ máy Tra cứu 5 Tầng (5-Tier Search Pipeline)
 *
 * Tầng 1: Khớp chính xác Headword (NFC Exact) - Confidence 1.0
 * Tầng 2: Khớp Chosung / Alias gõ tắt (ㄱㅂ -> 기분) - Confidence 0.95
 * Tầng 3: Khớp biến thể ngữ pháp tính sẵn (갔어요 -> 가다) - Confidence 0.92
 * Tầng 4: Bộ máy Hình thái (Morphology Rule Engine Hook) - Confidence 0.88
 * Tầng 5: Toàn văn FTS5 / Nét nghĩa tiếng Việt có dấu & không dấu - Confidence 0.75
 */
class SearchPipeline(
    private val dbProvider: () -> SQLiteDatabase,
    private val deconjugator: MorphologyDeconjugator? = null
) {

    constructor(helper: CoreDatabaseHelper, deconjugator: MorphologyDeconjugator? = null) : this(
        dbProvider = { helper.getReadableDatabase() },
        deconjugator = deconjugator
    )

    private val db: SQLiteDatabase
        get() = dbProvider()

    private val activeDeconjugator: MorphologyDeconjugator by lazy {
        deconjugator ?: MorphologyEngine(dbProvider)
    }

    // Mẫu kiểm tra ký tự Chosung Hangul thuần túy (ㄱ - ㅎ)
    private val chosungPattern = Pattern.compile("^[\\u3131-\\u314E\\u1100-\\u1112]+$")

    companion object {
        private val FTS_SANITIZER_REGEX = Regex("[^\\p{L}\\p{N}\\s]")
        private val PUNCTUATION_SPLIT_REGEX = Regex("[\\s,;:.!?，。！？]+")
    }

    /**
     * Tra cứu từ khóa theo quy trình 5 tầng có xếp hạng trọng số và khử trùng lặp.
     */
    fun search(rawQuery: String, limit: Int = 30): List<SearchResult> {
        val query = Normalizer.normalize(rawQuery.trim(), Normalizer.Form.NFC)
        if (query.isEmpty()) return emptyList()

        val resultsMap = LinkedHashMap<Long, SearchResult>()

        // ------------------------------------------------------------------
        // TẦNG 1: Khớp chính xác Headword (NFC Exact)
        // ------------------------------------------------------------------
        queryExactHeadword(query)?.let { exactResult ->
            resultsMap[exactResult.lexemeId] = exactResult
        }

        // ------------------------------------------------------------------
        // TẦNG 2: Khớp Alias / Chosung (ㄱㅂ -> 기분)
        // ------------------------------------------------------------------
        if (isChosungQuery(query) || resultsMap.isEmpty()) {
            queryAliases(query).forEach { aliasResult ->
                if (!resultsMap.containsKey(aliasResult.lexemeId)) {
                    resultsMap[aliasResult.lexemeId] = aliasResult
                }
            }
        }

        // ------------------------------------------------------------------
        // TẦNG 3: Khớp Biến thể Ngữ pháp Tính sẵn (Precomputed Forms)
        // ------------------------------------------------------------------
        queryPrecomputedForms(query).forEach { precomputedResult ->
            if (!resultsMap.containsKey(precomputedResult.lexemeId)) {
                resultsMap[precomputedResult.lexemeId] = precomputedResult
            }
        }

        // ------------------------------------------------------------------
        // TẦNG 4: Bộ máy Phân tích Hình thái (Morphology Rule Engine)
        // ------------------------------------------------------------------
        val candidates = activeDeconjugator.deconjugate(query)
        for (cand in candidates) {
            queryExactHeadword(cand.lemma)?.let { baseWord ->
                if (!resultsMap.containsKey(baseWord.lexemeId)) {
                    resultsMap[baseWord.lexemeId] = baseWord.copy(
                        matchTier = SearchMatchTier.MORPHOLOGY,
                        confidenceScore = cand.confidence,
                        matchNote = "${cand.grammarExplanation} (${cand.ruleApplied})"
                    )
                }
            }
        }

        // ------------------------------------------------------------------
        // TẦNG 5: Full-Text Search FTS5 / Nghĩa tiếng Việt không dấu & có dấu
        // ------------------------------------------------------------------
        queryFts5(query, limit).forEach { ftsResult ->
            if (!resultsMap.containsKey(ftsResult.lexemeId)) {
                resultsMap[ftsResult.lexemeId] = ftsResult
            }
        }

        return resultsMap.values
            .sortedWith(compareByDescending<SearchResult> { it.confidenceScore }
                .thenBy { it.matchTier.priority }
                .thenByDescending {
                    val m = it.primaryMeaningVi.trim().lowercase()
                    val q = query.trim().lowercase()
                    m == q || m.startsWith("$q,") || m.startsWith("$q ") || m.startsWith("$q;")
                }
                .thenBy { it.headwordNfc.length }
                .thenBy { if (it.topikLevel != null && it.topikLevel > 0) it.topikLevel else 99 }
                .thenBy { it.lexemeId })
            .take(limit)
    }

    /**
     * Thuật toán trích xuất cụm từ ghép N-Gram (Longest Match First)
     * Giải quyết tình huống nhập câu dài ("học chào hỏi rồi học văn hoá" -> ["chào hỏi", "văn hoá"])
     */
    fun extractCompoundKeywords(sentence: String): List<String> {
        val normalized = Normalizer.normalize(sentence.trim(), Normalizer.Form.NFC)
        if (normalized.isEmpty()) return emptyList()

        val tokens = normalized.split(PUNCTUATION_SPLIT_REGEX).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return emptyList()

        val foundKeywords = mutableListOf<String>()
        var i = 0
        val maxN = 3 // Quét tối đa cụm 3 từ (3-gram -> 2-gram -> 1-gram)

        while (i < tokens.size) {
            var matchedPhrase: String? = null
            var matchedLength = 0

            for (ngramSize in minOf(maxN, tokens.size - i) downTo 1) {
                val candidatePhrase = tokens.subList(i, i + ngramSize).joinToString(" ")
                if (hasDictionaryEntry(candidatePhrase)) {
                    matchedPhrase = candidatePhrase
                    matchedLength = ngramSize
                    break
                }
            }

            if (matchedPhrase != null) {
                foundKeywords.add(matchedPhrase)
                i += matchedLength // Nhảy cóc qua cụm từ đã tìm thấy
            } else {
                i += 1
            }
        }

        return foundKeywords
    }

    /**
     * Tra cứu nhanh xem cụm từ có tồn tại trong từ điển hay không (phục vụ N-Gram)
     */
    fun hasDictionaryEntry(term: String): Boolean {
        val cleanTerm = term.trim()
        if (cleanTerm.isEmpty()) return false

        // 1. Kiểm tra headword trực tiếp
        db.rawQuery("SELECT 1 FROM lexemes WHERE headword_nfc = ? LIMIT 1", arrayOf(cleanTerm)).use { cursor ->
            if (cursor.moveToFirst()) return true
        }

        // 2. Kiểm tra precomputed
        db.rawQuery("SELECT 1 FROM precomputed_forms WHERE inflected_form = ? LIMIT 1", arrayOf(cleanTerm)).use { cursor ->
            if (cursor.moveToFirst()) return true
        }

        // 3. Kiểm tra cụm từ chính xác qua FTS5 (đảm bảo khớp trọn vẹn từ/cụm từ, tránh khớp substring sai)
        val sanitized = cleanFtsQuery(cleanTerm)
        if (sanitized.isNotBlank()) {
            val ftsPhrase = "\"$sanitized\""
            try {
                db.rawQuery("SELECT 1 FROM search_fts WHERE search_fts MATCH ? LIMIT 1", arrayOf(ftsPhrase)).use { cursor ->
                    if (cursor.moveToFirst()) return true
                }
            } catch (_: Exception) {
                // Fallback nếu FTS không khả dụng
            }
        }

        // 4. Kiểm tra nghĩa tiếng Việt trong bảng senses với đối chiếu nguyên từ
        val unaccented = removeVietnameseDiacritics(cleanTerm)
        db.rawQuery(
            "SELECT 1 FROM senses WHERE meaning_vi = ? OR meaning_vi_normalized = ? OR meaning_vi LIKE ? OR meaning_vi_normalized LIKE ? OR meaning_vi LIKE ? OR meaning_vi_normalized LIKE ? LIMIT 1",
            arrayOf(cleanTerm, unaccented, "$cleanTerm, %", "$unaccented, %", "%, $cleanTerm%", "%, $unaccented%")
        ).use { cursor ->
            if (cursor.moveToFirst()) return true
        }

        return false
    }

    /**
     * Hàm loại bỏ dấu tiếng Việt chuẩn Unicode NFD
     */
    fun removeVietnameseDiacritics(str: String): String {
        val nfd = Normalizer.normalize(str, Normalizer.Form.NFD)
        val withoutDiacritics = nfd.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return withoutDiacritics.replace('đ', 'd').replace('Đ', 'D')
    }

    // ======================================================================
    // CÁC HÀM TRUY VẤN NỘI BỘ
    // ======================================================================

    private fun isChosungQuery(query: String): Boolean {
        return chosungPattern.matcher(query).matches()
    }

    private fun queryExactHeadword(query: String): SearchResult? {
        val sql = """
            SELECT l.lexeme_id, l.stable_key, l.headword_nfc, l.origin_type, l.phonetic_vi, l.topik_level,
                   (SELECT meaning_vi FROM senses s WHERE s.lexeme_id = l.lexeme_id ORDER BY s.display_order ASC LIMIT 1) AS primary_meaning
            FROM lexemes l
            WHERE l.headword_nfc = ?
            LIMIT 1
        """.trimIndent()

        db.rawQuery(sql, arrayOf(query)).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursorToSearchResult(cursor, SearchMatchTier.EXACT_HEADWORD, 1.0f, null)
            }
        }
        return null
    }

    private fun queryAliases(query: String): List<SearchResult> {
        val sql = """
            SELECT l.lexeme_id, l.stable_key, l.headword_nfc, l.origin_type, l.phonetic_vi, l.topik_level,
                   a.alias_text, a.alias_type,
                   (SELECT meaning_vi FROM senses s WHERE s.lexeme_id = l.lexeme_id ORDER BY s.display_order ASC LIMIT 1) AS primary_meaning
            FROM aliases a
            JOIN lexemes l ON a.lexeme_id = l.lexeme_id
            WHERE a.alias_text = ?
            ORDER BY CASE WHEN l.topik_level > 0 THEN l.topik_level ELSE 99 END ASC, l.lexeme_id ASC
            LIMIT 50
        """.trimIndent()

        val list = mutableListOf<SearchResult>()
        db.rawQuery(sql, arrayOf(query)).use { cursor ->
            while (cursor.moveToNext()) {
                val aliasText = cursor.getString(cursor.getColumnIndexOrThrow("alias_text"))
                val aliasType = cursor.getString(cursor.getColumnIndexOrThrow("alias_type"))
                list.add(
                    cursorToSearchResult(
                        cursor,
                        SearchMatchTier.CHOSUNG_ALIAS,
                        0.95f,
                        "Khớp $aliasType: $aliasText"
                    )
                )
            }
        }
        return list
    }

    private fun queryPrecomputedForms(query: String): List<SearchResult> {
        val sql = """
            SELECT l.lexeme_id, l.stable_key, l.headword_nfc, l.origin_type, l.phonetic_vi, l.topik_level,
                   p.inflected_form, p.grammar_desc, p.confidence,
                   (SELECT meaning_vi FROM senses s WHERE s.lexeme_id = l.lexeme_id ORDER BY s.display_order ASC LIMIT 1) AS primary_meaning
            FROM precomputed_forms p
            JOIN lexemes l ON p.lexeme_id = l.lexeme_id
            WHERE p.inflected_form = ?
            LIMIT 20
        """.trimIndent()

        val list = mutableListOf<SearchResult>()
        db.rawQuery(sql, arrayOf(query)).use { cursor ->
            while (cursor.moveToNext()) {
                val form = cursor.getString(cursor.getColumnIndexOrThrow("inflected_form"))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow("grammar_desc")) ?: "Dạng ngữ pháp"
                val conf = if (!cursor.isNull(cursor.getColumnIndexOrThrow("confidence"))) {
                    cursor.getFloat(cursor.getColumnIndexOrThrow("confidence"))
                } else {
                    0.92f
                }
                list.add(
                    cursorToSearchResult(
                        cursor,
                        SearchMatchTier.PRECOMPUTED_FORM,
                        conf,
                        "$desc ($form)"
                    )
                )
            }
        }
        return list
    }

    private fun queryFts5(query: String, limit: Int): List<SearchResult> {
        val clean = cleanFtsQuery(query)
        if (clean.isBlank()) return emptyList()

        val list = mutableListOf<SearchResult>()
        val seenLexemeIds = mutableSetOf<Long>()

        // 1. Thử tìm kiếm FTS5
        val ftsExpression = "\"$clean\"*"
        val ftsSql = """
            SELECT l.lexeme_id, l.stable_key, l.headword_nfc, l.origin_type, l.phonetic_vi, l.topik_level,
                   (SELECT meaning_vi FROM senses s WHERE s.lexeme_id = l.lexeme_id ORDER BY s.display_order ASC LIMIT 1) AS primary_meaning
            FROM search_fts
            JOIN lexemes l ON search_fts.rowid = l.lexeme_id
            WHERE search_fts MATCH ?
            LIMIT ?
        """.trimIndent()

        try {
            db.rawQuery(ftsSql, arrayOf(ftsExpression, limit.toString())).use { cursor ->
                while (cursor.moveToNext()) {
                    val result = cursorToSearchResult(
                        cursor,
                        SearchMatchTier.FTS_FULLTEXT,
                        0.75f,
                        "Khớp toàn văn"
                    )
                    if (seenLexemeIds.add(result.lexemeId)) {
                        list.add(result)
                    }
                }
            }
        } catch (_: Exception) {
            // Khi FTS5 không khả dụng trong môi trường runtime (như một số phiên bản Robolectric/SQLite), tiếp tục fallback
        }

        // 2. Dự phòng tra cứu bảng senses (meaning_vi và meaning_vi_normalized) nếu FTS5 chưa đủ kết quả
        if (list.size < limit) {
            val unaccented = removeVietnameseDiacritics(clean)
            val sensesSql = """
                SELECT DISTINCT l.lexeme_id, l.stable_key, l.headword_nfc, l.origin_type, l.phonetic_vi, l.topik_level,
                       (SELECT s2.meaning_vi FROM senses s2 WHERE s2.lexeme_id = l.lexeme_id ORDER BY s2.display_order ASC LIMIT 1) AS primary_meaning
                FROM senses s
                JOIN lexemes l ON s.lexeme_id = l.lexeme_id
                WHERE s.meaning_vi_normalized LIKE ? OR s.meaning_vi LIKE ?
                LIMIT ?
            """.trimIndent()

            try {
                db.rawQuery(sensesSql, arrayOf("%$unaccented%", "%$clean%", (limit - list.size).toString())).use { cursor ->
                    while (cursor.moveToNext()) {
                        val result = cursorToSearchResult(
                            cursor,
                            SearchMatchTier.FTS_FULLTEXT,
                            0.75f,
                            "Khớp nét nghĩa tiếng Việt"
                        )
                        if (seenLexemeIds.add(result.lexemeId)) {
                            list.add(result)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }

        return list
    }

    private fun cleanFtsQuery(input: String): String {
        return input.replace(FTS_SANITIZER_REGEX, "").trim()
    }

    private fun cursorToSearchResult(
        cursor: Cursor,
        tier: SearchMatchTier,
        confidence: Float,
        note: String?
    ): SearchResult {
        return SearchResult(
            lexemeId = cursor.getLong(cursor.getColumnIndexOrThrow("lexeme_id")),
            stableKey = cursor.getString(cursor.getColumnIndexOrThrow("stable_key")),
            headwordNfc = cursor.getString(cursor.getColumnIndexOrThrow("headword_nfc")),
            originType = cursor.getString(cursor.getColumnIndexOrThrow("origin_type")),
            phoneticVi = cursor.getStringOrNull("phonetic_vi"),
            topikLevel = cursor.getIntOrNull("topik_level"),
            primaryMeaningVi = cursor.getStringOrNull("primary_meaning") ?: "",
            matchTier = tier,
            confidenceScore = confidence,
            matchNote = note
        )
    }

    private fun Cursor.getStringOrNull(columnName: String): String? {
        val idx = getColumnIndex(columnName)
        return if (idx >= 0 && !isNull(idx)) getString(idx) else null
    }

    private fun Cursor.getIntOrNull(columnName: String): Int? {
        val idx = getColumnIndex(columnName)
        return if (idx >= 0 && !isNull(idx)) getInt(idx) else null
    }
}
