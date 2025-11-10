package com.nguyenmanhkien.taskmanager.features.account.di

import android.content.Context
import androidx.credentials.CredentialManager
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AccountFactoryModule {
    @Single
    fun provideCredentialManager(context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

}
