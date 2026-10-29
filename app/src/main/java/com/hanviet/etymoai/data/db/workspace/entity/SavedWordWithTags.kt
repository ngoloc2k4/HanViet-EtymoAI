package com.hanviet.etymoai.data.db.workspace.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * SavedWordWithTags: POJO kết hợp từ vựng đã lưu, các nhãn gắn kèm và ghi chú cá nhân.
 */
data class SavedWordWithTags(
    @Embedded
    val word: SavedWordEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "tag_id",
        associateBy = Junction(
            value = WordTagCrossRef::class,
            parentColumn = "word_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<WordTagEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "saved_word_id"
    )
    val notes: List<PersonalNoteEntity> = emptyList()
)
