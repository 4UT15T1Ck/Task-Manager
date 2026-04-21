package com.nguyenmanhkien.taskmanager.features.tasks.data.local.repository

import com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao.TaskDao
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.DayTaskSummary
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.SyncStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskWithSubtasks
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val dao: TaskDao
) : TaskRepository {

    override fun getParentTaskCountsByCategory(): Flow<Map<Int, Int>> {
        return dao.getParentTaskCountsByCategory()
            .map { counts -> counts.associate { it.categoryId to it.taskCount } }
    }

    override fun getFilteredTasks(
        categoryId: Int?,
        startTime: Long?,
        endTime: Long?,
        searchQuery: String?
    ): Flow<List<Task>> = dao.getFilteredTasks(categoryId, startTime, endTime, searchQuery)

    override fun getTaskWithSubtasks(taskId: Int): Flow<TaskWithSubtasks?> {
        return dao.getTaskByIdFlow(taskId)
            .combine(dao.getSubtasksForTaskFlow(taskId)) { task, subtasks ->
                if (task == null) null
                else TaskWithSubtasks(task, subtasks)
            }
    }

    override fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> =
        dao.getTasksByStatus(status)

    override fun getTasksByPriority(priority: TaskPriority): Flow<List<Task>> =
        dao.getTasksByPriority(priority)

    override fun getTasksBySyncStatus(syncStatus: SyncStatus): Flow<List<Task>> =
        dao.getTasksBySyncStatus(syncStatus)


    override fun getDaysWithTasks(startTime: Long, endTime: Long): Flow<List<Long>> =
        dao.getDaysWithTasksInRange(startTime, endTime)

    override fun getParentTasksInRange(startTime: Long, endTime: Long): Flow<List<Task>> =
        dao.getParentTasksInRange(startTime, endTime)

    override fun getDayTaskSummariesInRange(startTime: Long, endTime: Long): Flow<List<DayTaskSummary>> =
        dao.getDayTaskSummariesInRange(startTime, endTime)

    override suspend fun getSubtasksOnce(taskId: Int): List<Task> =
        dao.getSubtasksForTaskOnce(taskId)

    override suspend fun getTaskById(id: Int): Task? = dao.getTaskById(id)

    override suspend fun getTaskByGoogleId(googleId: Int): Task? =
        dao.getTaskByGoogleCalendarEventId(googleId)

    override suspend fun getOverdueInProgressIds(currentTime: Long): List<Int> =
        dao.getOverdueInProgressTasks(currentTime)

    override suspend fun upsertTask(task: Task): Long = dao.upsertTask(task)

    override suspend fun upsertTasks(tasks: List<Task>) = dao.upsertTasks(tasks)

    override suspend fun deleteTask(task: Task) = dao.deleteTask(task)

    override suspend fun deleteTasks(tasks: List<Task>) = dao.deleteTasks(tasks)

    override suspend fun updateStatus(id: Int, status: TaskStatus) = dao.setTaskStatus(id, status)

    override suspend fun updateStatuses(ids: List<Int>, status: TaskStatus) {
        if (ids.isEmpty()) {
            return
        }
        dao.setTasksStatus(ids, status)
    }

    override suspend fun updateStatusWithCompletionDate(id: Int, status: TaskStatus, completionDate: Long?) {
        dao.setTaskStatusWithCompletionDate(id, status, completionDate)
    }

    override suspend fun updateStatusesWithCompletionDate(ids: List<Int>, status: TaskStatus, completionDate: Long?) {
        if (ids.isEmpty()) {
            return
        }
        dao.setTasksStatusWithCompletionDate(ids, status, completionDate)
    }

    override suspend fun updateTaskAndSubtasksStatus(parentId: Int, status: TaskStatus, completionDate: Long?) {
        dao.setTaskAndSubtasksStatusWithCompletionDate(parentId, status, completionDate)
    }

    override suspend fun updatePriority(id: Int, priority: TaskPriority) = dao.setTaskPriority(id, priority)

    override suspend fun updateCategory(id: Int, categoryId: Int?) = dao.setTaskCategory(id, categoryId)

    override suspend fun updateCategories(ids: List<Int>, categoryId: Int?) {
        if (ids.isEmpty()) {
            return
        }
        dao.setTasksCategory(ids, categoryId)
    }

    override suspend fun updateSyncStatus(id: Int, syncStatus: SyncStatus) =
        dao.setTaskSyncStatus(id, syncStatus)

    override suspend fun updateSyncStatuses(ids: List<Int>, syncStatus: SyncStatus) {
        if (ids.isEmpty()) {
            return
        }
        dao.setTasksSyncStatus(ids, syncStatus)
    }

    override suspend fun updateTasksAndSubtasksSyncStatus(parentIds: List<Int>, syncStatus: SyncStatus) {
        dao.setTasksAndSubtasksSyncStatus(parentIds, syncStatus)
    }
}
