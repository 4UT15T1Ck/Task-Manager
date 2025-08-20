package com.app.taskmanager.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiPreferences(
    val googleRefreshToken: String?,
    val googleAccessToken: String?,
    val googleSyncToken: String?,
    val accessTokenExpirationTimestamp: Long,
    val lastSyncTimestamp: Long
)
