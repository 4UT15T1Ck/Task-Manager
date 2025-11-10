package com.nguyenmanhkien.taskmanager.features.reminders.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nguyenmanhkien.taskmanager.features.reminders.domain.model.Reminder
import com.nguyenmanhkien.taskmanager.features.reminders.domain.model.ReminderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<Reminder>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminders(reminders: List<Reminder>)

    @Query(
        "SELECT * FROM ${Reminder.TABLE_NAME} " +
                "WHERE ${Reminder.TASK_ID_COLUMN} = :taskId"
    )
    suspend fun getAllRemindersByTaskId(taskId: Int): List<Reminder>

    @Query(
        "SELECT * FROM ${Reminder.TABLE_NAME} " +
                "WHERE ${Reminder.ID_COLUMN} = :reminderId"
    )
    suspend fun getReminderById(reminderId: Int): Reminder?

    @Query(
        "SELECT * FROM ${Reminder.TABLE_NAME} " +
                "WHERE ${Reminder.TASK_ID_COLUMN} = :taskId " +
                "ORDER BY ${Reminder.REMINDER_TIME_COLUMN} ASC"
    )
    fun getRemindersForTaskFlow(taskId: Int): Flow<List<Reminder>>

    @Query(
        "SELECT * FROM ${Reminder.TABLE_NAME} WHERE " +
                "(${Reminder.STATUS_COLUMN} = 'PENDING' " +
                "AND ${Reminder.REMINDER_TIME_COLUMN} BETWEEN :startTime AND :endTime) " +
                "OR (${Reminder.STATUS_COLUMN} = 'SNOOZED' " +
                "AND ${Reminder.SNOOZED_UNTIL_COLUMN} BETWEEN :startTime AND :endTime)"
    )
    suspend fun getDueRemindersInRange(startTime: Long, endTime: Long): List<Reminder>

    @Query(
        "UPDATE ${Reminder.TABLE_NAME} " +
                "SET ${Reminder.STATUS_COLUMN} = :newStatus " +
                "WHERE ${Reminder.ID_COLUMN} = :reminderId"
    )
    suspend fun updateReminderStatus(reminderId: Int, newStatus: ReminderStatus)

    @Query(
        "UPDATE ${Reminder.TABLE_NAME} " +
                "SET ${Reminder.WORK_REQUEST_ID_COLUMN} = :workRequestId " +
                "WHERE ${Reminder.ID_COLUMN} = :reminderId"
    )
    suspend fun updateWorkRequestId(reminderId: Int, workRequestId: String?)

    @Query(
        "UPDATE ${Reminder.TABLE_NAME}" +
                " SET ${Reminder.STATUS_COLUMN} = 'SNOOZED', " +
                "${Reminder.SNOOZED_UNTIL_COLUMN} = :snoozedUntilTimestamp " +
                "WHERE ${Reminder.ID_COLUMN} = :reminderId"
    )
    suspend fun snoozeReminder(reminderId: Int, snoozedUntilTimestamp: Long)
}