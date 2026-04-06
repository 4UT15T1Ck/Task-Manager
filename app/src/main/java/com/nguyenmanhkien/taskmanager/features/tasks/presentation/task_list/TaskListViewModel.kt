package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.OrderType
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskOrderField
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskWithSubtasks
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TimeFilter
import com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.TaskUseCases
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TaskListViewModel(
    private val taskUseCases: TaskUseCases
) : ViewModel() {
    companion object {
        private const val SEARCH_DEBOUNCE_MS = 100L
        private const val PARENT_COMPLETION_ANIMATION_MS = 500L
    }

    private val _state = mutableStateOf(TaskListState())
    val state: State<TaskListState> = _state
    private var getTasksJob: Job? = null
    private var getCategoriesJob: Job? = null
    private var searchJob: Job? = null

    init {
        getCategories()
        getTasks()
    }

    fun onEvent(event: TaskListEvent) {
        when (event) {
            is TaskListEvent.FilterTaskByTime -> filterTaskByTime(event.timeFilter)
            is TaskListEvent.SortTask -> sortTask(event.taskOrderField, event.orderType)
            is TaskListEvent.FilterByCategory -> filterByCategory(event.category)
            is TaskListEvent.SearchTask -> searchTask(event.searchString)
            is TaskListEvent.ShowSearchBar -> showSearchBar()
            is TaskListEvent.HideSearchBar -> hideSearchBar()
            is TaskListEvent.ToggleParentTaskCompletion -> toggleParentTaskCompletion(event.task)
            is TaskListEvent.ToggleSubtaskCompletion -> toggleSubtaskCompletion(event.task)
            is TaskListEvent.SetTaskPriority -> setTaskPriority(event.taskId, event.priority)
            is TaskListEvent.OpenPriorityMenu -> openPriorityMenu(event.taskId)
            is TaskListEvent.DismissPriorityMenu -> dismissPriorityMenu()
            is TaskListEvent.ToggleHeaderMenu -> toggleHeaderMenu()
            is TaskListEvent.DismissHeaderMenu -> dismissHeaderMenu()
            is TaskListEvent.ToggleShowSubtasks -> toggleShowSubtasks()
            is TaskListEvent.EnterSelectionMode -> enterSelectionMode()
            is TaskListEvent.ExitSelectionMode -> exitSelectionMode()
            is TaskListEvent.ToggleTaskSelection -> toggleTaskSelection(event.taskId)
            is TaskListEvent.ToggleSectionSelection -> toggleSectionSelection(event.taskIds)
            is TaskListEvent.CompleteSelectedTasks -> completeSelectedTasks()
            is TaskListEvent.DeleteSelectedTasks -> deleteSelectedTasks()
            is TaskListEvent.ChangeSelectedTasksCategory -> changeSelectedTasksCategory(event.categoryId)
            is TaskListEvent.ShowBulkCategoryMenu -> showBulkCategoryMenu()
            is TaskListEvent.DismissBulkCategoryMenu -> dismissBulkCategoryMenu()
            is TaskListEvent.ToggleCompletedTasksVisibility -> toggleCompletedTasksVisibility()
            is TaskListEvent.TogglePendingTasksVisibility -> togglePendingTasksVisibility()
        }
    }

    private fun filterTaskByTime(timeFilter: TimeFilter) {
        _state.value = if (state.value.timeFilter == timeFilter) {
            state.value.copy(timeFilter = TimeFilter.NONE)
        } else {
            state.value.copy(timeFilter = timeFilter)
        }
        getTasks()
    }

    private fun sortTask(taskOrderField: TaskOrderField, orderType: OrderType) {
        if (state.value.taskOrderField == taskOrderField &&
            state.value.orderType == orderType
        ) {
            return
        }

        _state.value = state.value.copy(
            taskOrderField = taskOrderField,
            orderType = orderType
        )

        _state.value = state.value.copy(
            taskItems = applySorting(state.value.taskItems)
        )
    }

    private fun filterByCategory(category: Category?) {
        if (state.value.chosenCategory == category) {
            return
        }
        _state.value = state.value.copy(
            chosenCategory = category
        )
        getTasks()
    }

    private fun searchTask(searchString: String) {
        searchJob?.cancel()

        _state.value = state.value.copy(
            searchString = searchString,
            isSearchVisible = state.value.isSearchVisible || searchString.isNotBlank()
        )

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            getTasks()
        }
    }

    private fun showSearchBar() {
        if (state.value.isSearchVisible) {
            return
        }
        _state.value = state.value.copy(isSearchVisible = true)
    }

    private fun hideSearchBar() {
        if (!state.value.isSearchVisible || state.value.searchString.isNotBlank()) {
            return
        }
        _state.value = state.value.copy(isSearchVisible = false)
    }

    private fun toggleParentTaskCompletion(task: Task) {
        if (task.status == TaskStatus.COMPLETED) {
            viewModelScope.launch {
                taskUseCases.taskCRUD.setTaskAndSubtasksStatus(task.id, TaskStatus.IN_PROGRESS)
            }
            return
        }

        if (task.id in state.value.animatingTaskIds) {
            return
        }

        _state.value = state.value.copy(
            animatingTaskIds = state.value.animatingTaskIds + task.id
        )
        viewModelScope.launch {
            delay(PARENT_COMPLETION_ANIMATION_MS)
            taskUseCases.taskCRUD.setTaskAndSubtasksStatus(task.id, TaskStatus.COMPLETED)
            _state.value = state.value.copy(
                animatingTaskIds = state.value.animatingTaskIds - task.id
            )
        }
    }

    private fun toggleSubtaskCompletion(task: Task) {
        viewModelScope.launch {
            taskUseCases.taskCRUD.toggleTaskCompletion(task)
        }
    }

    private fun setTaskPriority(taskId: Int, priority: TaskPriority) {
        viewModelScope.launch {
            taskUseCases.taskCRUD.setPriority(taskId, priority)
        }
        dismissPriorityMenu()
    }

    private fun openPriorityMenu(taskId: Int) {
        _state.value = state.value.copy(activePriorityTaskId = taskId)
    }

    private fun dismissPriorityMenu() {
        _state.value = state.value.copy(activePriorityTaskId = null)
    }

    private fun toggleHeaderMenu() {
        _state.value = state.value.copy(
            isHeaderMenuExpanded = !state.value.isHeaderMenuExpanded
        )
    }

    private fun dismissHeaderMenu() {
        _state.value = state.value.copy(isHeaderMenuExpanded = false)
    }

    private fun toggleShowSubtasks() {
        _state.value = state.value.copy(
            isShowingSubtasks = !state.value.isShowingSubtasks,
            isHeaderMenuExpanded = false
        )
    }

    private fun enterSelectionMode() {
        _state.value = state.value.copy(
            isSelectionMode = true,
            selectedTaskIds = emptySet(),
            isHeaderMenuExpanded = false
        )
    }

    private fun exitSelectionMode() {
        _state.value = state.value.copy(
            isSelectionMode = false,
            selectedTaskIds = emptySet(),
            isBulkCategoryMenuExpanded = false
        )
    }

    private fun toggleTaskSelection(taskId: Int) {
        val selectedTaskIds = state.value.selectedTaskIds.toMutableSet()
        if (!selectedTaskIds.add(taskId)) {
            selectedTaskIds.remove(taskId)
        }
        _state.value = state.value.copy(selectedTaskIds = selectedTaskIds)
    }

    private fun toggleSectionSelection(taskIds: List<Int>) {
        if (taskIds.isEmpty()) {
            return
        }

        val selectedTaskIds = state.value.selectedTaskIds.toMutableSet()
        val areAllSelected = taskIds.all { it in selectedTaskIds }
        if (areAllSelected) {
            selectedTaskIds.removeAll(taskIds.toSet())
        } else {
            selectedTaskIds.addAll(taskIds)
        }
        _state.value = state.value.copy(selectedTaskIds = selectedTaskIds)
    }

    private fun completeSelectedTasks() {
        val selectedIds = state.value.selectedTaskIds.toList()
        if (selectedIds.isEmpty()) {
            return
        }
        val selectedInProgressIds = state.value.taskItems
            .asSequence()
            .filter { it.task.id in selectedIds }
            .filter { it.task.status != TaskStatus.COMPLETED }
            .map { it.task.id }
            .toList()
        viewModelScope.launch {
            taskUseCases.taskCRUD.setStatus(selectedInProgressIds, TaskStatus.COMPLETED)
            exitSelectionMode()
        }
    }

    private fun deleteSelectedTasks() {
        val selectedIds = state.value.selectedTaskIds.toList()
        if (selectedIds.isEmpty()) {
            return
        }
        viewModelScope.launch {
            taskUseCases.taskCRUD.bulkDeleteTasks(selectedIds)
            exitSelectionMode()
        }
    }

    private fun changeSelectedTasksCategory(categoryId: Int?) {
        val selectedIds = state.value.selectedTaskIds.toList()
        if (selectedIds.isEmpty()) {
            return
        }
        viewModelScope.launch {
            taskUseCases.taskCRUD.setCategory(selectedIds, categoryId)
            exitSelectionMode()
        }
    }

    private fun showBulkCategoryMenu() {
        _state.value = state.value.copy(isBulkCategoryMenuExpanded = true)
    }

    private fun dismissBulkCategoryMenu() {
        _state.value = state.value.copy(isBulkCategoryMenuExpanded = false)
    }

    private fun togglePendingTasksVisibility() {
        _state.value = state.value.copy(
            isPendingTasksVisible = !state.value.isPendingTasksVisible
        )
    }

    private fun toggleCompletedTasksVisibility() {
        _state.value = state.value.copy(
            isCompletedTasksVisible = !state.value.isCompletedTasksVisible
        )
    }

    private fun getTasks() {
        getTasksJob?.cancel()

        val categoryId = state.value.chosenCategory?.id

        val (startTime, endTime) = when (state.value.timeFilter) {
            TimeFilter.TODAY -> {
                val startCal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }

                val endCal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 23)
                    set(java.util.Calendar.MINUTE, 59)
                    set(java.util.Calendar.SECOND, 59)
                    set(java.util.Calendar.MILLISECOND, 999)
                }

                Pair(startCal.timeInMillis, endCal.timeInMillis)
            }

            TimeFilter.UPCOMING -> {
                val startCal = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }

                Pair(startCal.timeInMillis, null)
            }

            TimeFilter.PAST -> {
                val endCal = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, -1)
                    set(java.util.Calendar.HOUR_OF_DAY, 23)
                    set(java.util.Calendar.MINUTE, 59)
                    set(java.util.Calendar.SECOND, 59)
                    set(java.util.Calendar.MILLISECOND, 999)
                }

                Pair(null, endCal.timeInMillis)
            }

            TimeFilter.NONE -> Pair(null, null)
        }

        getTasksJob = taskUseCases.taskCRUD.getFilteredTasksWithSubtasks(
            categoryId = categoryId,
            startTime = startTime,
            endTime = endTime,
            searchQuery = state.value.searchString.takeIf { it.isNotBlank() }
        )
            .onEach { taskItems ->
                _state.value = state.value.copy(
                    taskItems = applySorting(taskItems)
                )
            }
            .launchIn(viewModelScope)
    }

    private fun applySorting(tasks: List<TaskWithSubtasks>): List<TaskWithSubtasks> {
        return when (state.value.taskOrderField) {
            TaskOrderField.DUE_DATE -> {
                when (state.value.orderType) {
                    OrderType.ASCENDING -> tasks.sortedBy { it.task.dueAt }
                    OrderType.DESCENDING -> tasks.sortedByDescending { it.task.dueAt }
                }
            }

            TaskOrderField.CREATION_TIME -> {
                when (state.value.orderType) {
                    OrderType.ASCENDING -> tasks.sortedBy { it.task.createdAt }
                    OrderType.DESCENDING -> tasks.sortedByDescending { it.task.createdAt }
                }
            }

            TaskOrderField.ALPHABET -> {
                when (state.value.orderType) {
                    OrderType.ASCENDING -> tasks.sortedBy { it.task.title.lowercase() }
                    OrderType.DESCENDING -> tasks.sortedByDescending { it.task.title.lowercase() }
                }
            }

            TaskOrderField.PRIORITY -> {
                when (state.value.orderType) {
                    OrderType.ASCENDING -> tasks.sortedBy { it.task.priority.ordinal }
                    OrderType.DESCENDING -> tasks.sortedByDescending { it.task.priority.ordinal }
                }
            }
        }
    }

    private fun getCategories() {
        getCategoriesJob?.cancel()
        getCategoriesJob = taskUseCases.category.getCategories()
            .onEach { categories ->
                _state.value = state.value.copy(
                    categories = categories
                )
            }
            .launchIn(viewModelScope)
    }
}
