package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority

data class TaskDialogState(
    val title: TaskTextFieldState = TaskTextFieldState(hint = "New Task"),
    val subtasks: List<TaskTextFieldState> = emptyList(),
    val categories: List<Category> = emptyList(),
    val categoryId: Int? = null,
    val showCategoryPicker: Boolean = false,
    val dateAtStartOfDay: Long? = null,
    val timeOffsetMillis: Long? = null,
    val durationMillis: Long? = null,
    val priority: TaskPriority = TaskPriority.NO_PRIORITY,
    val rrule: String? = null,
)