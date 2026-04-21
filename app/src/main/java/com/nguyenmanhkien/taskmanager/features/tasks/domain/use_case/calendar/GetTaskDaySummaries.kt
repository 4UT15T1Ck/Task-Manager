package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.calendar

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.DayTaskSummary
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetTaskDaySummaries(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(startTime: Long, endTime: Long): Flow<List<DayTaskSummary>> {
        return taskRepository.getDayTaskSummariesInRange(startTime, endTime)
    }
}

