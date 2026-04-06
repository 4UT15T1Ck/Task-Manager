package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import org.koin.core.annotation.Factory

@Factory
class SetTaskCategory(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: Int, categoryId: Int?) {
        repository.updateCategory(taskId, categoryId)
    }

    suspend operator fun invoke(taskIds: List<Int>, categoryId: Int?) {
        if (taskIds.isEmpty()) {
            return
        }
        repository.updateCategories(taskIds, categoryId)
    }
}
