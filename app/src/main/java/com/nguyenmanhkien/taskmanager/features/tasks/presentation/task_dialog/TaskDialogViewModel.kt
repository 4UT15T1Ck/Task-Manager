package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.TaskUseCases
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TaskDialogViewModel(
    private val taskUseCases: TaskUseCases,
) : ViewModel() {

    private val _dialogState = mutableStateOf(TaskDialogState())
    val dialogState: State<TaskDialogState> = _dialogState

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        getCategories()
    }

    fun setInitialCategoryId(categoryId: Int?) {
        _dialogState.value = dialogState.value.copy(categoryId = categoryId)
    }

    fun onEvent(event: TaskDialogEvent) {
        when (event) {
            is TaskDialogEvent.EnterTitle -> handleEnterTitle(event)
            is TaskDialogEvent.ChangeTitleFocus -> handleChangeTitleFocus(event)
            is TaskDialogEvent.SelectPriority -> handleSelectPriority(event)
            is TaskDialogEvent.SelectCategory -> handleSelectCategory(event)
            is TaskDialogEvent.ToggleCategoryPicker -> handleToggleCategoryPicker()
            is TaskDialogEvent.SelectStartDate -> handleSelectStartDate(event)
            is TaskDialogEvent.SelectDueDate -> handleSelectDueDate(event)
            is TaskDialogEvent.SetRecurrence -> handleSetRecurrence(event)
            is TaskDialogEvent.AddSubtask -> handleAddSubtask()
            is TaskDialogEvent.RemoveSubtask -> handleRemoveSubtask(event)
            is TaskDialogEvent.UpdateSubtaskText -> handleUpdateSubtaskText(event)
            is TaskDialogEvent.ChangeSubtaskFocus -> handleChangeSubtaskFocus(event)
            is TaskDialogEvent.SaveTask -> saveTask()
            is TaskDialogEvent.DismissDialog -> handleDismissDialog()
        }
    }

    private fun clearState() {
        _dialogState.value = TaskDialogState(categories = dialogState.value.categories)
    }

    private fun handleEnterTitle(event: TaskDialogEvent.EnterTitle) {
        _dialogState.value = dialogState.value.copy(
            title = dialogState.value.title.copy(text = event.value)
        )
    }

    private fun handleChangeTitleFocus(event: TaskDialogEvent.ChangeTitleFocus) {
        _dialogState.value = dialogState.value.copy(
            title = dialogState.value.title.copy(
                isHintVisible = !event.focusState.isFocused || dialogState.value.title.text.isBlank()
            )
        )
    }


    private fun handleSelectPriority(event: TaskDialogEvent.SelectPriority) {
        _dialogState.value = dialogState.value.copy(priority = event.priority)
    }

    private fun handleSelectCategory(event: TaskDialogEvent.SelectCategory) {
        _dialogState.value = dialogState.value.copy(
            categoryId = event.categoryId,
            showCategoryPicker = false
        )
    }

    private fun handleToggleCategoryPicker() {
        _dialogState.value = dialogState.value.copy(
            showCategoryPicker = !dialogState.value.showCategoryPicker
        )
    }

    private fun handleSelectStartDate(event: TaskDialogEvent.SelectStartDate) {
        _dialogState.value = dialogState.value.copy(startDate = event.timestamp)
    }

    private fun handleSelectDueDate(event: TaskDialogEvent.SelectDueDate) {
        _dialogState.value = dialogState.value.copy(dueDate = event.timestamp)
    }

    private fun handleSetRecurrence(event: TaskDialogEvent.SetRecurrence) {
        _dialogState.value = dialogState.value.copy(rrule = event.rrule)
    }

    private fun handleAddSubtask() {
        if (dialogState.value.subtasks.size >= 20) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackBar("Too many subtasks! Add a continued task for better management instead."))
            }
            return
        }
        _dialogState.value = dialogState.value.copy(
            subtasks = dialogState.value.subtasks + TaskTextFieldState(hint = "Subtask")
        )
    }

    private fun handleRemoveSubtask(event: TaskDialogEvent.RemoveSubtask) {
        _dialogState.value = dialogState.value.copy(
            subtasks = dialogState.value.subtasks.filterIndexed { index, _ ->
                index != event.index
            }
        )
    }

    private fun handleUpdateSubtaskText(event: TaskDialogEvent.UpdateSubtaskText) {
        _dialogState.value = dialogState.value.copy(
            subtasks = dialogState.value.subtasks.mapIndexed { index, subtask ->
                if (index == event.index) {
                    subtask.copy(text = event.text)
                } else subtask
            }
        )
    }

    private fun handleChangeSubtaskFocus(event: TaskDialogEvent.ChangeSubtaskFocus) {
        _dialogState.value = dialogState.value.copy(
            subtasks = dialogState.value.subtasks.mapIndexed { index, subtask ->
                if (index == event.index) {
                    subtask.copy(
                        isHintVisible = !event.focusState.isFocused || subtask.text.isBlank()
                    )
                } else subtask
            }
        )
    }

    private fun handleDismissDialog() {
        clearState()
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.DismissDialog)
        }
    }

    private fun saveTask() {
        viewModelScope.launch {
            if (dialogState.value.title.text.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackBar("Task title is required"))
                return@launch
            }

            try {
                // If dueDate is null, set it to 23:59:59 today
                val currentTime = System.currentTimeMillis()
                val finalDueDate = dialogState.value.dueDate ?: run {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = currentTime
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                    calendar.set(java.util.Calendar.MINUTE, 59)
                    calendar.set(java.util.Calendar.SECOND, 59)
                    calendar.set(java.util.Calendar.MILLISECOND, 999)
                    calendar.timeInMillis
                }

                val task = Task(
                    title = dialogState.value.title.text,
                    description = null,
                    startAt = dialogState.value.startDate ?: currentTime,
                    dueAt = finalDueDate,
                    categoryId = dialogState.value.categoryId,
                    priority = dialogState.value.priority,
                    rrule = dialogState.value.rrule,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )

                taskUseCases.taskCRUD.upsertTask(task)

                // Clear state after saving
                clearState()

                _eventFlow.emit(UiEvent.DismissDialog)
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackBar(e.message ?: "Failed to save task"))
            }
        }
    }

    private fun getCategories() {
        viewModelScope.launch {
            taskUseCases.category.getCategories().collect { categories ->
                _dialogState.value = dialogState.value.copy(categories = categories)
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackBar(val message: String) : UiEvent()
        data object DismissDialog : UiEvent()
    }
}
