package com.hanviet.etymoai.data.db.workspace.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hanviet.etymoai.data.db.workspace.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SearchHistoryEntity): Long

    @Query("SELECT * FROM search_history ORDER BY searched_at DESC LIMIT :limit")
    fun getRecentFlow(limit: Int = 30): Flow<List<SearchHistoryEntity>>

    @Query("SELECT * FROM search_history ORDER BY searched_at DESC LIMIT :limit")
    suspend fun getRecentSync(limit: Int = 30): List<SearchHistoryEntity>

    @Query("DELETE FROM search_history")
    suspend fun clearHistory(): Int

    @Query("SELECT COUNT(*) FROM search_history")
    suspend fun getCount(): Int
}
