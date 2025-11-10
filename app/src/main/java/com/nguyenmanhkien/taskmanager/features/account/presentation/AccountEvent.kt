package com.nguyenmanhkien.taskmanager.features.account.presentation

import android.content.Context

sealed class AccountEvent {
    data class LoginEvent(val context: Context) : AccountEvent()
    data object LogoutEvent : AccountEvent()
}