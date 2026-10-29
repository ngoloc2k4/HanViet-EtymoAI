package com.hanviet.etymoai.domain.srs

import com.hanviet.etymoai.data.db.workspace.entity.SavedWordEntity
import com.hanviet.etymoai.data.db.workspace.entity.SrsCardEntity
import com.hanviet.etymoai.domain.etymology.HanjaSegment

/**
 * ReviewRating: 4 mức độ đánh giá chất lượng phản hồi trong thuật toán SuperMemo SM-2.
 */
enum class ReviewRating(val quality: Int, val labelVi: String) {
    AGAIN(0, "Quên"),
    HARD(3, "Khó"),
    GOOD(4, "Tốt"),
    EASY(5, "Dễ")
}

/**
 * SrsCard: Trạng thái thẻ ôn tập ngắt quãng độc lập.
 */
data class SrsCard(
    val cardId: Long = 0,
    val savedWordId: Long,
    val repetitions: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Double = 2.5,
    val dueTimestamp: Long = System.currentTimeMillis(),
    val lastReviewedTimestamp: Long? = null,
    val lapses: Int = 0
) {
    fun toEntity(): SrsCardEntity {
        return SrsCardEntity(
            cardId = cardId,
            savedWordId = savedWordId,
            repetitions = repetitions,
            intervalDays = intervalDays,
            easeFactor = easeFactor,
            dueTimestamp = dueTimestamp,
            lastReviewedTimestamp = lastReviewedTimestamp,
            lapses = lapses
        )
    }

    companion object {
        fun fromEntity(entity: SrsCardEntity): SrsCard {
            return SrsCard(
                cardId = entity.cardId,
                savedWordId = entity.savedWordId,
                repetitions = entity.repetitions,
                intervalDays = entity.intervalDays,
                easeFactor = entity.easeFactor,
                dueTimestamp = entity.dueTimestamp,
                lastReviewedTimestamp = entity.lastReviewedTimestamp,
                lapses = entity.lapses
            )
        }
    }
}

/**
 * SrsFlashcardData: Dữ liệu đầy đủ hiển thị trên giao diện thẻ Flashcard (mặt trước & mặt sau).
 */
data class SrsFlashcardData(
    val card: SrsCard,
    val word: SavedWordEntity,
    val hanjaSegments: List<HanjaSegment> = emptyList(),
    val exampleSentenceKo: String? = null,
    val exampleSentenceVi: String? = null
)
