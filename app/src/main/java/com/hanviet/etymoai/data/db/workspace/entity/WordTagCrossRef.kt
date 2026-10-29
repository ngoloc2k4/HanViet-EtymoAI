package com.hanviet.etymoai.data.db.workspace.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * WordTagCrossRef: Bảng quan hệ Nhiều - Nhiều giữa từ vựng đã lưu (saved_words) và nhãn (word_tags).
 * Ràng buộc CASCADE: Khi xóa từ hoặc xóa nhãn thì liên kết tự động được dọn dẹp.
 */
@Entity(
    tableName = "word_tag_cross_ref",
    primaryKeys = ["word_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = SavedWordEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WordTagEntity::class,
            parentColumns = ["tag_id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["word_id"]),
        Index(value = ["tag_id"])
    ]
)
data class WordTagCrossRef(
    @ColumnInfo(name = "word_id")
    val wordId: Long,

    @ColumnInfo(name = "tag_id")
    val tagId: Long,

    @ColumnInfo(name = "assigned_at")
    val assignedAt: Long = System.currentTimeMillis()
)
