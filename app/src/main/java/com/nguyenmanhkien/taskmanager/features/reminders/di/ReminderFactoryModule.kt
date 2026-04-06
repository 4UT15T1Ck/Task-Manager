package com.nguyenmanhkien.taskmanager.features.reminders.di

import com.nguyenmanhkien.taskmanager.core.data.db.AppDatabase
import com.nguyenmanhkien.taskmanager.features.reminders.data.dao.ReminderDao
import com.nguyenmanhkien.taskmanager.features.reminders.data.repository.ReminderRepositoryImpl
import com.nguyenmanhkien.taskmanager.features.reminders.domain.repository.ReminderRepository
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@ComponentScan("com.nguyenmanhkien.taskmanager.features.reminders")
@Module
class ReminderFactoryModule {

    @Single
    fun provideReminderDao(database: AppDatabase): ReminderDao {
        return database.reminderDao()
    }

    @Single
    fun provideReminderRepository(reminderDao: ReminderDao): ReminderRepository {
        return ReminderRepositoryImpl(reminderDao)
    }
}
