package com.nguyenmanhkien.taskmanager.features.account.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmanhkien.taskmanager.core.domain.repository.UserPreferencesRepository
import com.nguyenmanhkien.taskmanager.features.account.domain.usecase.AccountUseCases
import com.nguyenmanhkien.taskmanager.features.account.domain.usecase.LoginResult
import com.nguyenmanhkien.taskmanager.features.account.domain.usecase.LogoutResult
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AccountViewModel(
    private val accountUseCases: AccountUseCases,
    private val userPreferenceRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = mutableStateOf(AccountState())
    val state: State<AccountState> = _state

//    fun onEvent(event: AccountEvent) {
//        when (event) {
//            is AccountEvent.LoginEvent -> login(event.context)
//            is AccountEvent.LogoutEvent -> logout()
//        }
//    }
//
//    private fun login(context: Context) {
//        viewModelScope.launch {
//            _state.value = state.value.copy(loading = true)
//        }
//        viewModelScope.launch {
//            when (val result = accountUseCases.loginUseCase(context)) {
//                is LoginResult.Success -> {
//                    _state.value = state.value.copy(loading = false, success = true)
//                }
//
//                is LoginResult.Error -> {
//                    _state.value = state.value.copy(loading = false, error = result.message)
//                }
//            }
//        }
//    }
//
//    private fun logout() {
//        viewModelScope.launch {
//            when (val result = accountUseCases.logoutUseCase()) {
//                is LogoutResult.Success -> {
//                    _state.value = state.value.copy(success = true)
//                }
//                is LogoutResult.Error -> {
//                    _state.value = state.value.copy(error = result.message)
//                }
//            }
//        }
//    }
}
