package com.app.taskmanager.core.data.db

import androidx.room.TypeConverter
import com.app.taskmanager.core.domain.models.SyncStatus
import com.app.taskmanager.core.domain.models.ReminderStatus
import com.app.taskmanager.core.domain.models.TaskCompletionType
import com.app.taskmanager.core.domain.models.TaskPriority
import com.app.taskmanager.core.domain.models.TaskStatus

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
    fun fromTaskCompletionType(type: TaskCompletionType?): String? = type?.name
    @TypeConverter
    fun toTaskCompletionType(name: String?): TaskCompletionType? = name?.let { TaskCompletionType.valueOf(it) }

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus?): String? = status?.name

    @TypeConverter
    fun toSyncStatus(name: String?): SyncStatus? = name?.let { SyncStatus.valueOf(it) }
}