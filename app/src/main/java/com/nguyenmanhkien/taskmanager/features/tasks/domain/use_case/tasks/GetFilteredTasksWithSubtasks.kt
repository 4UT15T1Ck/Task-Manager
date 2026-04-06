package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import com.nguyenmanhkien.taskmanager.features.reminders.domain.repository.ReminderRepository
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskWithSubtasks
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
class GetFilteredTasksWithSubtasks(
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository
) {
    operator fun invoke(
        categoryId: Int? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        searchQuery: String? = null
    ): Flow<List<TaskWithSubtasks>> {
        return taskRepository.getFilteredTasks(
            categoryId = categoryId,
            startTime = startTime,
            endTime = endTime,
            searchQuery = searchQuery
        ).flatMapLatest { tasks ->
            if (tasks.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(tasks.map { task ->
                    taskRepository.getTaskWithSubtasks(task.id)
                        .combine(reminderRepository.getRemindersForTask(task.id)) { taskWithSubtasks, reminders ->
                            taskWithSubtasks?.copy(hasReminder = reminders.isNotEmpty())
                        }
                }) { items ->
                    items.filterNotNull()
                }
            }
        }
    }
}
