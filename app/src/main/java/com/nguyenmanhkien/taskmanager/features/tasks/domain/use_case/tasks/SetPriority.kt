package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import org.koin.core.annotation.Factory

@Factory
class SetPriority(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: Int, priority: TaskPriority) {
        repository.updatePriority(taskId, priority)
    }
}