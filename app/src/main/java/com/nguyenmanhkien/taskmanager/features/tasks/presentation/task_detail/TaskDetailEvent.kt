package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_detail

sealed class TaskDetailEvent {
	data object ToggleMenu : TaskDetailEvent()
	data object DismissMenu : TaskDetailEvent()
	data object ToggleTaskCompletion : TaskDetailEvent()
	data object DuplicateTask : TaskDetailEvent()
	data object RequestTaskDeletion : TaskDetailEvent()
	data object CancelTaskDeletion : TaskDetailEvent()
	data object ConfirmTaskDeletion : TaskDetailEvent()

	data object ToggleCategoryPicker : TaskDetailEvent()
	data class SelectCategory(val categoryId: Int?) : TaskDetailEvent()

	data class UpdateTitle(val value: String) : TaskDetailEvent()
	data object CommitTitle : TaskDetailEvent()

	data class ToggleSubtaskCompletion(val subtaskId: Int) : TaskDetailEvent()
	data class UpdateSubtaskTitle(val subtaskId: Int, val value: String) : TaskDetailEvent()
	data class CommitSubtaskTitle(val subtaskId: Int) : TaskDetailEvent()
	data class SubmitSubtaskOnNext(val subtaskId: Int) : TaskDetailEvent()
	data class DeleteSubtask(val subtaskId: Int) : TaskDetailEvent()
	data object AddSubtask : TaskDetailEvent()

	data class ShowPickerDialog(val section: TaskDetailPickerSection) : TaskDetailEvent()
	data object HidePickerDialog : TaskDetailEvent()
	data class SelectDateAtStartOfDay(val timestamp: Long) : TaskDetailEvent()
	data class SelectTimeOffset(val offsetMillis: Long) : TaskDetailEvent()
	data class SelectDuration(val durationMillis: Long) : TaskDetailEvent()
	data class SetRecurrence(val recurrenceJson: String?) : TaskDetailEvent()

	data object OpenDescriptionEditor : TaskDetailEvent()
	data class UpdateDescription(val value: String) : TaskDetailEvent()
	data object SaveDescriptionAndCloseEditor : TaskDetailEvent()

	data object ClearPendingSubtaskFocus : TaskDetailEvent()
}
