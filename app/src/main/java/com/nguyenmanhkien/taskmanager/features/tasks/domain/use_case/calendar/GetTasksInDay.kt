package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.calendar

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetTasksInDay(
	private val taskRepository: TaskRepository
) {
	operator fun invoke(dayStartTime: Long, dayEndTime: Long): Flow<List<Task>> {
		return taskRepository.getParentTasksInRange(dayStartTime, dayEndTime)
	}
}