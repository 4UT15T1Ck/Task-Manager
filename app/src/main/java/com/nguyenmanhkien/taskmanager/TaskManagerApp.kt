package com.nguyenmanhkien.taskmanager

import android.app.Application
import com.nguyenmanhkien.taskmanager.core.di.CoreFactoryModule
import com.nguyenmanhkien.taskmanager.features.account.di.AccountFactoryModule
import com.nguyenmanhkien.taskmanager.features.tasks.data.local.seed.DatabaseInitializer
import com.nguyenmanhkien.taskmanager.features.tasks.di.TasksFactoryModule
import org.koin.android.ext.android.inject
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
                TasksFactoryModule().module
            )
        }

        val databaseInitializer: DatabaseInitializer by inject()
        databaseInitializer.initialize()
    }
}

