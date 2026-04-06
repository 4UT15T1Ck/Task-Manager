package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.OrderType
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskOrderField
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskWithSubtasks
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TimeFilter

data class TaskListState(
    val taskItems: List<TaskWithSubtasks> = emptyList(),
    val categories: List<Category> = emptyList(),
    val chosenCategory: Category? = null,
    val taskOrderField: TaskOrderField = TaskOrderField.DUE_DATE,
    val orderType: OrderType = OrderType.DESCENDING,
    val searchString: String = "",
    val timeFilter: TimeFilter = TimeFilter.TODAY,
    val isSearchVisible: Boolean = false,
    val isHeaderMenuExpanded: Boolean = false,
    val isShowingSubtasks: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedTaskIds: Set<Int> = emptySet(),
    val animatingTaskIds: Set<Int> = emptySet(),
    val activePriorityTaskId: Int? = null,
    val isBulkCategoryMenuExpanded: Boolean = false,
    val isPendingTasksVisible: Boolean = true,
    val isCompletedTasksVisible: Boolean = true
)
