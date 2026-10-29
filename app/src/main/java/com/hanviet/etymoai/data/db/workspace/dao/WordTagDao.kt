package com.hanviet.etymoai.data.db.workspace.dao

import androidx.room.*
import com.hanviet.etymoai.data.db.workspace.entity.WordTagCrossRef
import com.hanviet.etymoai.data.db.workspace.entity.WordTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordTagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: WordTagEntity): Long

    @Update
    suspend fun updateTag(tag: WordTagEntity): Int

    @Delete
    suspend fun deleteTag(tag: WordTagEntity): Int

    @Query("SELECT * FROM word_tags ORDER BY tag_name ASC")
    fun getAllTagsFlow(): Flow<List<WordTagEntity>>

    @Query("SELECT * FROM word_tags ORDER BY tag_name ASC")
    suspend fun getAllTagsSync(): List<WordTagEntity>

    @Query("SELECT * FROM word_tags WHERE tag_name = :name LIMIT 1")
    suspend fun getTagByName(name: String): WordTagEntity?

    @Query("SELECT * FROM word_tags WHERE tag_id = :id LIMIT 1")
    suspend fun getTagById(id: Long): WordTagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: WordTagCrossRef): Long

    @Delete
    suspend fun deleteCrossRef(crossRef: WordTagCrossRef): Int

    @Query("DELETE FROM word_tag_cross_ref WHERE word_id = :wordId AND tag_id = :tagId")
    suspend fun removeTagFromWord(wordId: Long, tagId: Long): Int

    @Query("DELETE FROM word_tag_cross_ref WHERE word_id = :wordId")
    suspend fun removeAllTagsFromWord(wordId: Long): Int

    @Query("SELECT * FROM word_tag_cross_ref")
    suspend fun getAllCrossRefsSync(): List<WordTagCrossRef>
}
