package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog

import androidx.compose.ui.focus.FocusState
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority

sealed class TaskDialogEvent {
    // Text field events
    data class EnterTitle(val value: String) : TaskDialogEvent()
    data class ChangeTitleFocus(val focusState: FocusState) : TaskDialogEvent()

    // Task property events
    data class SetInitialCategoryId(val categoryId: Int) : TaskDialogEvent()
    data class SetInitialDate(val timestamp: Long) : TaskDialogEvent()
    data class SelectPriority(val priority: TaskPriority) : TaskDialogEvent()
    data class SelectCategory(val categoryId: Int?) : TaskDialogEvent()
    data object ToggleCategoryPicker : TaskDialogEvent()
    data class SelectDateAtStartOfDay(val timestamp: Long?) : TaskDialogEvent()
    data class SelectTimeOffset(val offsetMillis: Long?) : TaskDialogEvent()
    data class SelectDuration(val durationMillis: Long?) : TaskDialogEvent()
    data class SetRecurrence(val rrule: String?) : TaskDialogEvent()

    // Subtask events
    data object AddSubtask : TaskDialogEvent()
    data class RemoveSubtask(val index: Int) : TaskDialogEvent()
    data class UpdateSubtaskText(val index: Int, val text: String) : TaskDialogEvent()
    data class ChangeSubtaskFocus(val index: Int, val focusState: FocusState) : TaskDialogEvent()

    // Dialog actions
    data object SaveTask : TaskDialogEvent()
    data object DismissDialog : TaskDialogEvent()
}