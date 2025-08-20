package com.app.taskmanager

import android.app.Application
import com.app.taskmanager.core.di.FactoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.*


class TaskManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TaskManagerApp)
            modules(FactoryModule().module)
        }
    }
}