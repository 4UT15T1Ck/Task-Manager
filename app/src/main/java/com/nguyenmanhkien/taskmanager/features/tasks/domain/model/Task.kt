package com.nguyenmanhkien.taskmanager.features.tasks.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class TaskStatus { IN_PROGRESS, COMPLETED, PAST_DUE }
enum class TaskPriority { NO_PRIORITY, LOW, MEDIUM, HIGH }
enum class TaskCompletionType { NOT_COMPLETED, EARLY, ON_TIME, LATE }
enum class SyncStatus { SYNCED, CREATED, UPDATED, DELETED }

@Entity(
    tableName = Task.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = [Category.ID_COLUMN],
            childColumns = [Task.CATEGORY_ID_COLUMN],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Task::class,
            parentColumns = [Task.ID_COLUMN],
            childColumns = [Task.PARENT_ID_COLUMN],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID_COLUMN)
    val id: Int = 0,

    @ColumnInfo(name = TITLE_COLUMN)
    val title: String,

    @ColumnInfo(name = DESCRIPTION_COLUMN)
    val description: String? = null,

    @ColumnInfo(name = PARENT_ID_COLUMN, index = true)
    val parentId: Int? = null,

    @ColumnInfo(name = CATEGORY_ID_COLUMN, index = true)
    val categoryId: Int? = null,

    @ColumnInfo(name = STATUS_COLUMN)
    val status: TaskStatus = TaskStatus.IN_PROGRESS,

    @ColumnInfo(name = PRIORITY_COLUMN)
    val priority: TaskPriority = TaskPriority.NO_PRIORITY,

    @ColumnInfo(name = DUE_AT_COLUMN)
    val dueAt: Long? = null,

    @ColumnInfo(name = START_AT_COLUMN)
    val startAt: Long? = null,

    @ColumnInfo(name = COMPLETION_DATE_COLUMN)
    val completionDate: Long? = null,

    @ColumnInfo(name = CREATED_AT_COLUMN)
    val createdAt: Long,

    @ColumnInfo(name = UPDATED_AT_COLUMN)
    val updatedAt: Long,

    @ColumnInfo(name = SYNC_STATUS_COLUMN, defaultValue = "CREATED")
    val syncStatus: SyncStatus = SyncStatus.CREATED,

    @ColumnInfo(name = GOOGLE_TASK_ID_COLUMN)
    val googleTaskId: String? = null,

    @ColumnInfo(name = COMPLETION_TYPE_COLUMN)
    val completionType: TaskCompletionType = TaskCompletionType.NOT_COMPLETED,

    @ColumnInfo(name = RRULE_COLUMN)
    val rrule: String? = null
) {
    companion object {
        const val TABLE_NAME = "tasks"
        const val ID_COLUMN = "_id"
        const val TITLE_COLUMN = "title"
        const val DESCRIPTION_COLUMN = "description"
        const val PARENT_ID_COLUMN = "parent_id"
        const val CATEGORY_ID_COLUMN = "category_id"
        const val STATUS_COLUMN = "status"
        const val PRIORITY_COLUMN = "priority"
        const val DUE_AT_COLUMN = "due_at"
        const val START_AT_COLUMN = "start_at"
        const val COMPLETION_DATE_COLUMN = "completion_date"
        const val CREATED_AT_COLUMN = "created_at"
        const val UPDATED_AT_COLUMN = "updated_at"
        const val SYNC_STATUS_COLUMN = "sync_status"
        const val GOOGLE_TASK_ID_COLUMN = "google_task_id"
        const val COMPLETION_TYPE_COLUMN = "completion_type"
        const val RRULE_COLUMN = "rrule"
    }
}