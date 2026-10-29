package com.hanviet.etymoai.data.db.workspace.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * SavedWordEntity: Quản lý từ vựng được người dùng đánh dấu lưu hoặc tạo thẻ ôn tập cá nhân.
 * Lưu trữ độc lập trong user_workspace.db, không bao giờ bị ghi đè khi cập nhật từ điển core.
 */
@Entity(
    tableName = "saved_words",
    indices = [
        Index(value = ["stable_key"], unique = false),
        Index(value = ["created_at"]),
        Index(value = ["updated_at"])
    ]
)
data class SavedWordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "stable_key")
    val stableKey: String,

    @ColumnInfo(name = "headword_snapshot")
    val headwordSnapshot: String,

    @ColumnInfo(name = "meaning_snapshot")
    val meaningSnapshot: String,

    @ColumnInfo(name = "phonetic_snapshot")
    val phoneticSnapshot: String? = null,

    @ColumnInfo(name = "origin_type")
    val originType: String = "SINO_KOREAN",

    @ColumnInfo(name = "personal_notes")
    val personalNotes: String? = null,

    @ColumnInfo(name = "tags")
    val tags: String? = null, // Phân loại theo nhãn dạng chuỗi dự phòng

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
