package com.app.taskmanager.core.domain.repository

import com.app.taskmanager.core.domain.models.Theme
import com.app.taskmanager.core.domain.models.UserPreferences
import com.app.taskmanager.core.domain.models.ViewType
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val userPreferences: Flow<UserPreferences>

    suspend fun updateTheme(theme: Theme)

    suspend fun updateDefaultView(viewType: ViewType)

    suspend fun updateSyncEnabled(isEnabled: Boolean)

    suspend fun updateLogInState(isLoggedIn: Boolean)

    suspend fun updateNotificationsEnabled(isEnabled: Boolean)
}