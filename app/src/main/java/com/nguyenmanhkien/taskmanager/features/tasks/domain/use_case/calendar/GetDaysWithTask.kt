package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.calendar

import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetDaysWithTask(
	private val taskRepository: TaskRepository
) {
	operator fun invoke(startTime: Long, endTime: Long): Flow<List<Long>> {
		return taskRepository.getDaysWithTasks(startTime, endTime)
	}
}