package com.hanviet.etymoai.data.db.workspace.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * SearchHistoryEntity: Quản lý lịch sử tra cứu của người dùng trên thiết bị.
 */
@Entity(
    tableName = "search_history",
    indices = [Index(value = ["searched_at"])]
)
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "query_text")
    val queryText: String,

    @ColumnInfo(name = "searched_at")
    val searchedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "result_count")
    val resultCount: Int = 0
)
