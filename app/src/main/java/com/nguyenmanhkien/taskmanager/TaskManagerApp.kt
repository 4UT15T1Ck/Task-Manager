package com.nguyenmanhkien.taskmanager

import android.app.Application
import com.nguyenmanhkien.taskmanager.core.di.CoreFactoryModule
import com.nguyenmanhkien.taskmanager.features.account.di.AccountFactoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.*


class TaskManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TaskManagerApp)
            modules(
                CoreFactoryModule().module,
                AccountFactoryModule().module,
            )
        }
    }
}