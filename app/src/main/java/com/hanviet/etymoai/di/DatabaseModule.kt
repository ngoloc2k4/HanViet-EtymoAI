package com.hanviet.etymoai.di

import android.content.Context
import com.hanviet.etymoai.data.db.core.CoreDatabaseHelper
import com.hanviet.etymoai.data.db.workspace.WorkspaceDatabase
import com.hanviet.etymoai.data.db.workspace.dao.AiCacheDao
import com.hanviet.etymoai.data.db.workspace.dao.SavedWordDao
import com.hanviet.etymoai.data.db.workspace.dao.SearchHistoryDao
import com.hanviet.etymoai.domain.etymology.DefaultEtymologyRepository
import com.hanviet.etymoai.domain.etymology.EtymologyRepository

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

    /**
     * Cung cấp WordTagDao
     */
    fun provideWordTagDao(database: WorkspaceDatabase): com.hanviet.etymoai.data.db.workspace.dao.WordTagDao {
        return database.wordTagDao()
    }

    /**
     * Cung cấp PersonalNoteDao
     */
    fun providePersonalNoteDao(database: WorkspaceDatabase): com.hanviet.etymoai.data.db.workspace.dao.PersonalNoteDao {
        return database.personalNoteDao()
    }

    /**
     * Cung cấp SrsCardDao
     */
    fun provideSrsCardDao(database: WorkspaceDatabase): com.hanviet.etymoai.data.db.workspace.dao.SrsCardDao {
        return database.srsCardDao()
    }

    /**
     * Cung cấp UserWorkspaceRepository
     */
    fun provideUserWorkspaceRepository(database: WorkspaceDatabase): com.hanviet.etymoai.data.repository.UserWorkspaceRepository {
        return com.hanviet.etymoai.data.repository.UserWorkspaceRepository(database)
    }

    /**
     * Cung cấp SrsRepository
     */
    fun provideSrsRepository(
        database: WorkspaceDatabase,
        etymologyRepository: EtymologyRepository? = null
    ): com.hanviet.etymoai.domain.srs.SrsRepository {
        return com.hanviet.etymoai.domain.srs.SrsRepository(database, etymologyRepository)
    }

    /**
     * Cung cấp EtymologyRepository (Truy vấn từ nguyên và chiết tự Hán tự)
     */
    fun provideEtymologyRepository(helper: CoreDatabaseHelper): EtymologyRepository {
        return DefaultEtymologyRepository(helper)
    }

    /**
     * Cung cấp SearchPipeline
     */
    fun provideSearchPipeline(helper: CoreDatabaseHelper): com.hanviet.etymoai.domain.search.SearchPipeline {
        return com.hanviet.etymoai.domain.search.SearchPipeline(helper)
    }

    /**
     * Cung cấp ApiKeyStorage (BYOK Encrypted SharedPreferences)
     */
    fun provideApiKeyStorage(context: Context): com.hanviet.etymoai.ai.security.ApiKeyStorage {
        return com.hanviet.etymoai.ai.security.SecureApiKeyStorage.create(context)
    }

    /**
     * Cung cấp GroundingGuard (Bộ lọc chống ảo giác Hán tự kèm AiCache)
     */
    fun provideGroundingGuard(database: WorkspaceDatabase): com.hanviet.etymoai.ai.grounding.GroundingGuard {
        return com.hanviet.etymoai.ai.grounding.GroundingGuard(database.aiCacheDao())
    }

    /**
     * Cung cấp AiProvider động theo cấu hình người dùng
     */
    fun provideAiProvider(
        providerType: com.hanviet.etymoai.ai.provider.AiProviderType,
        apiKeyStorage: com.hanviet.etymoai.ai.security.ApiKeyStorage
    ): com.hanviet.etymoai.ai.provider.AiProvider {
        return when (providerType) {
            com.hanviet.etymoai.ai.provider.AiProviderType.GEMINI ->
                com.hanviet.etymoai.ai.provider.GeminiClient(
                    apiKeyProvider = { apiKeyStorage.getApiKey(com.hanviet.etymoai.ai.provider.AiProviderType.GEMINI) }
                )
            else ->
                com.hanviet.etymoai.ai.provider.OpenAiCompatibleClient(
                    providerType = providerType,
                    apiKeyProvider = { apiKeyStorage.getApiKey(providerType) }
                )
        }
    }
}
