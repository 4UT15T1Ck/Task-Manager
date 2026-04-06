package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.SyncStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import org.koin.core.annotation.Factory

@Factory
class BulkDeleteTasks(private val repository: TaskRepository) {
    suspend operator fun invoke(taskIds: List<Int>) {
        if (taskIds.isEmpty()) {
            return
        }
        repository.updateTasksAndSubtasksSyncStatus(taskIds, SyncStatus.DELETED)
    }
}
