package com.nguyenmanhkien.taskmanager.features.tasks.domain.repository

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.SyncStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskWithSubtasks
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getFilteredTasks(
        categoryId: Int? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        searchQuery: String? = null
    ): Flow<List<Task>>

    fun getTaskWithSubtasks(taskId: Int): Flow<TaskWithSubtasks?>

    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>

    fun getTasksByPriority(priority: TaskPriority): Flow<List<Task>>

    fun getTasksBySyncStatus(syncStatus: SyncStatus): Flow<List<Task>>


    fun getDaysWithTasks(startTime: Long, endTime: Long): Flow<List<Long>>

    suspend fun getSubtasksOnce(taskId: Int): List<Task>

    suspend fun getTaskById(id: Int): Task?

    suspend fun getTaskByGoogleId(googleId: Int): Task?

    suspend fun getOverdueInProgressIds(currentTime: Long): List<Int>

    suspend fun upsertTask(task: Task): Long

    suspend fun upsertTasks(tasks: List<Task>)

    suspend fun deleteTask(task: Task)

    suspend fun deleteTasks(tasks: List<Task>)

    suspend fun updateStatus(id: Int, status: TaskStatus)

    suspend fun updateStatuses(ids: List<Int>, status: TaskStatus)

    suspend fun updateStatusWithCompletionDate(id: Int, status: TaskStatus, completionDate: Long?)

    suspend fun updateStatusesWithCompletionDate(ids: List<Int>, status: TaskStatus, completionDate: Long?)

    suspend fun updateTaskAndSubtasksStatus(parentId: Int, status: TaskStatus, completionDate: Long?)

    suspend fun updatePriority(id: Int, priority: TaskPriority)

    suspend fun updateCategory(id: Int, categoryId: Int?)

    suspend fun updateCategories(ids: List<Int>, categoryId: Int?)

    suspend fun updateSyncStatus(id: Int, syncStatus: SyncStatus)

    suspend fun updateSyncStatuses(ids: List<Int>, syncStatus: SyncStatus)

    suspend fun updateTasksAndSubtasksSyncStatus(parentIds: List<Int>, syncStatus: SyncStatus)
}
