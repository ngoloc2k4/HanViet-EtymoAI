package com.hanviet.etymoai.data.db.workspace.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * SrsCardEntity: Thẻ ôn tập ngắt quãng (Spaced Repetition System) theo giải thuật SuperMemo SM-2.
 */
@Entity(
    tableName = "srs_cards",
    foreignKeys = [
        ForeignKey(
            entity = SavedWordEntity::class,
            parentColumns = ["id"],
            childColumns = ["saved_word_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["saved_word_id"], unique = true),
        Index(value = ["due_timestamp"])
    ]
)
data class SrsCardEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "card_id")
    val cardId: Long = 0,

    @ColumnInfo(name = "saved_word_id")
    val savedWordId: Long,

    @ColumnInfo(name = "repetitions")
    val repetitions: Int = 0,

    @ColumnInfo(name = "interval_days")
    val intervalDays: Int = 0,

    @ColumnInfo(name = "ease_factor")
    val easeFactor: Double = 2.5,

    @ColumnInfo(name = "due_timestamp")
    val dueTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_reviewed_timestamp")
    val lastReviewedTimestamp: Long? = null,

    @ColumnInfo(name = "lapses")
    val lapses: Int = 0
)
