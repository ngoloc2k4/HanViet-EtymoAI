package com.hanviet.etymoai.domain.srs

import org.junit.Assert.*
import org.junit.Test

class SuperMemo2EngineTest {

    @Test
    fun testFirstRepetition_good() {
        val initialCard = SrsCard(
            cardId = 1L,
            savedWordId = 10L,
            repetitions = 0,
            intervalDays = 0,
            easeFactor = 2.5
        )
        val now = 1000000000L
        val next = SuperMemo2Engine.calculateNextState(initialCard, ReviewRating.GOOD, now)

        assertEquals(1, next.repetitions)
        assertEquals(1, next.intervalDays)
        assertEquals(2.5, next.easeFactor, 0.01)
        assertEquals(now + SuperMemo2Engine.MILLIS_PER_DAY, next.dueTimestamp)
        assertEquals(0, next.lapses)
    }

    @Test
    fun testSecondRepetition_good() {
        val card = SrsCard(
            cardId = 1L,
            savedWordId = 10L,
            repetitions = 1,
            intervalDays = 1,
            easeFactor = 2.5
        )
        val now = 1000000000L
        val next = SuperMemo2Engine.calculateNextState(card, ReviewRating.GOOD, now)

        assertEquals(2, next.repetitions)
        assertEquals(6, next.intervalDays)
        assertEquals(2.5, next.easeFactor, 0.01)
        assertEquals(now + 6 * SuperMemo2Engine.MILLIS_PER_DAY, next.dueTimestamp)
    }

    @Test
    fun testThirdRepetition_easy() {
        val card = SrsCard(
            cardId = 1L,
            savedWordId = 10L,
            repetitions = 2,
            intervalDays = 6,
            easeFactor = 2.5
        )
        val now = 1000000000L
        val next = SuperMemo2Engine.calculateNextState(card, ReviewRating.EASY, now)

        // q=5: delta = 0.1 -> new EF = 2.6
        // new interval = round(6 * 2.6) = round(15.6) = 16
        assertEquals(3, next.repetitions)
        assertEquals(16, next.intervalDays)
        assertEquals(2.6, next.easeFactor, 0.01)
        assertEquals(now + 16 * SuperMemo2Engine.MILLIS_PER_DAY, next.dueTimestamp)
    }

    @Test
    fun testFailureReset_again() {
        val card = SrsCard(
            cardId = 1L,
            savedWordId = 10L,
            repetitions = 4,
            intervalDays = 30,
            easeFactor = 2.4,
            lapses = 0
        )
        val now = 1000000000L
        val next = SuperMemo2Engine.calculateNextState(card, ReviewRating.AGAIN, now)

        assertEquals("Khi quên (Again), số lần lặp phải bị reset về 0", 0, next.repetitions)
        assertEquals("Khi quên, khoảng thời gian ôn tập reset về 1 ngày", 1, next.intervalDays)
        assertEquals(1, next.lapses)
        assertTrue("Ease factor phải giảm sau khi quên", next.easeFactor < 2.4)
        assertEquals(now + SuperMemo2Engine.MILLIS_PER_DAY, next.dueTimestamp)
    }

    @Test
    fun testMinimumEaseFactorFloor() {
        var card = SrsCard(
            cardId = 1L,
            savedWordId = 10L,
            repetitions = 0,
            intervalDays = 1,
            easeFactor = 1.35
        )
        // Lặp lại nhiều lần đánh giá AGAIN (0)
        for (i in 1..5) {
            card = SuperMemo2Engine.calculateNextState(card, ReviewRating.AGAIN)
        }
        assertEquals("Hệ số dễ (Ease Factor) không được giảm xuống dưới ngưỡng 1.3", 1.3, card.easeFactor, 0.001)
    }

    @Test
    fun testIntervalPreviewLabels() {
        val card = SrsCard(
            cardId = 1L,
            savedWordId = 10L,
            repetitions = 0,
            intervalDays = 0,
            easeFactor = 2.5
        )
        assertEquals("1 ngày", SuperMemo2Engine.getIntervalPreview(card, ReviewRating.AGAIN))
        assertEquals("1 ngày", SuperMemo2Engine.getIntervalPreview(card, ReviewRating.GOOD))

        val card2 = SrsCard(
            cardId = 1L,
            savedWordId = 10L,
            repetitions = 1,
            intervalDays = 1,
            easeFactor = 2.5
        )
        assertEquals("6 ngày", SuperMemo2Engine.getIntervalPreview(card2, ReviewRating.GOOD))
    }
}
