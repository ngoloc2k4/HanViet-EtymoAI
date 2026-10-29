package com.hanviet.etymoai.data.db.workspace.dao

import androidx.room.*
import com.hanviet.etymoai.data.db.workspace.entity.SavedWordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedWordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: SavedWordEntity): Long

    @Update
    suspend fun update(word: SavedWordEntity): Int

    @Delete
    suspend fun delete(word: SavedWordEntity): Int

    @Query("DELETE FROM saved_words WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM saved_words ORDER BY created_at DESC")
    fun getAllFlow(): Flow<List<SavedWordEntity>>

    @Query("SELECT * FROM saved_words ORDER BY created_at DESC")
    suspend fun getAllSync(): List<SavedWordEntity>

    @Query("SELECT * FROM saved_words WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SavedWordEntity?

    @Query("SELECT * FROM saved_words WHERE stable_key = :stableKey LIMIT 1")
    suspend fun getByStableKey(stableKey: String): SavedWordEntity?

    @Query("SELECT COUNT(*) FROM saved_words")
    suspend fun getCount(): Int
}
