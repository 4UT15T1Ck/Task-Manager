package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.SyncStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import org.koin.core.annotation.Factory

@Factory
class DeleteTask(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        repository.updateSyncStatus(task.id, SyncStatus.DELETED)
    }
}