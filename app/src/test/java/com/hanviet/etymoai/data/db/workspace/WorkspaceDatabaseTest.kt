package com.hanviet.etymoai.data.db.workspace

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hanviet.etymoai.data.db.workspace.entity.AiCacheEntity
import com.hanviet.etymoai.data.db.workspace.entity.SavedWordEntity
import com.hanviet.etymoai.data.db.workspace.entity.SearchHistoryEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WorkspaceDatabaseTest {

    private lateinit var db: WorkspaceDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = WorkspaceDatabase.createInMemory(context)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testSavedWordDao_insertAndReadBack() = runBlocking {
        val dao = db.savedWordDao()

        val word = SavedWordEntity(
            stableKey = "ko_gibun_01",
            headwordSnapshot = "기분",
            meaningSnapshot = "tâm trạng, cảm xúc",
            phoneticSnapshot = "ki-bun",
            personalNotes = "Từ rất quan trọng trong đời sống hàng ngày",
            tags = "TOPIK 1, Cảm xúc",
            isFavorite = true
        )

        val insertedId = dao.insert(word)
        assertTrue("ID sinh ra phải lớn hơn 0", insertedId > 0)

        val retrieved = dao.getByStableKey("ko_gibun_01")
        assertNotNull("Bản ghi đã lưu phải đọc ra được", retrieved)
        assertEquals("기분", retrieved?.headwordSnapshot)
        assertEquals("tâm trạng, cảm xúc", retrieved?.meaningSnapshot)
        assertEquals(true, retrieved?.isFavorite)

        // Cập nhật ghi chú
        val updated = retrieved!!.copy(personalNotes = "Đã cập nhật ghi chú mới")
        dao.update(updated)

        val retrievedUpdated = dao.getById(insertedId)
        assertEquals("Đã cập nhật ghi chú mới", retrievedUpdated?.personalNotes)

        // Xóa
        val deletedCount = dao.delete(retrievedUpdated!!)
        assertEquals(1, deletedCount)
        assertEquals(0, dao.getCount())
    }

    @Test
    fun testSearchHistoryDao_recordAndCount() = runBlocking {
        val dao = db.searchHistoryDao()

        dao.insert(SearchHistoryEntity(queryText = "기분", resultCount = 1))
        dao.insert(SearchHistoryEntity(queryText = "학생", resultCount = 1))

        assertEquals(2, dao.getCount())
        val recent = dao.getRecentSync(10)
        assertEquals(2, recent.size)
        assertEquals("학생", recent[0].queryText)

        dao.clearHistory()
        assertEquals(0, dao.getCount())
    }

    @Test
    fun testAiCacheDao_cachingAndExpiry() = runBlocking {
        val dao = db.aiCacheDao()

        val cache = AiCacheEntity(
            promptHash = "hash_gibun_context",
            responseJson = "{\"explanation\": \"Khí phân: khí sắc phân định\"}",
            modelId = "gemini-1.5-flash",
            expiryTime = System.currentTimeMillis() - 1000 // Đã hết hạn
        )

        dao.insert(cache)
        assertNotNull(dao.getByHash("hash_gibun_context"))

        val deleted = dao.deleteExpired(System.currentTimeMillis())
        assertEquals(1, deleted)
    }
}
