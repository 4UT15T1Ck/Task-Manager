package com.nguyenmanhkien.taskmanager.features.reminders.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task

@Entity(
    tableName = Reminder.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = [Task.ID_COLUMN],
            childColumns = [Reminder.TASK_ID_COLUMN],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID_COLUMN)
    val id: Int = 0,

    @ColumnInfo(name = TASK_ID_COLUMN, index = true)
    val taskId: Int,

    @ColumnInfo(name = REMINDER_TIME_COLUMN)
    val reminderTime: Long,

    @ColumnInfo(name = STATUS_COLUMN)
    val status: ReminderStatus = ReminderStatus.PENDING,

    @ColumnInfo(name = SNOOZED_UNTIL_COLUMN)
    val snoozedUntil: Long? = null,

    @ColumnInfo(name = WORK_REQUEST_ID_COLUMN)
    val workRequestId: String? = null
) {
    companion object {
        const val TABLE_NAME = "reminders"
        const val ID_COLUMN = "_id"
        const val TASK_ID_COLUMN = "task_id"
        const val REMINDER_TIME_COLUMN = "reminder_time"
        const val STATUS_COLUMN = "status"
        const val SNOOZED_UNTIL_COLUMN = "snoozed_until"
        const val WORK_REQUEST_ID_COLUMN = "work_request_id"
    }
}