package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskWithSubtasks
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetTaskDetail(private val repository: TaskRepository) {
    operator fun invoke(taskId: Int): Flow<TaskWithSubtasks?> {
        return repository.getTaskWithSubtasks(taskId)
    }
}
