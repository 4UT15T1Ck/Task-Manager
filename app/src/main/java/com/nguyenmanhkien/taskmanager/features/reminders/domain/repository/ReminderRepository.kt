package com.nguyenmanhkien.taskmanager.features.reminders.domain.repository

import com.nguyenmanhkien.taskmanager.features.reminders.domain.model.Reminder
import com.nguyenmanhkien.taskmanager.features.reminders.domain.model.ReminderStatus
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getRemindersForTask(taskId: Int): Flow<List<Reminder>>

    suspend fun getReminderById(id: Int): Reminder?

    suspend fun getRemindersByTaskId(taskId: Int): List<Reminder>

    suspend fun getDueReminders(startTime: Long, endTime: Long): List<Reminder>

    suspend fun upsertReminder(reminder: Reminder)

    suspend fun upsertReminders(reminders: List<Reminder>)

    suspend fun deleteReminder(reminder: Reminder)

    suspend fun deleteRemindersForTask(reminders: List<Reminder>)

    suspend fun updateStatus(id: Int, status: ReminderStatus)

    suspend fun updateWorkRequestId(id: Int, workRequestId: String?)

    suspend fun snoozeReminder(id: Int, snoozedUntil: Long)
}