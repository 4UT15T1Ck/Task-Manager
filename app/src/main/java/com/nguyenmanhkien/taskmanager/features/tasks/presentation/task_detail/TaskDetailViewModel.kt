package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.TaskUseCases
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.util.Calendar

@KoinViewModel
class TaskDetailViewModel(
	private val taskUseCases: TaskUseCases
) : ViewModel() {
	companion object {
		private const val MAX_SUBTASKS = 20
		private const val DEFAULT_SUBTASK_TITLE = "subtask"
	}

	private val _state = mutableStateOf(TaskDetailState())
	val state: State<TaskDetailState> = _state

	private val _eventFlow = MutableSharedFlow<UiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private var taskJob: Job? = null
	private var categoriesJob: Job? = null

	fun start(taskId: Int) {
		if (taskId < 0) {
			viewModelScope.launch { _eventFlow.emit(UiEvent.NavigateBack) }
			return
		}
		if (state.value.taskId == taskId && taskJob != null) {
			return
		}

		_state.value = state.value.copy(taskId = taskId)
		observeTask(taskId)
		observeCategories()
	}

	fun onEvent(event: TaskDetailEvent) {
		when (event) {
			TaskDetailEvent.ToggleMenu -> {
				_state.value = state.value.copy(isMenuExpanded = !state.value.isMenuExpanded)
			}

			TaskDetailEvent.DismissMenu -> {
				_state.value = state.value.copy(isMenuExpanded = false)
			}

			TaskDetailEvent.ToggleTaskCompletion -> toggleTaskCompletion()

			TaskDetailEvent.DuplicateTask -> duplicateTask()

			TaskDetailEvent.RequestTaskDeletion -> {
				_state.value = state.value.copy(
					isMenuExpanded = false,
					isDeletionRequest = true
				)
			}

			TaskDetailEvent.CancelTaskDeletion -> {
				_state.value = state.value.copy(isDeletionRequest = false)
			}

			TaskDetailEvent.ConfirmTaskDeletion -> {
				val task = state.value.taskWithSubtasks?.task ?: return
				viewModelScope.launch {
					taskUseCases.taskCRUD.deleteTask(task)
					_eventFlow.emit(UiEvent.NavigateBack)
				}
			}

			TaskDetailEvent.ToggleCategoryPicker -> {
				if (isCompleted()) return
				_state.value = state.value.copy(
					isCategoryPickerVisible = !state.value.isCategoryPickerVisible
				)
			}

			is TaskDetailEvent.SelectCategory -> selectCategory(event.categoryId)

			is TaskDetailEvent.UpdateTitle -> {
				if (isCompleted()) return
				_state.value = state.value.copy(titleInput = event.value)
			}

			TaskDetailEvent.CommitTitle -> commitTitle()

			is TaskDetailEvent.ToggleSubtaskCompletion -> toggleSubtaskCompletion(event.subtaskId)
			is TaskDetailEvent.UpdateSubtaskTitle -> updateSubtaskTitle(event.subtaskId, event.value)
			is TaskDetailEvent.CommitSubtaskTitle -> commitSubtaskTitle(event.subtaskId)
			is TaskDetailEvent.SubmitSubtaskOnNext -> submitSubtaskOnNext(event.subtaskId)
			is TaskDetailEvent.DeleteSubtask -> deleteSubtask(event.subtaskId)
			TaskDetailEvent.AddSubtask -> addSubtask()

			is TaskDetailEvent.ShowPickerDialog -> {
				if (isCompleted()) return
				_state.value = state.value.copy(activePickerSection = event.section)
			}

			TaskDetailEvent.HidePickerDialog -> {
				_state.value = state.value.copy(activePickerSection = null)
			}

			is TaskDetailEvent.SelectDateAtStartOfDay -> updateDate(event.timestamp)
			is TaskDetailEvent.SelectTimeOffset -> updateTimeOffset(event.offsetMillis)
			is TaskDetailEvent.SelectDuration -> updateDuration(event.durationMillis)
			is TaskDetailEvent.SetRecurrence -> updateRecurrence(event.recurrenceJson)

			TaskDetailEvent.OpenDescriptionEditor -> {
				if (isCompleted()) return
				_state.value = state.value.copy(
					isDescriptionEditorVisible = true,
					descriptionInput = state.value.taskWithSubtasks?.task?.description.orEmpty()
				)
			}

			is TaskDetailEvent.UpdateDescription -> {
				if (isCompleted()) return
				_state.value = state.value.copy(descriptionInput = event.value)
			}

			TaskDetailEvent.SaveDescriptionAndCloseEditor -> saveDescriptionAndCloseEditor()

			TaskDetailEvent.ClearPendingSubtaskFocus -> {
				_state.value = state.value.copy(pendingSubtaskFocusId = null)
			}
		}
	}

	private fun observeTask(taskId: Int) {
		taskJob?.cancel()
		taskJob = viewModelScope.launch {
			taskUseCases.taskCRUD.getTaskDetail(taskId).collect { taskWithSubtasks ->
				if (taskWithSubtasks == null) {
					_eventFlow.emit(UiEvent.NavigateBack)
					return@collect
				}

				val mergedSubtaskInputs = taskWithSubtasks.subtasks.associate { subtask ->
					val defaultInput = if (subtask.title == DEFAULT_SUBTASK_TITLE) "" else subtask.title
					subtask.id to (state.value.subtaskInputs[subtask.id] ?: defaultInput)
				}

				_state.value = state.value.copy(
					taskWithSubtasks = taskWithSubtasks,
					titleInput = taskWithSubtasks.task.title,
					subtaskInputs = mergedSubtaskInputs,
					descriptionInput = if (state.value.isDescriptionEditorVisible) {
						state.value.descriptionInput
					} else {
						taskWithSubtasks.task.description.orEmpty()
					}
				)
			}
		}
	}

	private fun observeCategories() {
		categoriesJob?.cancel()
		categoriesJob = viewModelScope.launch {
			taskUseCases.category.getCategories().collect { categories ->
				_state.value = state.value.copy(categories = categories)
			}
		}
	}

	private fun toggleTaskCompletion() {
		val parentTask = state.value.taskWithSubtasks?.task ?: return
		val nextStatus = if (parentTask.status == TaskStatus.COMPLETED) {
			TaskStatus.IN_PROGRESS
		} else {
			TaskStatus.COMPLETED
		}

		viewModelScope.launch {
			taskUseCases.taskCRUD.setTaskAndSubtasksStatus(parentTask.id, nextStatus)
			_state.value = state.value.copy(isMenuExpanded = false)
		}
	}

	private fun duplicateTask() {
		val currentTaskId = state.value.taskWithSubtasks?.task?.id ?: return
		viewModelScope.launch {
			val duplicatedTaskId = taskUseCases.taskCRUD.duplicateTaskWithSubtasks(currentTaskId)
			_state.value = state.value.copy(isMenuExpanded = false)
			if (duplicatedTaskId != null) {
				_eventFlow.emit(UiEvent.NavigateToTaskDetail(duplicatedTaskId))
			}
		}
	}

	private fun selectCategory(categoryId: Int?) {
		if (isCompleted()) return
		val task = state.value.taskWithSubtasks?.task ?: return
		viewModelScope.launch {
			taskUseCases.taskCRUD.upsertTask(task.copy(categoryId = categoryId))
			_state.value = state.value.copy(isCategoryPickerVisible = false)
		}
	}

	private fun commitTitle() {
		if (isCompleted()) return
		val task = state.value.taskWithSubtasks?.task ?: return
		val committedTitle = state.value.titleInput.trim().ifBlank { task.title }
		if (committedTitle == task.title) return

		viewModelScope.launch {
			taskUseCases.taskCRUD.upsertTask(task.copy(title = committedTitle))
		}
	}

	private fun toggleSubtaskCompletion(subtaskId: Int) {
		if (isCompleted()) return
		val subtask = state.value.taskWithSubtasks?.subtasks?.firstOrNull { it.id == subtaskId } ?: return
		val nextStatus = if (subtask.status == TaskStatus.COMPLETED) TaskStatus.IN_PROGRESS else TaskStatus.COMPLETED
		viewModelScope.launch {
			taskUseCases.taskCRUD.setStatus(subtask.id, nextStatus)
		}
	}

	private fun updateSubtaskTitle(subtaskId: Int, value: String) {
		if (isCompleted()) return
		_state.value = state.value.copy(
			subtaskInputs = state.value.subtaskInputs + (subtaskId to value)
		)
	}

	private fun commitSubtaskTitle(subtaskId: Int) {
		if (isCompleted()) return
		val subtask = state.value.taskWithSubtasks?.subtasks?.firstOrNull { it.id == subtaskId } ?: return
		val updatedTitle = state.value.subtaskInputs[subtaskId]?.trim().orEmpty().ifBlank { DEFAULT_SUBTASK_TITLE }
		if (updatedTitle == subtask.title) return

		viewModelScope.launch {
			taskUseCases.taskCRUD.upsertTask(subtask.copy(title = updatedTitle))
		}
	}

	private fun submitSubtaskOnNext(subtaskId: Int) {
		if (isCompleted()) return
		commitSubtaskTitle(subtaskId)
		addSubtask()
	}

	private fun deleteSubtask(subtaskId: Int) {
		if (isCompleted()) return
		val subtask = state.value.taskWithSubtasks?.subtasks?.firstOrNull { it.id == subtaskId } ?: return
		viewModelScope.launch {
			taskUseCases.taskCRUD.deleteTask(subtask)
		}
	}

	private fun addSubtask() {
		if (isCompleted()) return
		val taskWithSubtasks = state.value.taskWithSubtasks ?: return
		if (taskWithSubtasks.subtasks.size >= MAX_SUBTASKS) return

		val parentTask = taskWithSubtasks.task
		viewModelScope.launch {
			val now = System.currentTimeMillis()
			val subtaskId = taskUseCases.taskCRUD.upsertTask(
				Task(
					title = DEFAULT_SUBTASK_TITLE,
					parentId = parentTask.id,
					categoryId = parentTask.categoryId,
					createdAt = now,
					updatedAt = now
				)
			).toInt()
			_state.value = state.value.copy(
				subtaskInputs = state.value.subtaskInputs + (subtaskId to ""),
				pendingSubtaskFocusId = subtaskId
			)
		}
	}

	private fun updateDate(dateAtStartOfDay: Long) {
		if (isCompleted()) return
		val task = state.value.taskWithSubtasks?.task ?: return
		val startAt = task.startAt ?: System.currentTimeMillis()
		val timeOffset = timeOffsetMillis(startAt)
		val duration = taskDuration(task)

		val newStartAt = dateAtStartOfDay + timeOffset
		val newDueAt = duration?.let { newStartAt + it }

		viewModelScope.launch {
			taskUseCases.taskCRUD.upsertTask(task.copy(startAt = newStartAt, dueAt = newDueAt))
		}
	}

	private fun updateTimeOffset(offsetMillis: Long) {
		if (isCompleted()) return
		val task = state.value.taskWithSubtasks?.task ?: return
		val startAt = task.startAt ?: System.currentTimeMillis()
		val dateAtStartOfDay = startOfDay(startAt)
		val duration = taskDuration(task)

		val newStartAt = dateAtStartOfDay + offsetMillis
		val newDueAt = duration?.let { newStartAt + it }

		viewModelScope.launch {
			taskUseCases.taskCRUD.upsertTask(task.copy(startAt = newStartAt, dueAt = newDueAt))
		}
	}

	private fun updateDuration(durationMillis: Long) {
		if (isCompleted()) return
		val task = state.value.taskWithSubtasks?.task ?: return
		val startAt = task.startAt ?: System.currentTimeMillis()
		viewModelScope.launch {
			taskUseCases.taskCRUD.upsertTask(task.copy(startAt = startAt, dueAt = startAt + durationMillis))
		}
	}

	private fun updateRecurrence(recurrenceJson: String?) {
		if (isCompleted()) return
		val task = state.value.taskWithSubtasks?.task ?: return
		viewModelScope.launch {
			taskUseCases.taskCRUD.upsertTask(task.copy(rrule = recurrenceJson))
		}
	}

	private fun saveDescriptionAndCloseEditor() {
		if (isCompleted()) {
			_state.value = state.value.copy(isDescriptionEditorVisible = false)
			return
		}

		val task = state.value.taskWithSubtasks?.task ?: return
		val description = state.value.descriptionInput.trim().ifBlank { "" }
		viewModelScope.launch {
			taskUseCases.taskCRUD.upsertTask(
				task.copy(description = description.ifBlank { null })
			)
			_state.value = state.value.copy(isDescriptionEditorVisible = false)
		}
	}

	private fun isCompleted(): Boolean {
		return state.value.taskWithSubtasks?.task?.status == TaskStatus.COMPLETED
	}

	private fun taskDuration(task: Task): Long? {
		val startAt = task.startAt ?: return null
		val dueAt = task.dueAt ?: return null
		return (dueAt - startAt).coerceAtLeast(0L)
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
		return ((calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE)) * 60_000L
	}

	sealed class UiEvent {
		data object NavigateBack : UiEvent()
		data class NavigateToTaskDetail(val taskId: Int) : UiEvent()
	}
}
