package com.nguyenmanhkien.taskmanager.features.account.domain.usecase

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.nguyenmanhkien.taskmanager.core.domain.repository.ApiPreferencesRepository
import com.nguyenmanhkien.taskmanager.core.domain.repository.UserPreferencesRepository
import org.koin.core.annotation.Factory

sealed class LogoutResult {
    data object Success : LogoutResult()
    data class Error(val message: String) : LogoutResult()
}

@Factory
class LogoutUseCase(
    private val credentialManager: CredentialManager,
    private val apiPreferencesRepository: ApiPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(): LogoutResult {
        return try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            apiPreferencesRepository.clear()
            userPreferencesRepository.clearUserProfile()
            LogoutResult.Success
        } catch (e: Exception) {
            println("Error during logout: ${e.message}")
            LogoutResult.Error(e.message ?: "An unknown error occurred during logout.")
        }
    }
}