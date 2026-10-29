package com.hanviet.etymoai.domain.srs

import com.hanviet.etymoai.data.db.workspace.WorkspaceDatabase
import com.hanviet.etymoai.domain.etymology.EtymologyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SrsRepository @Inject constructor(
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
