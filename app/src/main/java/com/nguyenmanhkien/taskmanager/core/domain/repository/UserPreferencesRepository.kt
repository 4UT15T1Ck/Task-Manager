package com.nguyenmanhkien.taskmanager.core.domain.repository

import com.nguyenmanhkien.taskmanager.core.domain.model.Theme
import com.nguyenmanhkien.taskmanager.core.domain.model.UserPreferences
import com.nguyenmanhkien.taskmanager.core.domain.model.ViewType
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val userPreferences: Flow<UserPreferences>

    suspend fun updateTheme(theme: Theme)

    suspend fun updateDefaultView(viewType: ViewType)

    suspend fun updateSyncEnabled(isEnabled: Boolean)

    suspend fun updateNotificationsEnabled(isEnabled: Boolean)

    suspend fun updateUserProfile(
        email: String?,
        displayName: String?,
        photoUrl: String?
    )

    suspend fun clearUserProfile()
}