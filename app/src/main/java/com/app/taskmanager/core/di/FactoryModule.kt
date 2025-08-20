package com.app.taskmanager.core.di

import android.content.Context
import androidx.room.Room
import com.app.taskmanager.core.data.db.AppDatabase
import com.app.taskmanager.core.data.db.seed.DatabaseCallback
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@ComponentScan("com.app.taskmanager")
@Module
class FactoryModule {
    @Single
    fun createRoomDatabase(
        context: Context,
        databaseCallback: DatabaseCallback
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME)
            .addCallback(databaseCallback)
            .build()
    }
}