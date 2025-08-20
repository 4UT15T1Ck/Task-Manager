package com.app.taskmanager.core.domain.repository

import com.app.taskmanager.core.domain.models.DateFormat
import com.app.taskmanager.core.domain.models.ReminderTime
import com.app.taskmanager.core.domain.models.TaskPreferences
import com.app.taskmanager.core.domain.models.TimeFormat
import kotlinx.coroutines.flow.Flow

interface TaskPreferencesRepository {

    val taskPreferences: Flow<TaskPreferences>

    suspend fun updateTimeFormat(timeFormat: TimeFormat)

    suspend fun updateDateFormat(dateFormat: DateFormat)

    suspend fun updateReminderTimeBeforeStart(reminderTime: ReminderTime)

    suspend fun updateReminderTimeBeforeDue(reminderTime: ReminderTime)

    suspend fun updateDefaultTaskDurationMinutes(durationMinutes: Int)
}