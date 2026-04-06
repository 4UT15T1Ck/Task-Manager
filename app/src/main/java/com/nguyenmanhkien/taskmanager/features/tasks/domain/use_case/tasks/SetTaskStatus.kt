package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import org.koin.core.annotation.Factory

@Factory
class SetTaskStatus(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: Int, status: TaskStatus) {
        repository.updateStatusWithCompletionDate(taskId, status, completionDateFor(status))
    }

    suspend operator fun invoke(taskIds: List<Int>, status: TaskStatus) {
        if (taskIds.isEmpty()) {
            return
        }
        repository.updateStatusesWithCompletionDate(taskIds, status, completionDateFor(status))
    }

    private fun completionDateFor(status: TaskStatus): Long? {
        return if (status == TaskStatus.COMPLETED) System.currentTimeMillis() else null
    }
}
