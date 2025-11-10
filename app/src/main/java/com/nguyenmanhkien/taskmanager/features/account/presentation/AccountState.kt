package com.nguyenmanhkien.taskmanager.features.account.presentation

enum class AccountScreenStatus {
    LOADING,
    SUCCESS,
    ERROR,
    IDLE
}

data class ProfileInfo (
    val message: String? = null,
    val email: String? = null,
    val displayName: String? = null,
)

data class AccountState(
    val status: AccountScreenStatus = AccountScreenStatus.IDLE,
    val profileInfo: ProfileInfo? = null,
    val photoUrl: String? = null
)