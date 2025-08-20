package com.app.taskmanager.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.taskmanager.core.domain.models.Theme
import com.app.taskmanager.core.domain.models.UserPreferences
import com.app.taskmanager.core.domain.models.ViewType
import com.app.taskmanager.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object UserPreferencesKeys {
        val THEME = stringPreferencesKey("theme_preference")
        val DEFAULT_VIEW = stringPreferencesKey("default_view_preference")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val LOGGED_IN = booleanPreferencesKey("logged_in")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    override val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val theme = Theme.fromString(preferences[UserPreferencesKeys.THEME])
            val defaultView = ViewType.fromString(preferences[UserPreferencesKeys.DEFAULT_VIEW])
            val syncEnabled = preferences[UserPreferencesKeys.SYNC_ENABLED] ?: false
            val loggedIn = preferences[UserPreferencesKeys.LOGGED_IN] ?: false
            val notificationsEnabled = preferences[UserPreferencesKeys.NOTIFICATIONS_ENABLED] ?: true

            UserPreferences(theme, defaultView, syncEnabled, loggedIn, notificationsEnabled)
        }

    override suspend fun updateTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.THEME] = theme.name
        }
    }

    override suspend fun updateDefaultView(viewType: ViewType) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.DEFAULT_VIEW] = viewType.name
        }
    }

    override suspend fun updateSyncEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.SYNC_ENABLED] = isEnabled
        }
    }

    override suspend fun updateLogInState(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.LOGGED_IN] = isLoggedIn
        }
    }

    override suspend fun updateNotificationsEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.NOTIFICATIONS_ENABLED] = isEnabled
        }
    }
}