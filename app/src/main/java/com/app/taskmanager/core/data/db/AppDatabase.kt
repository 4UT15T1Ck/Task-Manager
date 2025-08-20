package com.app.taskmanager.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.taskmanager.core.data.db.dao.ArchivedTaskDao
import com.app.taskmanager.core.data.db.dao.CategoryDao
import com.app.taskmanager.core.data.db.dao.ReminderDao
import com.app.taskmanager.core.data.db.dao.SubtaskDao
import com.app.taskmanager.core.data.db.dao.TaskDao
import com.app.taskmanager.core.domain.models.ArchivedTask
import com.app.taskmanager.core.domain.models.Category
import com.app.taskmanager.core.domain.models.Reminder
import com.app.taskmanager.core.domain.models.Subtask
import com.app.taskmanager.core.domain.models.Task

@Database(
    entities = [
        Task::class,
        ArchivedTask::class,
        Category::class,
        Reminder::class,
        Subtask::class,
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun archivedTaskDao(): ArchivedTaskDao

    companion object {
        const val DB_NAME = "taskmanager.db"
}}