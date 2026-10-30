package com.hanviet.etymoai.data.repository

import com.hanviet.etymoai.data.db.workspace.WorkspaceDatabase
import com.hanviet.etymoai.data.db.workspace.entity.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

/**
 * UserWorkspaceRepository: Quản trị toàn bộ dữ liệu cá nhân hóa của người học.
 * Độc lập với dictionary_core.db, đảm bảo an toàn dữ liệu và hỗ trợ Backup/Restore JSON.
 */
class UserWorkspaceRepository(
    private val db: WorkspaceDatabase
) {
    private val savedWordDao = db.savedWordDao()
    private val wordTagDao = db.wordTagDao()
    private val personalNoteDao = db.personalNoteDao()
    private val searchHistoryDao = db.searchHistoryDao()

    fun getSearchHistoryFlow(limit: Int = 10): Flow<List<SearchHistoryEntity>> {
        return searchHistoryDao.getRecentFlow(limit)
    }

    suspend fun getSearchHistorySync(limit: Int = 10): List<SearchHistoryEntity> {
        return searchHistoryDao.getRecentSync(limit)
    }

    suspend fun recordSearch(query: String, resultCount: Int = 1) {
        val clean = query.trim()
        if (clean.isBlank()) return
        searchHistoryDao.insert(
            SearchHistoryEntity(
                queryText = clean,
                searchedAt = System.currentTimeMillis(),
                resultCount = resultCount
            )
        )
    }

    suspend fun clearSearchHistory(): Int {
        return searchHistoryDao.clearHistory()
    }

    suspend fun getSavedWordsCount(): Int {
        return savedWordDao.getCount()
    }

    enum class ConflictStrategy {
        MERGE,
        OVERWRITE
    }

    data class ImportResult(
        val wordsImported: Int,
        val tagsImported: Int,
        val notesImported: Int,
        val success: Boolean,
        val message: String
    )

    fun getAllSavedWordsFlow(): Flow<List<SavedWordWithTags>> {
        return savedWordDao.getAllWithTagsFlow()
    }

    suspend fun getAllSavedWordsSync(): List<SavedWordWithTags> {
        return savedWordDao.getAllWithTagsSync()
    }

    suspend fun getSavedWordById(id: Long): SavedWordWithTags? {
        return savedWordDao.getWithTagsById(id)
    }

    suspend fun getSavedWordByStableKey(stableKey: String): SavedWordEntity? {
        return savedWordDao.getByStableKey(stableKey)
    }

    suspend fun saveWord(
        stableKey: String,
        headword: String,
        meaning: String,
        phonetic: String? = null,
        originType: String = "SINO_KOREAN",
        isFavorite: Boolean = false,
        tagNames: List<String> = emptyList(),
        note: String? = null
    ): Long {
        val existing = savedWordDao.getByStableKey(stableKey)
        val wordId = if (existing != null) {
            val updated = existing.copy(
                headwordSnapshot = headword,
                meaningSnapshot = meaning,
                phoneticSnapshot = phonetic,
                originType = originType,
                isFavorite = isFavorite || existing.isFavorite,
                updatedAt = System.currentTimeMillis()
            )
            savedWordDao.update(updated)
            existing.id
        } else {
            val entity = SavedWordEntity(
                stableKey = stableKey,
                headwordSnapshot = headword,
                meaningSnapshot = meaning,
                phoneticSnapshot = phonetic,
                originType = originType,
                isFavorite = isFavorite,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            savedWordDao.insert(entity)
        }

        // Gắn các nhãn (tags)
        for (tagName in tagNames) {
            if (tagName.isNotBlank()) {
                addTagToWord(wordId, tagName.trim())
            }
        }

        // Thêm ghi chú nếu có
        if (!note.isNullOrBlank()) {
            addNote(wordId, note.trim())
        }

        return wordId
    }

    suspend fun removeWord(wordId: Long): Boolean {
        return savedWordDao.deleteById(wordId) > 0
    }

    suspend fun toggleFavorite(wordId: Long): Boolean {
        val current = savedWordDao.getById(wordId) ?: return false
        val newStatus = !current.isFavorite
        savedWordDao.updateFavorite(wordId, newStatus)
        return newStatus
    }

    fun getAllTagsFlow(): Flow<List<WordTagEntity>> {
        return wordTagDao.getAllTagsFlow()
    }

    suspend fun addTagToWord(wordId: Long, tagName: String, tagColorHex: String = "#1976D2"): Long {
        val cleanName = tagName.trim()
        val tagId = wordTagDao.getTagByName(cleanName)?.tagId ?: run {
            wordTagDao.insertTag(
                WordTagEntity(
                    tagName = cleanName,
                    tagColorHex = tagColorHex
                )
            )
        }
        wordTagDao.insertCrossRef(WordTagCrossRef(wordId = wordId, tagId = tagId))
        return tagId
    }

    suspend fun removeTagFromWord(wordId: Long, tagId: Long): Boolean {
        return wordTagDao.removeTagFromWord(wordId, tagId) > 0
    }

    suspend fun addNote(wordId: Long, markdownContent: String): Long {
        return personalNoteDao.insertNote(
            PersonalNoteEntity(
                savedWordId = wordId,
                markdownContent = markdownContent
            )
        )
    }

    suspend fun updateNote(noteId: Long, markdownContent: String): Boolean {
        val existing = personalNoteDao.getNoteById(noteId) ?: return false
        val updated = existing.copy(
            markdownContent = markdownContent,
            updatedAt = System.currentTimeMillis()
        )
        return personalNoteDao.updateNote(updated) > 0
    }

    suspend fun deleteNote(noteId: Long): Boolean {
        return personalNoteDao.deleteById(noteId) > 0
    }

    suspend fun getWordCount(): Int {
        return savedWordDao.getCount()
    }

    /**
     * Xuất toàn bộ sổ tay từ vựng sang chuỗi JSON chuẩn hóa (v1).
     */
    suspend fun exportWorkspaceToJson(): String {
        val wordsWithTags = savedWordDao.getAllWithTagsSync()
        val root = JSONObject()
        root.put("schema_version", 1)
        root.put("exported_at", System.currentTimeMillis())

        val wordsArray = JSONArray()
        for (item in wordsWithTags) {
            val wordObj = JSONObject()
            wordObj.put("stable_key", item.word.stableKey)
            wordObj.put("headword", item.word.headwordSnapshot)
            wordObj.put("meaning", item.word.meaningSnapshot)
            wordObj.put("phonetic", item.word.phoneticSnapshot ?: "")
            wordObj.put("origin_type", item.word.originType)
            wordObj.put("is_favorite", item.word.isFavorite)
            wordObj.put("created_at", item.word.createdAt)
            wordObj.put("updated_at", item.word.updatedAt)

            val tagsArray = JSONArray()
            for (tag in item.tags) {
                val tagObj = JSONObject()
                tagObj.put("name", tag.tagName)
                tagObj.put("color", tag.tagColorHex)
                tagsArray.put(tagObj)
            }
            wordObj.put("tags", tagsArray)

            val notesArray = JSONArray()
            for (note in item.notes) {
                val noteObj = JSONObject()
                noteObj.put("content", note.markdownContent)
                noteObj.put("created_at", note.createdAt)
                noteObj.put("updated_at", note.updatedAt)
                notesArray.put(noteObj)
            }
            wordObj.put("notes", notesArray)

            wordsArray.put(wordObj)
        }
        root.put("saved_words", wordsArray)

        return root.toString(2)
    }

    /**
     * Nạp lại dữ liệu sổ tay từ chuỗi JSON sao lưu với chiến lược xử lý xung đột (Merge hoặc Overwrite).
     */
    suspend fun importWorkspaceFromJson(
        jsonString: String,
        strategy: ConflictStrategy = ConflictStrategy.MERGE
    ): ImportResult {
        return try {
            val root = JSONObject(jsonString)
            val schemaVersion = root.optInt("schema_version", 1)
            if (schemaVersion > 1) {
                return ImportResult(
                    wordsImported = 0,
                    tagsImported = 0,
                    notesImported = 0,
                    success = false,
                    message = "Phiên bản sao lưu không được hỗ trợ (version $schemaVersion)"
                )
            }

            if (strategy == ConflictStrategy.OVERWRITE) {
                savedWordDao.clearAll()
            }

            val wordsArray = root.optJSONArray("saved_words") ?: JSONArray()
            var wordsCount = 0
            var tagsCount = 0
            var notesCount = 0

            for (i in 0 until wordsArray.length()) {
                val wordObj = wordsArray.getJSONObject(i)
                val stableKey = wordObj.getString("stable_key")
                val headword = wordObj.getString("headword")
                val meaning = wordObj.getString("meaning")
                val phonetic = wordObj.optString("phonetic").takeIf { it.isNotBlank() }
                val originType = wordObj.optString("origin_type", "SINO_KOREAN")
                val isFavorite = wordObj.optBoolean("is_favorite", false)
                val createdAt = wordObj.optLong("created_at", System.currentTimeMillis())
                val updatedAt = wordObj.optLong("updated_at", System.currentTimeMillis())

                val existing = savedWordDao.getByStableKey(stableKey)
                val wordId = if (existing != null) {
                    if (strategy == ConflictStrategy.OVERWRITE) {
                        val replaced = existing.copy(
                            headwordSnapshot = headword,
                            meaningSnapshot = meaning,
                            phoneticSnapshot = phonetic,
                            originType = originType,
                            isFavorite = isFavorite,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                        savedWordDao.update(replaced)
                        existing.id
                    } else {
                        existing.id
                    }
                } else {
                    savedWordDao.insert(
                        SavedWordEntity(
                            stableKey = stableKey,
                            headwordSnapshot = headword,
                            meaningSnapshot = meaning,
                            phoneticSnapshot = phonetic,
                            originType = originType,
                            isFavorite = isFavorite,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                    )
                }
                wordsCount++

                val tagsArray = wordObj.optJSONArray("tags") ?: JSONArray()
                for (j in 0 until tagsArray.length()) {
                    val tagObj = tagsArray.getJSONObject(j)
                    val tagName = tagObj.getString("name")
                    val tagColor = tagObj.optString("color", "#1976D2")
                    addTagToWord(wordId, tagName, tagColor)
                    tagsCount++
                }

                val notesArray = wordObj.optJSONArray("notes") ?: JSONArray()
                for (k in 0 until notesArray.length()) {
                    val noteObj = notesArray.getJSONObject(k)
                    val content = noteObj.getString("content")
                    val noteCreated = noteObj.optLong("created_at", System.currentTimeMillis())
                    val noteUpdated = noteObj.optLong("updated_at", System.currentTimeMillis())
                    personalNoteDao.insertNote(
                        PersonalNoteEntity(
                            savedWordId = wordId,
                            markdownContent = content,
                            createdAt = noteCreated,
                            updatedAt = noteUpdated
                        )
                    )
                    notesCount++
                }
            }

            ImportResult(
                wordsImported = wordsCount,
                tagsImported = tagsCount,
                notesImported = notesCount,
                success = true,
                message = "Khôi phục thành công $wordsCount từ vựng, $tagsCount nhãn và $notesCount ghi chú."
            )
        } catch (e: Exception) {
            ImportResult(
                wordsImported = 0,
                tagsImported = 0,
                notesImported = 0,
                success = false,
                message = "Lỗi đọc file sao lưu: ${e.message}"
            )
        }
    }
}
