package com.hanviet.etymoai.data.db.workspace.dao

import androidx.room.*
import com.hanviet.etymoai.data.db.workspace.entity.PersonalNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalNoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: PersonalNoteEntity): Long

    @Update
    suspend fun updateNote(note: PersonalNoteEntity): Int

    @Delete
    suspend fun deleteNote(note: PersonalNoteEntity): Int

    @Query("DELETE FROM personal_notes WHERE note_id = :noteId")
    suspend fun deleteById(noteId: Long): Int

    @Query("SELECT * FROM personal_notes WHERE saved_word_id = :wordId ORDER BY updated_at DESC")
    fun getNotesByWordIdFlow(wordId: Long): Flow<List<PersonalNoteEntity>>

    @Query("SELECT * FROM personal_notes WHERE saved_word_id = :wordId ORDER BY updated_at DESC")
    suspend fun getNotesByWordIdSync(wordId: Long): List<PersonalNoteEntity>

    @Query("SELECT * FROM personal_notes WHERE note_id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: Long): PersonalNoteEntity?

    @Query("SELECT * FROM personal_notes ORDER BY updated_at DESC")
    suspend fun getAllNotesSync(): List<PersonalNoteEntity>
}
