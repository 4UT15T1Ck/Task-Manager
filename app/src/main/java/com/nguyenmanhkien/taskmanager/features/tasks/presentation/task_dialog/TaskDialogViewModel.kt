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
import java.util.Calendar

@KoinViewModel
class TaskDialogViewModel(
    private val taskUseCases: TaskUseCases,
) : ViewModel() {

    companion object {
        private const val MAX_SUBTASKS = 20
    }

    private val _dialogState = mutableStateOf(TaskDialogState())
    val dialogState: State<TaskDialogState> = _dialogState

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        getCategories()
    }

    fun onEvent(event: TaskDialogEvent) {
        when (event) {
            is TaskDialogEvent.SetInitialCategoryId -> handleSetInitialCategoryId(event)
            is TaskDialogEvent.EnterTitle -> handleEnterTitle(event)
            is TaskDialogEvent.ChangeTitleFocus -> handleChangeTitleFocus(event)
            is TaskDialogEvent.SelectPriority -> handleSelectPriority(event)
            is TaskDialogEvent.SelectCategory -> handleSelectCategory(event)
            is TaskDialogEvent.ToggleCategoryPicker -> handleToggleCategoryPicker()
            is TaskDialogEvent.SelectDateAtStartOfDay -> handleSelectDateAtStartOfDay(event)
            is TaskDialogEvent.SelectTimeOffset -> handleSelectTimeOffset(event)
            is TaskDialogEvent.SelectDuration -> handleSelectDuration(event)
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

    private fun handleSetInitialCategoryId(event: TaskDialogEvent.SetInitialCategoryId) {
        _dialogState.value = dialogState.value.copy(categoryId = event.categoryId)
    }

    private fun handleEnterTitle(event: TaskDialogEvent.EnterTitle) {
        _dialogState.value = dialogState.value.copy(
            title = dialogState.value.title.copy(
                text = event.value,
                isHintVisible = event.value.isBlank()
            )
        )
    }

    private fun handleChangeTitleFocus(event: TaskDialogEvent.ChangeTitleFocus) {
        _dialogState.value = dialogState.value.copy(
            title = dialogState.value.title.copy(
                isHintVisible = dialogState.value.title.text.isBlank()
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

    private fun handleSelectDateAtStartOfDay(event: TaskDialogEvent.SelectDateAtStartOfDay) {
        _dialogState.value = dialogState.value.copy(dateAtStartOfDay = event.timestamp)
    }

    private fun handleSelectTimeOffset(event: TaskDialogEvent.SelectTimeOffset) {
        _dialogState.value = dialogState.value.copy(timeOffsetMillis = event.offsetMillis)
    }

    private fun handleSelectDuration(event: TaskDialogEvent.SelectDuration) {
        _dialogState.value = dialogState.value.copy(durationMillis = event.durationMillis)
    }

    private fun handleSetRecurrence(event: TaskDialogEvent.SetRecurrence) {
        _dialogState.value = dialogState.value.copy(rrule = event.rrule)
    }

    private fun handleAddSubtask() {
        if (dialogState.value.subtasks.size >= MAX_SUBTASKS) {
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
                    subtask.copy(
                        text = event.text,
                        isHintVisible = event.text.isBlank()
                    )
                } else subtask
            }
        )
    }

    private fun handleChangeSubtaskFocus(event: TaskDialogEvent.ChangeSubtaskFocus) {
        _dialogState.value = dialogState.value.copy(
            subtasks = dialogState.value.subtasks.mapIndexed { index, subtask ->
                if (index == event.index) {
                    subtask.copy(
                        isHintVisible = subtask.text.isBlank()
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
                val currentTime = System.currentTimeMillis()
                val selectedDate = dialogState.value.dateAtStartOfDay ?: startOfDay(currentTime)
                val selectedTimeOffset =
                    dialogState.value.timeOffsetMillis ?: timeOffsetMillis(currentTime)
                val finalStartAt = selectedDate + selectedTimeOffset
                val finalDueDate = dialogState.value.durationMillis?.let { duration ->
                    finalStartAt + duration
                } ?: finalStartAt

                val task = Task(
                    title = dialogState.value.title.text,
                    description = null,
                    startAt = finalStartAt,
                    dueAt = finalDueDate,
                    categoryId = dialogState.value.categoryId,
                    priority = dialogState.value.priority,
                    rrule = dialogState.value.rrule,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )

                val parentTaskId = taskUseCases.taskCRUD.upsertTask(task).toInt()

                val subtaskTitles = dialogState.value.subtasks
                    .map { it.text.trim() }
                    .filter { it.isNotEmpty() }

                subtaskTitles.forEach { subtaskTitle ->
                    taskUseCases.taskCRUD.upsertTask(
                        Task(
                            title = subtaskTitle,
                            parentId = parentTaskId,
                            categoryId = task.categoryId,
                            createdAt = currentTime,
                            updatedAt = currentTime
                        )
                    )
                }

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

    private fun startOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun timeOffsetMillis(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        return ((hours * 60) + minutes) * 60_000L
    }

    sealed class UiEvent {
        data class ShowSnackBar(val message: String) : UiEvent()
        data object DismissDialog : UiEvent()
    }
}
