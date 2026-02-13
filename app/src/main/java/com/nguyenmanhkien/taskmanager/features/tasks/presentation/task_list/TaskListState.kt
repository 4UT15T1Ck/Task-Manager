package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TimeFilter
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.OrderType
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskOrderField

data class TaskListState(
    val tasks: List<Task> = emptyList(),
    val categories: List<Category> = emptyList(),
    val chosenCategory: Category? = null,
    val taskOrderField: TaskOrderField = TaskOrderField.DUE_DATE,
    val orderType: OrderType = OrderType.DESCENDING,
    val searchString: String = "",
    val timeFilter: TimeFilter = TimeFilter.TODAY,
    val isPendingTasksVisible: Boolean = true,
    val isCompletedTasksVisible: Boolean = true
)