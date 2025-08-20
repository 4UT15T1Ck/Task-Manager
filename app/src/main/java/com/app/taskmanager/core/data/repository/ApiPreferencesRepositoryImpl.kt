package com.app.taskmanager.core.data.repository

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.app.taskmanager.core.data.encryption.Crypto
import com.app.taskmanager.core.domain.models.ApiPreferences
import com.app.taskmanager.core.domain.repository.ApiPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class ApiPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : ApiPreferencesRepository {

    private object ApiPreferencesKeys {
        val GOOGLE_ACCESS_TOKEN = stringPreferencesKey("google_access_token")
        val GOOGLE_REFRESH_TOKEN = stringPreferencesKey("google_refresh_token")
        val ACCESS_TOKEN_EXPIRATION_TIMESTAMP =
            longPreferencesKey("access_token_expiration_timestamp")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val GOOGLE_SYNC_TOKEN = stringPreferencesKey("google_sync_token")
    }

    override val apiPreferences: Flow<ApiPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val accessToken = decryptString(preferences[ApiPreferencesKeys.GOOGLE_ACCESS_TOKEN])
            val refreshToken = decryptString(preferences[ApiPreferencesKeys.GOOGLE_REFRESH_TOKEN])
            val syncToken = decryptString(preferences[ApiPreferencesKeys.GOOGLE_SYNC_TOKEN])

            val expirationTimestamp =
                preferences[ApiPreferencesKeys.ACCESS_TOKEN_EXPIRATION_TIMESTAMP] ?: 0L
            val lastSyncTimestamp = preferences[ApiPreferencesKeys.LAST_SYNC_TIMESTAMP] ?: 0L

            ApiPreferences(
                googleAccessToken = accessToken,
                googleRefreshToken = refreshToken,
                accessTokenExpirationTimestamp = expirationTimestamp,
                lastSyncTimestamp = lastSyncTimestamp,
                googleSyncToken = syncToken
            )
        }

    override suspend fun updateGoogleAuthTokens(
        accessToken: String?,
        refreshToken: String?,
        expirationTimestamp: Long
    ) {
        dataStore.edit { preferences ->
            encryptAndSetString(preferences, ApiPreferencesKeys.GOOGLE_ACCESS_TOKEN, accessToken)
            encryptAndSetString(preferences, ApiPreferencesKeys.GOOGLE_REFRESH_TOKEN, refreshToken)
            preferences[ApiPreferencesKeys.ACCESS_TOKEN_EXPIRATION_TIMESTAMP] = expirationTimestamp
        }
    }

    override suspend fun updateLastFullSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[ApiPreferencesKeys.LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    override suspend fun updateGoogleSyncToken(syncToken: String?) {
        dataStore.edit { preferences ->
            encryptAndSetString(preferences, ApiPreferencesKeys.GOOGLE_SYNC_TOKEN, syncToken)
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun encryptString(value: String?): String? {
        if (value == null) return null
        val bytes = value.encodeToByteArray()
        val encryptedBytes = Crypto.encrypt(bytes)
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    private fun decryptString(encryptedValue: String?): String? {
        if (encryptedValue == null) return null
        return try {
            val encryptedBytes = Base64.decode(encryptedValue, Base64.DEFAULT)
            val decryptedBytes = Crypto.decrypt(encryptedBytes)
            decryptedBytes.decodeToString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun encryptAndSetString(
        preferences: MutablePreferences,
        key: Preferences.Key<String>,
        value: String?
    ) {
        val encryptedValue = encryptString(value)
        if (encryptedValue != null) {
            preferences[key] = encryptedValue
        } else {
            preferences.remove(key)
        }
    }
}