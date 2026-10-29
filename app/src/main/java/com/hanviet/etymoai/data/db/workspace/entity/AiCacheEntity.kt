package com.hanviet.etymoai.data.db.workspace.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AiCacheEntity: Bộ đệm cục bộ lưu trữ các phản hồi phân tích từ ngữ của AI.
 * Giúp người dùng tiết kiệm token API và hiển thị câu trả lời ngay lập tức khi tra lại cùng một cụm từ.
 */
@Entity(tableName = "ai_cache")
data class AiCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "prompt_hash")
    val promptHash: String,

    @ColumnInfo(name = "response_json")
    val responseJson: String,

    @ColumnInfo(name = "model_id")
    val modelId: String? = null,

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "expiry_time")
    val expiryTime: Long? = null
)
