package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_calendar

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskWithSubtasks
import java.time.LocalDate
import java.time.YearMonth

data class TaskCalendarState(
	val visibleMonth: YearMonth = YearMonth.now(),
	val selectedDate: LocalDate? = LocalDate.now(),
	val isCalendarCollapsed: Boolean = false,
	val daysWithTasks: Set<LocalDate> = emptySet(),
	val selectedDayTaskItems: List<TaskWithSubtasks> = emptyList(),
	val monthSummaries: List<CalendarDaySummary> = emptyList(),
	val categories: List<Category> = emptyList(),
	val animatingTaskIds: Set<Int> = emptySet(),
	val activePriorityTaskId: Int? = null
)

data class CalendarDaySummary(
	val date: LocalDate,
	val totalCount: Int,
	val completedCount: Int
)

