package com.app.taskmanager.core.domain.repository

import com.app.taskmanager.core.domain.models.ApiPreferences
import kotlinx.coroutines.flow.Flow

interface ApiPreferencesRepository {

    val apiPreferences: Flow<ApiPreferences>

    suspend fun updateGoogleAuthTokens(
        accessToken: String?,
        refreshToken: String?,
        expirationTimestamp: Long
    )

    suspend fun updateLastFullSyncTimestamp(timestamp: Long)

    suspend fun updateGoogleSyncToken(syncToken: String?)

    suspend fun clear()
}