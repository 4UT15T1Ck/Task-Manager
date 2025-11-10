package com.nguyenmanhkien.taskmanager.features.account.domain.usecase

import android.content.Context
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialException
import com.nguyenmanhkien.taskmanager.BuildConfig
import com.nguyenmanhkien.taskmanager.core.domain.repository.ApiPreferencesRepository
import com.nguyenmanhkien.taskmanager.features.account.data.remote.AuthService
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.nguyenmanhkien.taskmanager.core.domain.repository.UserPreferencesRepository
import org.koin.core.annotation.Factory
import java.security.SecureRandom

sealed class LoginResult {
    data object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
}

@Factory
class LoginUseCase(
    private val credentialManager: CredentialManager,
    private val authService: AuthService,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val apiPreferencesRepository: ApiPreferencesRepository
) {
    suspend operator fun invoke(context: Context): LoginResult {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
            .setNonce(generateNonce())
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            handleSignInSuccess(result)
        } catch (e: GetCredentialException) {
            LoginResult.Error(e.message ?: "An unknown error occurred during login.")
        }
    }

    private suspend fun handleSignInSuccess(result: GetCredentialResponse): LoginResult {
        return when (val credential = result.credential) {
            is GoogleIdTokenCredential -> {
                userPreferencesRepository.updateUserProfile(
                    email = credential.id,
                    displayName = credential.displayName,
                    photoUrl = credential.profilePictureUri?.toString()
                )
                val serverAuthCode =
                    credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_SERVER_AUTH_CODE")
                if (serverAuthCode != null) {
                    exchangeAuthCodeForTokens(serverAuthCode)
                } else {
                    LoginResult.Error("Server Auth Code was null.")
                }
            }

            else -> LoginResult.Error("Unrecognized credential type.")
        }
    }

    private suspend fun exchangeAuthCodeForTokens(authCode: String): LoginResult {
        val tokenResponse = authService.exchangeCodeForTokens(authCode)
        return if (tokenResponse != null) {
            val expirationTimestamp = System.currentTimeMillis() + (tokenResponse.expiresIn * 1000)
            apiPreferencesRepository.updateGoogleAuthTokens(
                accessToken = tokenResponse.accessToken,
                refreshToken = tokenResponse.refreshToken,
                expirationTimestamp = expirationTimestamp
            )
            LoginResult.Success
        } else {
            LoginResult.Error("Failed to exchange auth code for tokens.")
        }
    }

    private fun generateNonce(length: Int = 16): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}