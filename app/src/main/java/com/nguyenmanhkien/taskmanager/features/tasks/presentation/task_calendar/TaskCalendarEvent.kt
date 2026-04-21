package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_calendar

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import java.time.LocalDate
import java.time.YearMonth

sealed class TaskCalendarEvent {
	data class ChangeVisibleMonth(val month: YearMonth) : TaskCalendarEvent()
	data class ToggleDateSelection(val date: LocalDate) : TaskCalendarEvent()
	data class SelectSummaryDate(val date: LocalDate) : TaskCalendarEvent()
	data class SetCalendarCollapsed(val isCollapsed: Boolean) : TaskCalendarEvent()
	data class ToggleTaskCompletion(val task: Task) : TaskCalendarEvent()
	data class OpenPriorityMenu(val taskId: Int) : TaskCalendarEvent()
	data object DismissPriorityMenu : TaskCalendarEvent()
	data class SetTaskPriority(val taskId: Int, val priority: TaskPriority) : TaskCalendarEvent()
}

