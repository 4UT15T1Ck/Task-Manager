package com.app.taskmanager.core.domain.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = Subtask.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = [Task.ID_COLUMN],
            childColumns = [Subtask.PARENT_TASK_ID_COLUMN],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Subtask(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID_COLUMN)
    val id: Int = 0,

    @ColumnInfo(name = PARENT_TASK_ID_COLUMN, index = true)
    val parentTaskId: Int,

    @ColumnInfo(name = TITLE_COLUMN)
    val title: String,

    @ColumnInfo(name = IS_COMPLETED_COLUMN)
    val isCompleted: Boolean = false
) {
    companion object {
        const val TABLE_NAME = "subtasks"
        const val ID_COLUMN = "_id"
        const val PARENT_TASK_ID_COLUMN = "parent_task_id"
        const val TITLE_COLUMN = "title"
        const val IS_COMPLETED_COLUMN = "is_completed"
    }
}