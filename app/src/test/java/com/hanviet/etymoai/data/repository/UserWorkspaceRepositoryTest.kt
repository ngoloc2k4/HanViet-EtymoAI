package com.hanviet.etymoai.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hanviet.etymoai.data.db.workspace.WorkspaceDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserWorkspaceRepositoryTest {

    private lateinit var db: WorkspaceDatabase
    private lateinit var repository: UserWorkspaceRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = WorkspaceDatabase.createInMemory(context)
        repository = UserWorkspaceRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testSaveWordWithTagsAndNote() = runBlocking {
        val wordId = repository.saveWord(
            stableKey = "ko_haksaeng_1",
            headword = "학생",
            meaning = "Học sinh",
            phonetic = "hác-xeng",
            originType = "SINO_KOREAN",
            isFavorite = true,
            tagNames = listOf("TOPIK I", "Trường học"),
            note = "Từ ghép Hán-Hàn: Học Sinh (學生)"
        )
        assertTrue(wordId > 0)

        val retrieved = repository.getSavedWordById(wordId)
        assertNotNull(retrieved)
        assertEquals("학생", retrieved!!.word.headwordSnapshot)
        assertEquals("Học sinh", retrieved.word.meaningSnapshot)
        assertEquals("SINO_KOREAN", retrieved.word.originType)
        assertTrue(retrieved.word.isFavorite)

        // Kiểm tra nhãn (tags)
        assertEquals(2, retrieved.tags.size)
        val tagNames = retrieved.tags.map { it.tagName }
        assertTrue(tagNames.contains("TOPIK I"))
        assertTrue(tagNames.contains("Trường học"))

        // Kiểm tra ghi chú (notes)
        assertEquals(1, retrieved.notes.size)
        assertTrue(retrieved.notes[0].markdownContent.contains("學生"))
    }

    @Test
    fun testDeleteWord_cascades() = runBlocking {
        val wordId = repository.saveWord(
            stableKey = "ko_hakgyo_1",
            headword = "학교",
            meaning = "Trường học",
            tagNames = listOf("Địa điểm"),
            note = "Học Hiệu (學校)"
        )
        assertEquals(1, repository.getWordCount())

        val deleted = repository.removeWord(wordId)
        assertTrue(deleted)
        assertEquals(0, repository.getWordCount())

        val retrieved = repository.getSavedWordById(wordId)
        assertNull(retrieved)

        val notes = db.personalNoteDao().getNotesByWordIdSync(wordId)
        assertTrue(notes.isEmpty())
    }

    @Test
    fun testToggleFavorite() = runBlocking {
        val wordId = repository.saveWord(
            stableKey = "ko_gibun_1",
            headword = "기분",
            meaning = "Tâm trạng, khí sắc",
            isFavorite = false
        )

        val firstToggle = repository.toggleFavorite(wordId)
        assertTrue("Sau toggle lần 1 phải là true", firstToggle)

        val savedWord1 = repository.getSavedWordById(wordId)
        assertTrue(savedWord1!!.word.isFavorite)

        val secondToggle = repository.toggleFavorite(wordId)
        assertFalse("Sau toggle lần 2 phải là false", secondToggle)

        val savedWord2 = repository.getSavedWordById(wordId)
        assertFalse(savedWord2!!.word.isFavorite)
    }

    @Test
    fun testExportAndImportJson_100PercentFidelity() = runBlocking {
        repository.saveWord(
            stableKey = "ko_haksaeng_1",
            headword = "학생",
            meaning = "Học sinh",
            phonetic = "hác-xeng",
            originType = "SINO_KOREAN",
            isFavorite = false,
            tagNames = listOf("TOPIK I"),
            note = "Học Sinh (學生)"
        )

        repository.saveWord(
            stableKey = "ko_hakgyo_1",
            headword = "학교",
            meaning = "Trường học",
            phonetic = "hác-ki-ô",
            originType = "SINO_KOREAN",
            isFavorite = true,
            tagNames = listOf("Cơ sở giáo dục"),
            note = "Học Hiệu (學校)"
        )

        assertEquals(2, repository.getWordCount())

        // Xuất file JSON
        val jsonBackup = repository.exportWorkspaceToJson()
        assertNotNull(jsonBackup)
        assertTrue(jsonBackup.contains("\"schema_version\": 1"))
        assertTrue(jsonBackup.contains("학생"))
        assertTrue(jsonBackup.contains("학교"))
        assertTrue(jsonBackup.contains("Học Sinh (學生)"))

        // Xóa sạch dữ liệu trong database
        db.savedWordDao().clearAll()
        assertEquals(0, repository.getWordCount())

        // Nạp lại dữ liệu từ JSON
        val importResult = repository.importWorkspaceFromJson(jsonBackup, UserWorkspaceRepository.ConflictStrategy.OVERWRITE)
        assertTrue(importResult.success)
        assertEquals(2, importResult.wordsImported)
        assertEquals(2, repository.getWordCount())

        val wordsList = repository.getAllSavedWordsFlow().first()
        assertEquals(2, wordsList.size)

        val restoredHaksaeng = wordsList.find { it.word.headwordSnapshot == "학생" }
        assertNotNull(restoredHaksaeng)
        assertEquals("Học sinh", restoredHaksaeng!!.word.meaningSnapshot)
        assertEquals("TOPIK I", restoredHaksaeng.tags[0].tagName)
        assertEquals("Học Sinh (學生)", restoredHaksaeng.notes[0].markdownContent)

        val restoredHakgyo = wordsList.find { it.word.headwordSnapshot == "학교" }
        assertNotNull(restoredHakgyo)
        assertTrue(restoredHakgyo!!.word.isFavorite)
        assertEquals("Cơ sở giáo dục", restoredHakgyo.tags[0].tagName)
    }
}
