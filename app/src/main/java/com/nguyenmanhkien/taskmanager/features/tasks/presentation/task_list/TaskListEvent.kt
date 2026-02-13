package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TimeFilter
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.OrderType
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskOrderField

sealed class TaskListEvent {
    data class FilterTaskByTime(val timeFilter: TimeFilter) : TaskListEvent()
    data class SortTask(val taskOrderField: TaskOrderField, val orderType: OrderType) : TaskListEvent()
    data class FilterByCategory(val category: Category?) : TaskListEvent()
    data class SearchTask(val searchString: String) : TaskListEvent()
    data class ToggleTaskCompletion(val task: Task) : TaskListEvent()
    data class SetTaskPriority(val taskId: Int, val priority: TaskPriority) : TaskListEvent()
    data object TogglePendingTasksVisibility : TaskListEvent()
    data object ToggleCompletedTasksVisibility : TaskListEvent()
}