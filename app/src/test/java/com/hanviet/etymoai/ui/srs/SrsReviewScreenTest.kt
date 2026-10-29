package com.hanviet.etymoai.ui.srs

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.hanviet.etymoai.data.db.workspace.WorkspaceDatabase
import com.hanviet.etymoai.data.db.workspace.entity.SavedWordEntity
import com.hanviet.etymoai.domain.etymology.HanjaSegment
import com.hanviet.etymoai.domain.srs.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SrsReviewScreenTest {

    private lateinit var db: WorkspaceDatabase
    private lateinit var repository: SrsRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = WorkspaceDatabase.createInMemory(context)
        repository = SrsRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testReviewRatingAttributes() {
        assertEquals(0, ReviewRating.AGAIN.quality)
        assertEquals("Quên", ReviewRating.AGAIN.labelVi)

        assertEquals(3, ReviewRating.HARD.quality)
        assertEquals("Khó", ReviewRating.HARD.labelVi)

        assertEquals(4, ReviewRating.GOOD.quality)
        assertEquals("Tốt", ReviewRating.GOOD.labelVi)

        assertEquals(5, ReviewRating.EASY.quality)
        assertEquals("Dễ", ReviewRating.EASY.labelVi)
    }

    @Test
    fun testSrsCardModel_toAndFromEntity() {
        val card = SrsCard(
            cardId = 42L,
            savedWordId = 100L,
            repetitions = 3,
            intervalDays = 15,
            easeFactor = 2.6,
            dueTimestamp = 1729000000000L,
            lastReviewedTimestamp = 1728000000000L,
            lapses = 1
        )

        val entity = card.toEntity()
        assertEquals(42L, entity.cardId)
        assertEquals(100L, entity.savedWordId)
        assertEquals(3, entity.repetitions)
        assertEquals(15, entity.intervalDays)
        assertEquals(2.6, entity.easeFactor, 0.001)

        val restored = SrsCard.fromEntity(entity)
        assertEquals(card, restored)
    }

    @Test
    fun testSrsRepository_createCardAndReviewCycle() = runBlocking {
        // 1. Lưu từ vựng vào workspace
        val wordId = db.savedWordDao().insert(
            SavedWordEntity(
                stableKey = "ko_haksaeng_1",
                headwordSnapshot = "학생",
                meaningSnapshot = "Học sinh",
                originType = "SINO_KOREAN"
            )
        )
        assertTrue(wordId > 0)

        // 2. Tạo thẻ SRS cho từ vừa lưu
        val cardId = repository.createCardForSavedWord(wordId)
        assertTrue(cardId > 0)

        // 3. Đếm số thẻ đến hạn ôn tập
        val dueCount = repository.countDueCards(System.currentTimeMillis() + 1000)
        assertEquals(1, dueCount)

        // 4. Lấy danh sách thẻ ôn
        val dueCards = repository.getDueCardsSync(System.currentTimeMillis() + 1000)
        assertEquals(1, dueCards.size)
        assertEquals("학생", dueCards[0].word.headwordSnapshot)

        // 5. Đánh giá chất lượng GOOD (nhớ tốt)
        val reviewedCard = repository.processReview(
            card = dueCards[0].card,
            rating = ReviewRating.GOOD,
            reviewTimestamp = System.currentTimeMillis()
        )
        assertEquals(1, reviewedCard.repetitions)
        assertEquals(1, reviewedCard.intervalDays)

        // Sau khi đánh giá, thẻ được dời lịch sang ngày mai nên không còn nằm trong số thẻ đến hạn hiện tại
        val remainingDue = repository.countDueCards(System.currentTimeMillis())
        assertEquals(0, remainingDue)
    }
}
