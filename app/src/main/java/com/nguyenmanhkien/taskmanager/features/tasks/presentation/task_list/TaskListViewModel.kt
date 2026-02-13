package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TimeFilter
import com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.TaskUseCases
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.OrderType
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskOrderField
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
            is TaskListEvent.ToggleTaskCompletion -> toggleTaskCompletion(event.task)
            is TaskListEvent.SetTaskPriority -> setTaskPriority(event.taskId, event.priority)
            is TaskListEvent.ToggleCompletedTasksVisibility -> toggleCompletedTasksVisibility()
            is TaskListEvent.TogglePendingTasksVisibility -> togglePendingTasksVisibility()
        }
    }

    private fun filterTaskByTime(timeFilter: TimeFilter) {
        if (state.value.timeFilter == timeFilter) {
            _state.value = state.value.copy(
                timeFilter = TimeFilter.NONE
            )
        } else {
            _state.value = state.value.copy(
                timeFilter = timeFilter
            )
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

        val sortedTasks = applySorting(state.value.tasks)
        _state.value = state.value.copy(
            tasks = sortedTasks
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

        searchJob = viewModelScope.launch {
            delay(200)
            _state.value = state.value.copy(searchString = searchString)
            getTasks()
        }
    }

    private fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            taskUseCases.taskCRUD.toggleTaskCompletion(task)
        }
    }

    private fun setTaskPriority(taskId: Int, priority: TaskPriority) {
        viewModelScope.launch {
            taskUseCases.taskCRUD.setPriority(taskId, priority)
        }
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

            TimeFilter.NONE -> {
                Pair(null, null)
            }
        }

        getTasksJob = taskUseCases.taskCRUD.getFilteredTasks(
            categoryId = categoryId,
            startTime = startTime,
            endTime = endTime,
            searchQuery = state.value.searchString.takeIf { it.isNotBlank() }
        )
            .onEach { tasks ->
                val sortedTasks = applySorting(tasks)
                _state.value = state.value.copy(
                    tasks = sortedTasks
                )
            }
            .launchIn(viewModelScope)
    }

    private fun applySorting(tasks: List<Task>): List<Task> {
        return when (state.value.taskOrderField) {
            TaskOrderField.DUE_DATE -> {
                when (state.value.orderType) {
                    OrderType.ASCENDING -> tasks.sortedBy { it.dueAt }
                    OrderType.DESCENDING -> tasks.sortedByDescending { it.dueAt }
                }
            }

            TaskOrderField.CREATION_TIME -> {
                when (state.value.orderType) {
                    OrderType.ASCENDING -> tasks.sortedBy { it.createdAt }
                    OrderType.DESCENDING -> tasks.sortedByDescending { it.createdAt }
                }
            }

            TaskOrderField.ALPHABET -> {
                when (state.value.orderType) {
                    OrderType.ASCENDING -> tasks.sortedBy { it.title.lowercase() }
                    OrderType.DESCENDING -> tasks.sortedByDescending { it.title.lowercase() }
                }
            }

            TaskOrderField.PRIORITY -> {
                when (state.value.orderType) {
                    OrderType.ASCENDING -> tasks.sortedBy { it.priority.ordinal }
                    OrderType.DESCENDING -> tasks.sortedByDescending { it.priority.ordinal }
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
