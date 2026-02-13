package com.nguyenmanhkien.taskmanager.core.data.db

import androidx.room.TypeConverter
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.SyncStatus
import com.nguyenmanhkien.taskmanager.features.reminders.domain.model.ReminderStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus

class Converters {
    @TypeConverter
    fun fromReminderStatus(status: ReminderStatus?): String? = status?.name

    @TypeConverter
    fun toReminderStatus(name: String?): ReminderStatus? = name?.let { ReminderStatus.valueOf(it) }

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus?): String? = status?.name

    @TypeConverter
    fun toTaskStatus(name: String?): TaskStatus? = name?.let { TaskStatus.valueOf(it) }

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority?): String? = priority?.name

    @TypeConverter
    fun toTaskPriority(name: String?): TaskPriority? = name?.let { TaskPriority.valueOf(it) }

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus?): String? = status?.name

    @TypeConverter
    fun toSyncStatus(name: String?): SyncStatus? = name?.let { SyncStatus.valueOf(it) }
}