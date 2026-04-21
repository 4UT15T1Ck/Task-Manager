package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_calendar

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.TaskUseCases
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.time.Instant
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Calendar

@KoinViewModel
class TaskCalendarViewModel(
    private val taskUseCases: TaskUseCases
) : ViewModel() {
    companion object {
        private const val PARENT_COMPLETION_ANIMATION_MS = 500L
    }

    private val _state = mutableStateOf(TaskCalendarState())
    val state: State<TaskCalendarState> = _state

    private var monthMarkerJob: Job? = null
    private var monthSummaryJob: Job? = null
    private var selectedDayTasksJob: Job? = null
    private var categoriesJob: Job? = null

    init {
        observeCategories()
        observeMonthData(state.value.visibleMonth)
        observeSelectedDayTasks()
    }

    fun onEvent(event: TaskCalendarEvent) {
        when (event) {
            is TaskCalendarEvent.ChangeVisibleMonth -> changeVisibleMonth(event.month)
            is TaskCalendarEvent.ToggleDateSelection -> toggleDateSelection(event.date)
            is TaskCalendarEvent.SelectSummaryDate -> selectSummaryDate(event.date)
            is TaskCalendarEvent.SetCalendarCollapsed -> setCalendarCollapsed(event.isCollapsed)
            is TaskCalendarEvent.ToggleTaskCompletion -> toggleTaskCompletion(event.task)
            is TaskCalendarEvent.OpenPriorityMenu -> openPriorityMenu(event.taskId)
            is TaskCalendarEvent.DismissPriorityMenu -> dismissPriorityMenu()
            is TaskCalendarEvent.SetTaskPriority -> setTaskPriority(event.taskId, event.priority)
        }
    }

    private fun changeVisibleMonth(month: YearMonth) {
        if (month == state.value.visibleMonth) {
            return
        }

        val selectedDate = state.value.selectedDate
        val nextSelectedDate = if (selectedDate == null) {
            null
        } else {
            month.atDay(selectedDate.dayOfMonth.coerceAtMost(month.lengthOfMonth()))
        }

        _state.value = state.value.copy(
            visibleMonth = month,
            selectedDate = nextSelectedDate
        )

        observeMonthData(month)
        observeSelectedDayTasks()
    }

    private fun toggleDateSelection(date: LocalDate) {
        val nextSelectedDate = if (state.value.selectedDate == date) null else date
        _state.value = state.value.copy(selectedDate = nextSelectedDate)
        observeSelectedDayTasks()
    }

    private fun selectSummaryDate(date: LocalDate) {
        _state.value = state.value.copy(selectedDate = date)
        observeSelectedDayTasks()
    }

    private fun setCalendarCollapsed(isCollapsed: Boolean) {
        _state.value = state.value.copy(isCalendarCollapsed = isCollapsed)
    }

    private fun openPriorityMenu(taskId: Int) {
        _state.value = state.value.copy(activePriorityTaskId = taskId)
    }

    private fun dismissPriorityMenu() {
        _state.value = state.value.copy(activePriorityTaskId = null)
    }

    private fun observeMonthData(month: YearMonth) {
        val monthStart = month.atDay(1).atStartOfDayMillis()
        val monthEnd = month.atEndOfMonth().atEndOfDayMillis()
        val visibleGridStart = month.atDay(1)
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDayMillis()
        val visibleGridEnd = month.atEndOfMonth()
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .atEndOfDayMillis()

        monthMarkerJob?.cancel()
        monthMarkerJob = taskUseCases.calendar.getDaysWithTask(visibleGridStart, visibleGridEnd)
            .onEach { days ->
                _state.value = state.value.copy(
                    daysWithTasks = days.map { millisToLocalDate(it) }.toSet()
                )
            }
            .launchIn(viewModelScope)

        monthSummaryJob?.cancel()
        monthSummaryJob = taskUseCases.calendar.getTaskDaySummaries(monthStart, monthEnd)
            .onEach { summaries ->
                _state.value = state.value.copy(
                    monthSummaries = summaries
                        .map { summary ->
                            CalendarDaySummary(
                                date = millisToLocalDate(summary.dayStart),
                                totalCount = summary.totalCount,
                                completedCount = summary.completedCount
                            )
                        }
                        .sortedBy { it.date }
                )
            }
            .launchIn(viewModelScope)
    }

    private fun observeSelectedDayTasks() {
        selectedDayTasksJob?.cancel()

        val selectedDate = state.value.selectedDate
        if (selectedDate == null) {
            _state.value = state.value.copy(selectedDayTaskItems = emptyList())
            return
        }

        val dayStart = selectedDate.atStartOfDayMillis()
        val dayEnd = selectedDate.atEndOfDayMillis()

        selectedDayTasksJob = taskUseCases.taskCRUD.getFilteredTasksWithSubtasks(
            startTime = dayStart,
            endTime = dayEnd
        ).onEach { taskItems ->
            _state.value = state.value.copy(
                selectedDayTaskItems = taskItems.sortedBy { it.task.startAt ?: Long.MAX_VALUE }
            )
        }.launchIn(viewModelScope)
    }

    private fun observeCategories() {
        categoriesJob?.cancel()
        categoriesJob = taskUseCases.category.getCategories()
            .onEach { categories ->
                _state.value = state.value.copy(categories = categories)
            }
            .launchIn(viewModelScope)
    }

    private fun toggleTaskCompletion(task: Task) {
        if (task.id in state.value.animatingTaskIds) {
            return
        }

        if (task.status == TaskStatus.COMPLETED) {
            viewModelScope.launch {
                taskUseCases.taskCRUD.setTaskAndSubtasksStatus(task.id, TaskStatus.IN_PROGRESS)
            }
            return
        }

        _state.value = state.value.copy(animatingTaskIds = state.value.animatingTaskIds + task.id)
        viewModelScope.launch {
            delay(PARENT_COMPLETION_ANIMATION_MS)
            taskUseCases.taskCRUD.setTaskAndSubtasksStatus(task.id, TaskStatus.COMPLETED)
            _state.value =
                state.value.copy(animatingTaskIds = state.value.animatingTaskIds - task.id)
        }
    }

    private fun setTaskPriority(taskId: Int, priority: TaskPriority) {
        viewModelScope.launch {
            taskUseCases.taskCRUD.setPriority(taskId, priority)
        }
        _state.value = state.value.copy(activePriorityTaskId = null)
    }

    private fun LocalDate.atStartOfDayMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthValue - 1)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun LocalDate.atEndOfDayMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthValue - 1)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    private fun millisToLocalDate(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}

