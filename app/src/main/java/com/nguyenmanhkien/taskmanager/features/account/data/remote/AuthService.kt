package com.nguyenmanhkien.taskmanager.features.account.data.remote

import com.nguyenmanhkien.taskmanager.features.account.data.remote.dto.GoogleTokenResponseDto

interface AuthService {
    suspend fun exchangeCodeForTokens(authCode: String): GoogleTokenResponseDto?
}