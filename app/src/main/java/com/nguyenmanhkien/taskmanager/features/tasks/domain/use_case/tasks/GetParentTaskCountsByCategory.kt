package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetParentTaskCountsByCategory(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<Map<Int, Int>> = repository.getParentTaskCountsByCategory()
}

