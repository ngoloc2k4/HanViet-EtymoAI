package com.hanviet.etymoai.data.db.workspace.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hanviet.etymoai.data.db.workspace.entity.AiCacheEntity

@Dao
interface AiCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: AiCacheEntity): Long

    @Query("SELECT * FROM ai_cache WHERE prompt_hash = :promptHash LIMIT 1")
    suspend fun getByHash(promptHash: String): AiCacheEntity?

    @Query("DELETE FROM ai_cache WHERE expiry_time IS NOT NULL AND expiry_time < :now")
    suspend fun deleteExpired(now: Long = System.currentTimeMillis()): Int

    @Query("DELETE FROM ai_cache")
    suspend fun clearAll(): Int
}
