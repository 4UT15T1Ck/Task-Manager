package com.app.taskmanager.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.taskmanager.core.domain.models.DateFormat
import com.app.taskmanager.core.domain.models.ReminderTime
import com.app.taskmanager.core.domain.models.TaskPreferences
import com.app.taskmanager.core.domain.models.TimeFormat
import com.app.taskmanager.core.domain.repository.TaskPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class TaskPreferencesRepositoryImpl (
    private val dataStore: DataStore<Preferences>
) : TaskPreferencesRepository {

    private object TaskPreferencesKeys {
        val TIME_FORMAT = stringPreferencesKey("time_format_preference")
        val DATE_FORMAT = stringPreferencesKey("date_format_preference")
        val REMINDER_TIME_BEFORE_START = intPreferencesKey("reminder_time_before_start")
        val REMINDER_TIME_BEFORE_DUE = intPreferencesKey("reminder_time_before_due")
        val DEFAULT_TASK_DURATION_MINUTES = intPreferencesKey("default_task_duration_minutes")
    }

    override val taskPreferences: Flow<TaskPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map {
            preferences ->
            val timeFormat = TimeFormat.fromString(preferences[TaskPreferencesKeys.TIME_FORMAT])
            val dateFormat = DateFormat.fromString(preferences[TaskPreferencesKeys.DATE_FORMAT])
            val reminderTimeBeforeStart = ReminderTime.fromMinutes(preferences[TaskPreferencesKeys.REMINDER_TIME_BEFORE_START])
            val reminderTimeBeforeDue = ReminderTime.fromMinutes(preferences[TaskPreferencesKeys.REMINDER_TIME_BEFORE_DUE])
            val defaultTaskDurationMinutes = preferences[TaskPreferencesKeys.DEFAULT_TASK_DURATION_MINUTES] ?: 60

            TaskPreferences(timeFormat, dateFormat, reminderTimeBeforeStart, reminderTimeBeforeDue, defaultTaskDurationMinutes)
        }

    override suspend fun updateTimeFormat(timeFormat: TimeFormat) {
        dataStore.edit { preferences ->
            preferences[TaskPreferencesKeys.TIME_FORMAT] = timeFormat.name
        }
    }

    override suspend fun updateDateFormat(dateFormat: DateFormat) {
        dataStore.edit { preferences ->
            preferences[TaskPreferencesKeys.DATE_FORMAT] = dateFormat.name
        }
    }

    override suspend fun updateReminderTimeBeforeStart(reminderTime: ReminderTime) {
        dataStore.edit { preferences ->
            preferences[TaskPreferencesKeys.REMINDER_TIME_BEFORE_START] = reminderTime.durationMinutes
        }
    }

    override suspend fun updateReminderTimeBeforeDue(reminderTime: ReminderTime) {
        dataStore.edit { preferences ->
            preferences[TaskPreferencesKeys.REMINDER_TIME_BEFORE_DUE] = reminderTime.durationMinutes
        }
    }

    override suspend fun updateDefaultTaskDurationMinutes(durationMinutes: Int) {
        dataStore.edit { preferences ->
            preferences[TaskPreferencesKeys.DEFAULT_TASK_DURATION_MINUTES] = durationMinutes
        }
    }
}