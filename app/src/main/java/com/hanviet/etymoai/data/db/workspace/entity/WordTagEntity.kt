package com.hanviet.etymoai.data.db.workspace.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * WordTagEntity: Thẻ gắn nhãn phân loại từ vựng cá nhân (ví dụ: "TOPIK II", "Công sở", "Từ khó").
 */
@Entity(
    tableName = "word_tags",
    indices = [
        Index(value = ["tag_name"], unique = true)
    ]
)
data class WordTagEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tag_id")
    val tagId: Long = 0,

    @ColumnInfo(name = "tag_name")
    val tagName: String,

    @ColumnInfo(name = "tag_color_hex")
    val tagColorHex: String = "#1976D2",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
