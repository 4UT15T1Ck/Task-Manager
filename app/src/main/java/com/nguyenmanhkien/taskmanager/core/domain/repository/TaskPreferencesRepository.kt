package com.nguyenmanhkien.taskmanager.core.domain.repository

import com.nguyenmanhkien.taskmanager.core.domain.model.DateFormat
import com.nguyenmanhkien.taskmanager.core.domain.model.ReminderTime
import com.nguyenmanhkien.taskmanager.core.domain.model.TaskPreferences
import com.nguyenmanhkien.taskmanager.core.domain.model.TimeFormat
import kotlinx.coroutines.flow.Flow

interface TaskPreferencesRepository {

    val taskPreferences: Flow<TaskPreferences>

    suspend fun updateTimeFormat(timeFormat: TimeFormat)

    suspend fun updateDateFormat(dateFormat: DateFormat)

    suspend fun updateReminderTimeBeforeStart(reminderTime: ReminderTime)

    suspend fun updateReminderTimeBeforeDue(reminderTime: ReminderTime)

    suspend fun updateDefaultTaskDurationMinutes(durationMinutes: Int)
}