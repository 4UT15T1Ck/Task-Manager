package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TimeFilter
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components.CategoryChip
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components.CollapsibleSectionHeader
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.components.TaskItemCard
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components.TaskSortDialog
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components.TimeFilterChip
import org.koin.androidx.compose.koinViewModel

private val SearchRevealThreshold = 48.dp
private val SearchHideThreshold = 12.dp

@Composable
fun TaskListScreen(
    onTaskClick: (Int) -> Unit = {},
    onCategorySelected: (Int?) -> Unit = {},
    onManageCategories: () -> Unit = {},
    onEnterSelectionMode: () -> Unit = {},
    viewModel: TaskListViewModel = koinViewModel()
) {
    val state by viewModel.state
    val lazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val revealThresholdPx = with(LocalDensity.current) { SearchRevealThreshold.toPx() }
    val hideThresholdPx = with(LocalDensity.current) { SearchHideThreshold.toPx() }
    val revealAccumulator = remember { floatArrayOf(0f) }
    val hideAccumulator = remember { floatArrayOf(0f) }
    var searchFieldBounds by remember { mutableStateOf<Rect?>(null) }

    val nestedScrollConnection = remember(lazyListState, revealThresholdPx, hideThresholdPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput
                    || !(lazyListState.firstVisibleItemIndex == 0
                            && lazyListState.firstVisibleItemScrollOffset == 0)
                ) {
                    revealAccumulator[0] = 0f
                    hideAccumulator[0] = 0f
                    return Offset.Zero
                }

                when {
                    available.y > 0f && !state.isSearchVisible -> {
                        revealAccumulator[0] += available.y
                        hideAccumulator[0] = 0f
                        if (revealAccumulator[0] >= revealThresholdPx) {
                            viewModel.onEvent(TaskListEvent.ShowSearchBar)
                            revealAccumulator[0] = 0f
                        }
                    }

                    available.y < 0f && state.isSearchVisible && state.searchString.isBlank() -> {
                        hideAccumulator[0] += -available.y
                        revealAccumulator[0] = 0f
                        if (hideAccumulator[0] >= hideThresholdPx) {
                            viewModel.onEvent(TaskListEvent.HideSearchBar)
                            searchFieldBounds = null
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            hideAccumulator[0] = 0f
                        }
                    }

                    else -> {
                        revealAccumulator[0] = 0f
                        hideAccumulator[0] = 0f
                    }
                }

                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                revealAccumulator[0] = 0f
                hideAccumulator[0] = 0f
                return Velocity.Zero
            }
        }
    }

    val inProgressTasks = state.taskItems.filter { it.task.status == TaskStatus.IN_PROGRESS }
    val completedTasks = state.taskItems.filter { it.task.status == TaskStatus.COMPLETED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .pointerInput(state.isSearchVisible, searchFieldBounds) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Final)
                        val upChange = event.changes.firstOrNull { it.changedToUpIgnoreConsumed() }
                            ?: continue

                        val tappedOutsideSearch =
                            searchFieldBounds?.contains(upChange.position) != true
                        if (tappedOutsideSearch) {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        }
                    }
                }
            }
    ) {
        AnimatedVisibility(visible = state.isSearchVisible) {
            OutlinedTextField(
                value = state.searchString,
                onValueChange = { viewModel.onEvent(TaskListEvent.SearchTask(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        searchFieldBounds = coordinates.boundsInParent()
                    },
                placeholder = { Text("Search tasks") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search tasks"
                    )
                },
                trailingIcon = {
                    if (state.searchString.isNotBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.onEvent(TaskListEvent.SearchTask(""))
                                viewModel.onEvent(TaskListEvent.HideSearchBar)
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                }
            )
        }

        if (state.isSearchVisible) {
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    CategoryChip(
                        text = "All",
                        isSelected = state.chosenCategory == null,
                        onClick = {
                            viewModel.onEvent(TaskListEvent.FilterByCategory(null))
                            onCategorySelected(null)
                        }
                    )
                }

                items(state.categories, key = { it.id }) { category ->
                    CategoryChip(
                        text = category.name,
                        isSelected = state.chosenCategory == category,
                        onClick = {
                            viewModel.onEvent(TaskListEvent.FilterByCategory(category))
                            onCategorySelected(category.id)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                IconButton(
                    modifier = Modifier
                        .padding(0.dp)
                        .size(width = 24.dp, height = 24.dp),
                    onClick = { viewModel.onEvent(TaskListEvent.ToggleHeaderMenu) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Task list actions"
                    )
                }

                DropdownMenu(
                    expanded = state.isHeaderMenuExpanded,
                    onDismissRequest = { viewModel.onEvent(TaskListEvent.DismissHeaderMenu) }
                ) {
                    DropdownMenuItem(
                        text = { Text("Category Menu") },
                        onClick = {
                            viewModel.onEvent(TaskListEvent.DismissHeaderMenu)
                            onManageCategories()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Select mode") },
                        onClick = {
                            viewModel.onEvent(TaskListEvent.EnterSelectionMode)
                            viewModel.onEvent(TaskListEvent.DismissHeaderMenu)
                            onEnterSelectionMode()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (state.isShowingSubtasks) {
                                    "Hide subtask"
                                } else {
                                    "Show subtask"
                                }
                            )
                        },
                        onClick = { viewModel.onEvent(TaskListEvent.ToggleShowSubtasks) }
                    )
                    DropdownMenuItem(
                        text = { Text("Sort") },
                        onClick = {
                            viewModel.onEvent(TaskListEvent.ShowSortDialog)
                            viewModel.onEvent(TaskListEvent.DismissHeaderMenu)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TimeFilterChip(
                text = TimeFilter.PAST.name,
                isSelected = state.timeFilter == TimeFilter.PAST,
                onClick = { viewModel.onEvent(TaskListEvent.FilterTaskByTime(TimeFilter.PAST)) },
                modifier = Modifier.weight(1f)
            )
            TimeFilterChip(
                text = TimeFilter.TODAY.name,
                isSelected = state.timeFilter == TimeFilter.TODAY,
                onClick = { viewModel.onEvent(TaskListEvent.FilterTaskByTime(TimeFilter.TODAY)) },
                modifier = Modifier.weight(1f)
            )
            TimeFilterChip(
                text = TimeFilter.UPCOMING.name,
                isSelected = state.timeFilter == TimeFilter.UPCOMING,
                onClick = { viewModel.onEvent(TaskListEvent.FilterTaskByTime(TimeFilter.UPCOMING)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            item {
                CollapsibleSectionHeader(
                    title = "IN PROGRESS",
                    isExpanded = state.isPendingTasksVisible,
                    onToggle = { viewModel.onEvent(TaskListEvent.TogglePendingTasksVisibility) }
                )
            }

            if (state.isPendingTasksVisible) {
                items(inProgressTasks, key = { it.task.id }) { taskItem ->
                    TaskItemCard(
                        taskItem = taskItem,
                        isSelectionMode = state.isSelectionMode,
                        isSelected = taskItem.task.id in state.selectedTaskIds,
                        isAnimatingCompletion = taskItem.task.id in state.animatingTaskIds,
                        isShowingSubtasks = state.isShowingSubtasks,
                        isPriorityMenuExpanded = state.activePriorityTaskId == taskItem.task.id,
                        onClick = {
                            if (state.isSelectionMode) {
                                viewModel.onEvent(TaskListEvent.ToggleTaskSelection(taskItem.task.id))
                            } else {
                                onTaskClick(taskItem.task.id)
                            }
                        },
                        onCompletionClick = {
                            if (state.isSelectionMode) {
                                viewModel.onEvent(TaskListEvent.ToggleTaskSelection(taskItem.task.id))
                            } else {
                                viewModel.onEvent(TaskListEvent.ToggleParentTaskCompletion(taskItem.task))
                            }
                        },
                        onPriorityClick = {
                            viewModel.onEvent(TaskListEvent.OpenPriorityMenu(taskItem.task.id))
                        },
                        onPriorityDismiss = {
                            viewModel.onEvent(TaskListEvent.DismissPriorityMenu)
                        },
                        onPrioritySelected = { priority ->
                            viewModel.onEvent(
                                TaskListEvent.SetTaskPriority(
                                    taskItem.task.id,
                                    priority
                                )
                            )
                        },
                        onSubtaskCompletionClick = { subtask ->
                            viewModel.onEvent(TaskListEvent.ToggleSubtaskCompletion(subtask))
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                CollapsibleSectionHeader(
                    title = "COMPLETED",
                    isExpanded = state.isCompletedTasksVisible,
                    onToggle = { viewModel.onEvent(TaskListEvent.ToggleCompletedTasksVisibility) }
                )
            }

            if (state.isCompletedTasksVisible) {
                items(completedTasks, key = { it.task.id }) { taskItem ->
                    TaskItemCard(
                        taskItem = taskItem,
                        isSelectionMode = state.isSelectionMode,
                        isSelected = taskItem.task.id in state.selectedTaskIds,
                        isAnimatingCompletion = false,
                        isShowingSubtasks = state.isShowingSubtasks,
                        isPriorityMenuExpanded = state.activePriorityTaskId == taskItem.task.id,
                        onClick = {
                            if (state.isSelectionMode) {
                                viewModel.onEvent(TaskListEvent.ToggleTaskSelection(taskItem.task.id))
                            } else {
                                onTaskClick(taskItem.task.id)
                            }
                        },
                        onCompletionClick = {
                            if (state.isSelectionMode) {
                                viewModel.onEvent(TaskListEvent.ToggleTaskSelection(taskItem.task.id))
                            } else {
                                viewModel.onEvent(TaskListEvent.ToggleParentTaskCompletion(taskItem.task))
                            }
                        },
                        onPriorityClick = {
                            viewModel.onEvent(TaskListEvent.OpenPriorityMenu(taskItem.task.id))
                        },
                        onPriorityDismiss = {
                            viewModel.onEvent(TaskListEvent.DismissPriorityMenu)
                        },
                        onPrioritySelected = { priority ->
                            viewModel.onEvent(
                                TaskListEvent.SetTaskPriority(
                                    taskItem.task.id,
                                    priority
                                )
                            )
                        },
                        onSubtaskCompletionClick = { subtask ->
                            viewModel.onEvent(TaskListEvent.ToggleSubtaskCompletion(subtask))
                        }
                    )
                }
            }
        }
        if (state.isSortDialogVisible) {
            TaskSortDialog(
                initialTaskOrderField = state.taskOrderField,
                initialOrderType = state.orderType,
                onCancel = { viewModel.onEvent(TaskListEvent.DismissSortDialog) },
                onApply = { taskOrderField, orderType ->
                    viewModel.onEvent(TaskListEvent.SortTask(taskOrderField, orderType))
                }
            )
        }

    }
}

@Preview(apiLevel = 34, showBackground = true)
@Composable
fun PreviewTaskScreen() {
    MaterialTheme {
        TaskListScreen()
    }
}
