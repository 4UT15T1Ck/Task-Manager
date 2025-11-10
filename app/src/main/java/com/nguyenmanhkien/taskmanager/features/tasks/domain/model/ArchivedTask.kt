package com.nguyenmanhkien.taskmanager.features.tasks.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = ArchivedTask.TABLE_NAME)
data class ArchivedTask(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = ID_COLUMN)
    val id: Int,

    @ColumnInfo(name = TITLE_COLUMN)
    val title: String,

    @ColumnInfo(name = CATEGORY_ID_COLUMN, index = true)
    val categoryId: Int?,

    @ColumnInfo(name = STATUS_COLUMN)
    val status: TaskStatus,

    @ColumnInfo(name = START_AT_COLUMN)
    val startAt: Long?,

    @ColumnInfo(name = DUE_AT_COLUMN)
    val dueAt: Long?,

    @ColumnInfo(name = COMPLETION_DATE_COLUMN)
    val completionDate: Long?,

    @ColumnInfo(name = CREATED_AT_COLUMN)
    val createdAt: Long,

    @ColumnInfo(name = ARCHIVED_AT_COLUMN)
    val archivedAt: Long,

    @ColumnInfo(name = COMPLETION_TYPE_COLUMN)
    val completionType: TaskCompletionType
) {
    companion object {
        const val TABLE_NAME = "archived_tasks"
        const val ID_COLUMN = "_id"
        const val TITLE_COLUMN = "title"
        const val CATEGORY_ID_COLUMN = "category_id"
        const val STATUS_COLUMN = "status"
        const val START_AT_COLUMN = "start_at"
        const val DUE_AT_COLUMN = "due_at"
        const val COMPLETION_DATE_COLUMN = "completion_date"
        const val CREATED_AT_COLUMN = "created_at"
        const val ARCHIVED_AT_COLUMN = "archived_at"
        const val COMPLETION_TYPE_COLUMN = "completion_type"
    }
}