package com.nguyenmanhkien.taskmanager.features.reminders.data.repository

import com.nguyenmanhkien.taskmanager.features.reminders.data.dao.ReminderDao
import com.nguyenmanhkien.taskmanager.features.reminders.domain.model.Reminder
import com.nguyenmanhkien.taskmanager.features.reminders.domain.model.ReminderStatus
import com.nguyenmanhkien.taskmanager.features.reminders.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow

class ReminderRepositoryImpl(
    private val dao: ReminderDao
) : ReminderRepository {

    override fun getRemindersForTask(taskId: Int): Flow<List<Reminder>> =
        dao.getRemindersForTaskFlow(taskId)

    override suspend fun getReminderById(id: Int): Reminder? =
        dao.getReminderById(id)

    override suspend fun getRemindersByTaskId(taskId: Int): List<Reminder> =
        dao.getAllRemindersByTaskId(taskId)

    override suspend fun getDueReminders(startTime: Long, endTime: Long): List<Reminder> =
        dao.getDueRemindersInRange(startTime, endTime)

    override suspend fun upsertReminder(reminder: Reminder) =
        dao.upsertReminder(reminder)

    override suspend fun upsertReminders(reminders: List<Reminder>) =
        dao.upsertReminders(reminders)

    override suspend fun deleteReminder(reminder: Reminder) =
        dao.deleteReminder(reminder)

    override suspend fun deleteRemindersForTask(reminders: List<Reminder>) =
        dao.deleteReminders(reminders)

    override suspend fun updateStatus(id: Int, status: ReminderStatus) =
        dao.updateReminderStatus(id, status)

    override suspend fun updateWorkRequestId(id: Int, workRequestId: String?) =
        dao.updateWorkRequestId(id, workRequestId)

    override suspend fun snoozeReminder(id: Int, snoozedUntil: Long) =
        dao.snoozeReminder(id, snoozedUntil)
}