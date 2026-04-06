package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_detail

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskWithSubtasks

data class TaskDetailState(
	val taskId: Int = -1,
	val taskWithSubtasks: TaskWithSubtasks? = null,
	val categories: List<Category> = emptyList(),
	val isMenuExpanded: Boolean = false,
	val isCategoryPickerVisible: Boolean = false,
	val activePickerSection: TaskDetailPickerSection? = null,
	val isDescriptionEditorVisible: Boolean = false,
	val titleInput: String = "",
	val subtaskInputs: Map<Int, String> = emptyMap(),
	val descriptionInput: String = "",
	val pendingSubtaskFocusId: Int? = null
)

enum class TaskDetailPickerSection {
	TIME,
	DATE,
	DURATION,
	REMINDER,
	REPEAT
}

