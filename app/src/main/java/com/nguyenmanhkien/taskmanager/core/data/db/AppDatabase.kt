package com.nguyenmanhkien.taskmanager.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao.ArchivedTaskDao
import com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao.CategoryDao
import com.nguyenmanhkien.taskmanager.features.reminders.data.dao.ReminderDao
import com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao.TaskDao
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.ArchivedTask
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.reminders.domain.model.Reminder
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task

@Database(
    entities = [
        Task::class,
        ArchivedTask::class,
        Category::class,
        Reminder::class,
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun archivedTaskDao(): ArchivedTaskDao

    companion object {
        const val DB_NAME = "taskmanager.db"
}}