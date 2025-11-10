package com.nguyenmanhkien.taskmanager.features.account.data.remote

import com.nguyenmanhkien.taskmanager.BuildConfig
import com.nguyenmanhkien.taskmanager.features.account.data.remote.dto.GoogleTokenResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import org.koin.core.annotation.Single

@Single
class AuthServiceImpl(private val client: HttpClient) : AuthService {

    private val TOKEN_URL = "https://oauth2.googleapis.com/token"

    override suspend fun exchangeCodeForTokens(authCode: String): GoogleTokenResponseDto? {
        return try {
            client.post(TOKEN_URL) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build {
                    append("grant_type", "authorization_code")
                    append("client_id", BuildConfig.GOOGLE_CLIENT_ID)
                    append("code", authCode)
                }))
            }.body()
        } catch (e: Exception) {
            println("Error exchanging auth code for tokens: ${e.message}")
            null
        }
    }
}