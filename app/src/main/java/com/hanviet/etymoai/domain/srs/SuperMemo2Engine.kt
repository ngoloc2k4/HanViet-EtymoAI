package com.hanviet.etymoai.domain.srs

import kotlin.math.roundToInt

/**
 * SuperMemo2Engine: Hiện thực hóa thuật toán SuperMemo SM-2 chuẩn hóa bởi Piotr Woźniak.
 * Quản trị chu kỳ lặp lại ngắt quãng (Spaced Repetition) dựa trên hệ số độ dễ (Ease Factor)
 * và chất lượng nhớ từ vựng (Quality 0..5).
 */
object SuperMemo2Engine {

    const val MILLIS_PER_DAY = 86_400_000L
    const val MINIMUM_EASE_FACTOR = 1.3
    const val INITIAL_EASE_FACTOR = 2.5

    /**
     * Tính toán trạng thái tiếp theo của thẻ Flashcard theo giải thuật SuperMemo SM-2.
     *
     * @param currentCard Trạng thái thẻ hiện tại
     * @param rating Đánh giá của người học (AGAIN=0, HARD=3, GOOD=4, EASY=5)
     * @param reviewTimestamp Thời điểm hoàn thành đánh giá (mili-giây)
     * @return SrsCard trạng thái mới với interval và dueTimestamp đã được cập nhật
     */
    fun calculateNextState(
        currentCard: SrsCard,
        rating: ReviewRating,
        reviewTimestamp: Long = System.currentTimeMillis()
    ): SrsCard {
        val q = rating.quality

        // 1. Tính toán Ease Factor mới:
        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        val delta = 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)
        val calculatedEf = currentCard.easeFactor + delta
        val newEf = if (calculatedEf < MINIMUM_EASE_FACTOR) MINIMUM_EASE_FACTOR else calculatedEf

        val newRepetitions: Int
        val newIntervalDays: Int
        val newLapses: Int

        // 2. Cập nhật repetitions và khoảng thời gian lặp lại (interval):
        if (q < 3) {
            // Đánh giá thất bại (AGAIN): reset số lần lặp, hẹn ôn lại vào ngày hôm sau
            newRepetitions = 0
            newIntervalDays = 1
            newLapses = currentCard.lapses + 1
        } else {
            // Đánh giá thành công (HARD, GOOD, EASY):
            when (currentCard.repetitions) {
                0 -> {
                    newIntervalDays = 1
                }
                1 -> {
                    newIntervalDays = 6
                }
                else -> {
                    val rawInterval = currentCard.intervalDays * newEf
                    newIntervalDays = rawInterval.roundToInt().coerceAtLeast(1)
                }
            }
            newRepetitions = currentCard.repetitions + 1
            newLapses = currentCard.lapses
        }

        val newDueTimestamp = reviewTimestamp + (newIntervalDays.toLong() * MILLIS_PER_DAY)

        return currentCard.copy(
            repetitions = newRepetitions,
            intervalDays = newIntervalDays,
            easeFactor = (newEf * 100.0).roundToInt() / 100.0, // Làm tròn 2 chữ số thập phân
            dueTimestamp = newDueTimestamp,
            lastReviewedTimestamp = reviewTimestamp,
            lapses = newLapses
        )
    }

    /**
     * Dự đoán khoảng thời gian ôn tiếp theo để hiển thị trên nhãn các nút bấm (ví dụ: "<1 ngày", "6 ngày", "15 ngày").
     */
    fun getIntervalPreview(currentCard: SrsCard, rating: ReviewRating): String {
        val next = calculateNextState(currentCard, rating)
        return when {
            next.intervalDays <= 1 -> "1 ngày"
            next.intervalDays < 30 -> "${next.intervalDays} ngày"
            next.intervalDays < 365 -> "${next.intervalDays / 30} tháng"
            else -> "${next.intervalDays / 365} năm"
        }
    }
}
