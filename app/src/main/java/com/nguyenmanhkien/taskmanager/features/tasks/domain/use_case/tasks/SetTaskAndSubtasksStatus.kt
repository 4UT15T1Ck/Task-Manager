package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import org.koin.core.annotation.Factory

@Factory
class SetTaskAndSubtasksStatus(private val repository: TaskRepository) {
    suspend operator fun invoke(parentTaskId: Int, status: TaskStatus) {
        val completionDate = if (status == TaskStatus.COMPLETED) {
            System.currentTimeMillis()
        } else {
            null
        }
        repository.updateTaskAndSubtasksStatus(parentTaskId, status, completionDate)
    }
}

