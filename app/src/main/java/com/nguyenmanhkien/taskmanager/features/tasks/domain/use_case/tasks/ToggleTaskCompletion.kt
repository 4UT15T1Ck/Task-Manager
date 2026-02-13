package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import org.koin.core.annotation.Factory

@Factory
class ToggleTaskCompletion(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        val newStatus = when (task.status) {
            TaskStatus.IN_PROGRESS -> TaskStatus.COMPLETED
            TaskStatus.COMPLETED -> TaskStatus.IN_PROGRESS
        }
        repository.updateStatus(task.id, newStatus)
    }
}