package com.hanviet.etymoai.data.db.workspace

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hanviet.etymoai.data.db.workspace.dao.AiCacheDao
import com.hanviet.etymoai.data.db.workspace.dao.SavedWordDao
import com.hanviet.etymoai.data.db.workspace.dao.SearchHistoryDao
import com.hanviet.etymoai.data.db.workspace.entity.AiCacheEntity
import com.hanviet.etymoai.data.db.workspace.entity.SavedWordEntity
import com.hanviet.etymoai.data.db.workspace.entity.SearchHistoryEntity

/**
 * WorkspaceDatabase: Cơ sở dữ liệu Room có thể GHI (Writable) dành riêng cho người dùng.
 * Tách biệt hoàn toàn với dictionary_core.db để bảo vệ dữ liệu cá nhân (từ lưu, ghi chú, flashcard, lịch sử).
 */
@Database(
    entities = [
        SavedWordEntity::class,
        SearchHistoryEntity::class,
        AiCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WorkspaceDatabase : RoomDatabase() {

    abstract fun savedWordDao(): SavedWordDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun aiCacheDao(): AiCacheDao

    companion object {
        const val DB_NAME = "user_workspace.db"

        @Volatile
        private var instance: WorkspaceDatabase? = null

        fun getInstance(context: Context): WorkspaceDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): WorkspaceDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                WorkspaceDatabase::class.java,
                DB_NAME
            )
            .fallbackToDestructiveMigration()
            .build()
        }

        /**
         * Khởi tạo database trong bộ nhớ (In-Memory) chuyên dùng cho Unit Test.
         */
        fun createInMemory(context: Context): WorkspaceDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                WorkspaceDatabase::class.java
            )
            .allowMainThreadQueries()
            .build()
        }
    }
}
