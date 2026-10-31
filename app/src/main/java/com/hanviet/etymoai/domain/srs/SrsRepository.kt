package com.hanviet.etymoai.domain.srs

import com.hanviet.etymoai.data.db.workspace.WorkspaceDatabase
import com.hanviet.etymoai.data.db.workspace.entity.SrsCardEntity
import com.hanviet.etymoai.domain.etymology.EtymologyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class LeechWordData(
    val rank: Int,
    val headword: String,
    val posLabel: String,
    val lastReviewedDaysAgo: String,
    val lapsesCount: Int
)

class SrsRepository(
    private val db: WorkspaceDatabase,
    private val etymologyRepository: EtymologyRepository? = null
) {
    private val srsCardDao = db.srsCardDao()
    private val savedWordDao = db.savedWordDao()

    fun getDueCardsFlow(currentTimestamp: Long = System.currentTimeMillis()): Flow<List<SrsFlashcardData>> {
        return srsCardDao.getDueCardsFlow(currentTimestamp).map { entities ->
            entities.mapNotNull { entity ->
                val word = savedWordDao.getById(entity.savedWordId) ?: return@mapNotNull null
                val segments = etymologyRepository?.getEtymologyForWordByHeadword(word.headwordSnapshot)?.segments ?: emptyList()
                SrsFlashcardData(
                    card = SrsCard.fromEntity(entity),
                    word = word,
                    hanjaSegments = segments
                )
            }
        }
    }

    suspend fun getDueCardsSync(currentTimestamp: Long = System.currentTimeMillis()): List<SrsFlashcardData> {
        val entities = srsCardDao.getDueCardsSync(currentTimestamp)
        return entities.mapNotNull { entity ->
            val word = savedWordDao.getById(entity.savedWordId) ?: return@mapNotNull null
            val segments = etymologyRepository?.getEtymologyForWordByHeadword(word.headwordSnapshot)?.segments ?: emptyList()
            SrsFlashcardData(
                card = SrsCard.fromEntity(entity),
                word = word,
                hanjaSegments = segments
            )
        }
    }

    suspend fun createCardForSavedWord(savedWordId: Long): Long {
        val existing = srsCardDao.getCardBySavedWordId(savedWordId)
        if (existing != null) {
            return existing.cardId
        }
        val card = SrsCard(
            savedWordId = savedWordId,
            repetitions = 0,
            intervalDays = 0,
            easeFactor = SuperMemo2Engine.INITIAL_EASE_FACTOR,
            dueTimestamp = System.currentTimeMillis()
        )
        return srsCardDao.insertCard(card.toEntity())
    }

    suspend fun enrollAllSavedWords() {
        val allSaved = savedWordDao.getAllSync()
        for (word in allSaved) {
            val existing = srsCardDao.getCardBySavedWordId(word.id)
            if (existing == null) {
                val card = SrsCard(
                    savedWordId = word.id,
                    repetitions = 0,
                    intervalDays = 0,
                    easeFactor = SuperMemo2Engine.INITIAL_EASE_FACTOR,
                    dueTimestamp = System.currentTimeMillis()
                )
                srsCardDao.insertCard(card.toEntity())
            }
        }
    }

    suspend fun getAllCardsSync(): List<SrsCardEntity> = srsCardDao.getAllCardsSync()

    fun getAllCardsFlow(): Flow<List<SrsCardEntity>> = srsCardDao.getAllCardsFlow()

    fun getLeechCardsFlow(limit: Int = 10): Flow<List<LeechWordData>> {
        return srsCardDao.getLeechCardsFlow(limit).map { list ->
            list.mapIndexedNotNull { index, card ->
                val word = savedWordDao.getById(card.savedWordId) ?: return@mapIndexedNotNull null
                val daysAgo = if (card.lastReviewedTimestamp != null) {
                    ((System.currentTimeMillis() - card.lastReviewedTimestamp) / (24 * 3600 * 1000L)).coerceAtLeast(1)
                } else {
                    1L
                }
                LeechWordData(
                    rank = index + 1,
                    headword = word.headwordSnapshot,
                    posLabel = word.originType,
                    lastReviewedDaysAgo = "$daysAgo ngày",
                    lapsesCount = card.lapses
                )
            }
        }
    }

    suspend fun processReview(
        card: SrsCard,
        rating: ReviewRating,
        reviewTimestamp: Long = System.currentTimeMillis()
    ): SrsCard {
        val nextCard = SuperMemo2Engine.calculateNextState(card, rating, reviewTimestamp)
        srsCardDao.updateCard(nextCard.toEntity())
        return nextCard
    }

    suspend fun countDueCards(currentTimestamp: Long = System.currentTimeMillis()): Int {
        return srsCardDao.countDueCards(currentTimestamp)
    }
}
