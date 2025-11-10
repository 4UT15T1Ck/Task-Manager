package com.nguyenmanhkien.taskmanager.features.account.domain.usecase

import org.koin.core.annotation.Single

@Single
data class AccountUseCases (
    val loginUseCase: LoginUseCase,
    val logoutUseCase: LogoutUseCase
)