package com.hanviet.etymoai.data.db.workspace.dao

import androidx.room.*
import com.hanviet.etymoai.data.db.workspace.entity.SrsCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SrsCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: SrsCardEntity): Long

    @Update
    suspend fun updateCard(card: SrsCardEntity): Int

    @Delete
    suspend fun deleteCard(card: SrsCardEntity): Int

    @Query("SELECT * FROM srs_cards WHERE card_id = :cardId LIMIT 1")
    suspend fun getCardById(cardId: Long): SrsCardEntity?

    @Query("SELECT * FROM srs_cards WHERE saved_word_id = :savedWordId LIMIT 1")
    suspend fun getCardBySavedWordId(savedWordId: Long): SrsCardEntity?

    @Query("SELECT * FROM srs_cards WHERE due_timestamp <= :currentTimestamp ORDER BY due_timestamp ASC")
    fun getDueCardsFlow(currentTimestamp: Long): Flow<List<SrsCardEntity>>

    @Query("SELECT * FROM srs_cards WHERE due_timestamp <= :currentTimestamp ORDER BY due_timestamp ASC")
    suspend fun getDueCardsSync(currentTimestamp: Long): List<SrsCardEntity>

    @Query("SELECT COUNT(*) FROM srs_cards WHERE due_timestamp <= :currentTimestamp")
    suspend fun countDueCards(currentTimestamp: Long): Int

    @Query("SELECT * FROM srs_cards ORDER BY due_timestamp ASC")
    fun getAllCardsFlow(): Flow<List<SrsCardEntity>>

    @Query("SELECT * FROM srs_cards ORDER BY due_timestamp ASC")
    suspend fun getAllCardsSync(): List<SrsCardEntity>
}
