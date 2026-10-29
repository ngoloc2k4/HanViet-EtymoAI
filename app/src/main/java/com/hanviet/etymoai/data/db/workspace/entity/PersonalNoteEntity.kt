package com.hanviet.etymoai.data.db.workspace.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * PersonalNoteEntity: Ghi chú học tập cá nhân (hỗ trợ Markdown) đính kèm theo từ vựng.
 * Ràng buộc CASCADE: Khi xóa từ vựng đã lưu thì toàn bộ ghi chú liên quan sẽ tự động xóa.
 */
@Entity(
    tableName = "personal_notes",
    foreignKeys = [
        ForeignKey(
            entity = SavedWordEntity::class,
            parentColumns = ["id"],
            childColumns = ["saved_word_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["saved_word_id"]),
        Index(value = ["updated_at"])
    ]
)
data class PersonalNoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    val noteId: Long = 0,

    @ColumnInfo(name = "saved_word_id")
    val savedWordId: Long,

    @ColumnInfo(name = "markdown_content")
    val markdownContent: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
