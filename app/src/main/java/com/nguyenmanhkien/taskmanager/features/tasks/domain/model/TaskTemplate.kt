package com.nguyenmanhkien.taskmanager.features.tasks.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = TaskTemplate.TABLE_NAME)
data class TaskTemplate(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID_COLUMN)
    val id: Int = 0,

    @ColumnInfo(name = TITLE_COLUMN)
    val title: String,

    @ColumnInfo(name = DESCRIPTION_COLUMN)
    val description: String? = null,

    @ColumnInfo(name = CREATED_AT_COLUMN)
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = RRULE_COLUMN)
    val rrule: String? = null
) {
    companion object {
        const val TABLE_NAME = "task_templates"
        const val ID_COLUMN = "_id"
        const val TITLE_COLUMN = "title"
        const val DESCRIPTION_COLUMN = "description"
        const val CREATED_AT_COLUMN = "created_at"
        const val RRULE_COLUMN = "rrule"
    }
}