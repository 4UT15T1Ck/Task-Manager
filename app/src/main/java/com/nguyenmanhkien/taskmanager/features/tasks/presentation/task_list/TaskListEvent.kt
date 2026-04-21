package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.OrderType
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskOrderField
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TimeFilter

sealed class TaskListEvent {
    data class FilterTaskByTime(val timeFilter: TimeFilter) : TaskListEvent()
    data class SortTask(val taskOrderField: TaskOrderField, val orderType: OrderType) : TaskListEvent()
    data class FilterByCategory(val category: Category?) : TaskListEvent()

    data class SearchTask(val searchString: String) : TaskListEvent()
    data object ShowSearchBar : TaskListEvent()
    data object HideSearchBar : TaskListEvent()

    data class ToggleParentTaskCompletion(val task: Task) : TaskListEvent()
    data class ToggleSubtaskCompletion(val task: Task) : TaskListEvent()
    data class SetTaskPriority(val taskId: Int, val priority: TaskPriority) : TaskListEvent()
    data class OpenPriorityMenu(val taskId: Int) : TaskListEvent()
    data object DismissPriorityMenu : TaskListEvent()

    data object TogglePendingTasksVisibility : TaskListEvent()
    data object ToggleCompletedTasksVisibility : TaskListEvent()

    data object ToggleHeaderMenu : TaskListEvent()
    data object DismissHeaderMenu : TaskListEvent()
    data object ToggleShowSubtasks : TaskListEvent()
    data object ShowSortDialog : TaskListEvent()
    data object DismissSortDialog : TaskListEvent()
    data object ShowCategoryMenu : TaskListEvent()
    data object DismissCategoryMenu : TaskListEvent()

    data class DeleteCategory(val category: Category) : TaskListEvent()
    data class UpsertCategory(val category: Category) : TaskListEvent()

    data object EnterSelectionMode : TaskListEvent()
    data object ExitSelectionMode : TaskListEvent()
    data class ToggleTaskSelection(val taskId: Int) : TaskListEvent()
    data class ToggleSectionSelection(val taskIds: List<Int>) : TaskListEvent()
    data object CompleteSelectedTasks : TaskListEvent()
    data object DeleteSelectedTasks : TaskListEvent()
    data class ChangeSelectedTasksCategory(val categoryId: Int?) : TaskListEvent()
}
