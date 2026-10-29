package com.hanviet.etymoai.di

import android.content.Context
import com.hanviet.etymoai.data.db.core.CoreDatabaseHelper
import com.hanviet.etymoai.data.db.workspace.WorkspaceDatabase
import com.hanviet.etymoai.data.db.workspace.dao.AiCacheDao
import com.hanviet.etymoai.data.db.workspace.dao.SavedWordDao
import com.hanviet.etymoai.data.db.workspace.dao.SearchHistoryDao

/**
 * DatabaseModule: Thiết lập DI phân tách rõ ràng hai luồng cơ sở dữ liệu:
 * 1. Core Stream: Read-Only SQLite Core Database (dictionary_core.db)
 * 2. Workspace Stream: Writable Room User Workspace Database (user_workspace.db)
 */
object DatabaseModule {

    /**
     * Cung cấp CoreDatabaseHelper (Read-Only)
     */
    fun provideCoreDatabaseHelper(context: Context): CoreDatabaseHelper {
        return CoreDatabaseHelper.getInstance(context)
    }

    /**
     * Cung cấp WorkspaceDatabase (Writable Room)
     */
    fun provideWorkspaceDatabase(context: Context): WorkspaceDatabase {
        return WorkspaceDatabase.getInstance(context)
    }

    /**
     * Cung cấp SavedWordDao
     */
    fun provideSavedWordDao(database: WorkspaceDatabase): SavedWordDao {
        return database.savedWordDao()
    }

    /**
     * Cung cấp SearchHistoryDao
     */
    fun provideSearchHistoryDao(database: WorkspaceDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }

    /**
     * Cung cấp AiCacheDao
     */
    fun provideAiCacheDao(database: WorkspaceDatabase): AiCacheDao {
        return database.aiCacheDao()
    }
}
