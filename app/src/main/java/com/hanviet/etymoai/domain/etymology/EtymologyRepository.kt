package com.hanviet.etymoai.domain.etymology

import android.database.sqlite.SQLiteDatabase
import com.hanviet.etymoai.data.db.core.CoreDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Interface Repository quản lý truy vấn từ nguyên và chiết tự Hán tự
 */
interface EtymologyRepository {
    suspend fun getEtymologyForWord(lexemeId: Long): WordEtymology?
    suspend fun getEtymologyForWordByHeadword(headword: String): WordEtymology?
    fun getEtymologyForWordFlow(lexemeId: Long): Flow<WordEtymology?>

    suspend fun getEtymologyFamily(hanjaChar: String): EtymologyFamilyNode?
    fun getEtymologyFamilyFlow(hanjaChar: String): Flow<EtymologyFamilyNode?>

    suspend fun getHanjaMaster(hanjaChar: String): HanjaMaster?
}

/**
 * Triển khai mặc định của EtymologyRepository truy vấn trực tiếp CSDL Core SQLite
 */
class DefaultEtymologyRepository(
    private val dbProvider: () -> SQLiteDatabase
) : EtymologyRepository {

    constructor(helper: CoreDatabaseHelper) : this(dbProvider = { helper.getReadableDatabase() })

    private val db: SQLiteDatabase
        get() = dbProvider()

    override suspend fun getEtymologyForWord(lexemeId: Long): WordEtymology? = withContext(Dispatchers.IO) {
        val lexSql = """
            SELECT l.lexeme_id, l.headword_nfc, l.origin_type, l.origin_status, l.phonetic_vi,
                   wo.adaptation_note, wo.verification_status
            FROM lexemes l
            LEFT JOIN word_origins wo ON l.lexeme_id = wo.lexeme_id
            WHERE l.lexeme_id = ?
            LIMIT 1
        """.trimIndent()

        var wordEtymology: WordEtymology? = null
        db.rawQuery(lexSql, arrayOf(lexemeId.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                val hw = cursor.getString(1)
                val origTypeStr = cursor.getString(2)
                val origStatusStr = cursor.getString(3)
                val phoneticVi = if (!cursor.isNull(4)) cursor.getString(4) else null
                val adaptationNote = if (!cursor.isNull(5)) cursor.getString(5) else null
                val verStatusStr = if (!cursor.isNull(6)) cursor.getString(6) else origStatusStr

                val originType = OriginType.fromString(origTypeStr)
                val verificationStatus = VerificationStatus.fromString(verStatusStr)

                wordEtymology = WordEtymology(
                    lexemeId = lexemeId,
                    headword = hw,
                    originType = originType,
                    verificationStatus = verificationStatus,
                    phoneticVi = phoneticVi,
                    adaptationNote = adaptationNote,
                    segments = emptyList()
                )
            }
        }

        if (wordEtymology == null) return@withContext null

        val segSql = """
            SELECT segment_index, syllable_ko, hanja_char, reading_vi, meaning_vi
            FROM lexeme_hanja_segments
            WHERE lexeme_id = ?
            ORDER BY segment_index ASC
        """.trimIndent()

        val segments = mutableListOf<HanjaSegment>()
        db.rawQuery(segSql, arrayOf(lexemeId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                val idx = cursor.getInt(0)
                val syl = cursor.getString(1)
                val hanja = if (!cursor.isNull(2)) cursor.getString(2) else null
                val readVi = if (!cursor.isNull(3)) cursor.getString(3) else null
                val meanVi = if (!cursor.isNull(4)) cursor.getString(4) else null
                segments.add(
                    HanjaSegment(
                        segmentIndex = idx,
                        syllableKo = syl,
                        hanjaChar = hanja,
                        readingVi = readVi,
                        meaningVi = meanVi
                    )
                )
            }
        }

        wordEtymology?.copy(segments = segments)
    }

    override suspend fun getEtymologyForWordByHeadword(headword: String): WordEtymology? = withContext(Dispatchers.IO) {
        val cleanHw = headword.trim()
        if (cleanHw.isEmpty()) return@withContext null
        var lexId: Long? = null
        db.rawQuery("SELECT lexeme_id FROM lexemes WHERE headword_nfc = ? LIMIT 1", arrayOf(cleanHw)).use { cursor ->
            if (cursor.moveToFirst()) {
                lexId = cursor.getLong(0)
            }
        }
        lexId?.let { getEtymologyForWord(it) }
    }

    override fun getEtymologyForWordFlow(lexemeId: Long): Flow<WordEtymology?> = flow {
        emit(getEtymologyForWord(lexemeId))
    }.flowOn(Dispatchers.IO)

    override suspend fun getEtymologyFamily(hanjaChar: String): EtymologyFamilyNode? = withContext(Dispatchers.IO) {
        val cleanHanja = hanjaChar.trim()
        if (cleanHanja.isEmpty()) return@withContext null

        val master = getHanjaMaster(cleanHanja)

        val wordsSql = """
            SELECT DISTINCT l.lexeme_id, l.headword_nfc, l.phonetic_vi, l.origin_type,
                   (SELECT meaning_vi FROM senses s WHERE s.lexeme_id = l.lexeme_id ORDER BY s.display_order ASC LIMIT 1) AS primary_meaning,
                   seg.reading_vi, seg.meaning_vi AS seg_meaning
            FROM lexeme_hanja_segments seg
            JOIN lexemes l ON seg.lexeme_id = l.lexeme_id
            WHERE seg.hanja_char = ?
            ORDER BY l.topik_level ASC, l.lexeme_id ASC
        """.trimIndent()

        val relatedWords = mutableListOf<RelatedWordSummary>()
        var derivedReadingVi = ""
        var derivedMeaningVi = master?.meaningVi ?: ""

        db.rawQuery(wordsSql, arrayOf(cleanHanja)).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val hw = cursor.getString(1)
                val phonetic = if (!cursor.isNull(2)) cursor.getString(2) else null
                val oType = OriginType.fromString(cursor.getString(3))
                val meaning = if (!cursor.isNull(4)) cursor.getString(4) else ""
                val segRead = if (!cursor.isNull(5)) cursor.getString(5) else ""
                val segMean = if (!cursor.isNull(6)) cursor.getString(6) else ""

                if (derivedReadingVi.isEmpty() && segRead.isNotEmpty()) {
                    derivedReadingVi = segRead
                }
                if (derivedMeaningVi.isEmpty() && segMean.isNotEmpty()) {
                    derivedMeaningVi = segMean
                }

                relatedWords.add(
                    RelatedWordSummary(
                        lexemeId = id,
                        headword = hw,
                        phoneticVi = phonetic,
                        meaningVi = meaning,
                        originType = oType
                    )
                )
            }
        }

        if (master == null && relatedWords.isEmpty()) {
            return@withContext null
        }

        EtymologyFamilyNode(
            hanjaChar = cleanHanja,
            readingVi = derivedReadingVi,
            meaningVi = derivedMeaningVi,
            radical = master?.radical,
            strokeCount = master?.strokeCount,
            relatedWords = relatedWords
        )
    }

    override fun getEtymologyFamilyFlow(hanjaChar: String): Flow<EtymologyFamilyNode?> = flow {
        emit(getEtymologyFamily(hanjaChar))
    }.flowOn(Dispatchers.IO)

    override suspend fun getHanjaMaster(hanjaChar: String): HanjaMaster? = withContext(Dispatchers.IO) {
        val sql = "SELECT hanja_char, radical, stroke_count, meaning_vi, general_level FROM hanja_master WHERE hanja_char = ? LIMIT 1"
        db.rawQuery(sql, arrayOf(hanjaChar.trim())).use { cursor ->
            if (cursor.moveToFirst()) {
                HanjaMaster(
                    hanjaChar = cursor.getString(0),
                    radical = if (!cursor.isNull(1)) cursor.getString(1) else null,
                    strokeCount = if (!cursor.isNull(2)) cursor.getInt(2) else null,
                    meaningVi = if (!cursor.isNull(3)) cursor.getString(3) else null,
                    generalLevel = if (!cursor.isNull(4)) cursor.getString(4) else null
                )
            } else {
                null
            }
        }
    }
}
