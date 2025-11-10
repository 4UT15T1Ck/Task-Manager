package com.nguyenmanhkien.taskmanager.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nguyenmanhkien.taskmanager.core.domain.model.Theme
import com.nguyenmanhkien.taskmanager.core.domain.model.UserPreferences
import com.nguyenmanhkien.taskmanager.core.domain.model.ViewType
import com.nguyenmanhkien.taskmanager.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class  UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object UserPreferencesKeys {
        val THEME = stringPreferencesKey("theme_preference")
        val DEFAULT_VIEW = stringPreferencesKey("default_view_preference")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val USER_PHOTO_URL = stringPreferencesKey("user_photo_url")
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
            val notificationsEnabled =
                preferences[UserPreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
            val email = preferences[UserPreferencesKeys.USER_EMAIL]
            val displayName = preferences[UserPreferencesKeys.USER_DISPLAY_NAME]
            val photoUrl = preferences[UserPreferencesKeys.USER_PHOTO_URL]

            UserPreferences(
                theme,
                defaultView,
                syncEnabled,
                email,
                displayName,
                photoUrl,
                notificationsEnabled
            )
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

    override suspend fun updateNotificationsEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.NOTIFICATIONS_ENABLED] = isEnabled
        }
    }

    override suspend fun updateUserProfile(
        email: String?,
        displayName: String?,
        photoUrl: String?
    ) {
        dataStore.edit { preferences ->
            if (email != null) preferences[UserPreferencesKeys.USER_EMAIL] =
                email else preferences.remove(UserPreferencesKeys.USER_EMAIL)
            if (displayName != null) preferences[UserPreferencesKeys.USER_DISPLAY_NAME] =
                displayName else preferences.remove(UserPreferencesKeys.USER_DISPLAY_NAME)
            if (photoUrl != null) preferences[UserPreferencesKeys.USER_PHOTO_URL] =
                photoUrl else preferences.remove(UserPreferencesKeys.USER_PHOTO_URL)
        }
    }

    override suspend fun clearUserProfile() {
        dataStore.edit { preferences ->
            preferences.remove(UserPreferencesKeys.USER_EMAIL)
            preferences.remove(UserPreferencesKeys.USER_DISPLAY_NAME)
            preferences.remove(UserPreferencesKeys.USER_PHOTO_URL)
        }
    }
}