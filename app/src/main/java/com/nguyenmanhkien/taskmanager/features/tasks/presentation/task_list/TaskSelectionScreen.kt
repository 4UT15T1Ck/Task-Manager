package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TimeFilter
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components.CategoryChip
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components.TaskItemCard
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components.TimeFilterChip

@Composable
fun TaskSelectionScreen(
    onNavigateBack: () -> Unit,
    onCategorySelected: (Int?) -> Unit = {},
    viewModel: TaskListViewModel
) {
    val state by viewModel.state
    val selectedTaskIds = state.selectedTaskIds
    val hasSelectedTasks = selectedTaskIds.isNotEmpty()

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showCompleteConfirmDialog by remember { mutableStateOf(false) }
    var pendingCategoryId by remember { mutableStateOf<Int?>(null) }
    var hasPendingCategorySelection by remember { mutableStateOf(false) }

    val inProgressTasks = state.taskItems.filter { it.task.status == TaskStatus.IN_PROGRESS }
    val completedTasks = state.taskItems.filter { it.task.status == TaskStatus.COMPLETED }

    val inProgressTaskIds = inProgressTasks.map { it.task.id }
    val completedTaskIds = completedTasks.map { it.task.id }

    val isInProgressSelected = inProgressTaskIds.isNotEmpty() &&
        inProgressTaskIds.all { it in selectedTaskIds }
    val isCompletedSelected = completedTaskIds.isNotEmpty() &&
        completedTaskIds.all { it in selectedTaskIds }

    fun openCategoryDialog() {
        val selectedCategories = state.taskItems
            .asSequence()
            .filter { it.task.id in selectedTaskIds }
            .map { it.task.categoryId }
            .distinct()
            .toList()

        hasPendingCategorySelection = selectedCategories.size == 1
        pendingCategoryId = selectedCategories.singleOrNull()
        showCategoryDialog = true
    }

    fun requestExitSelectionMode() {
        viewModel.onEvent(TaskListEvent.ExitSelectionMode)
    }

    BackHandler {
        requestExitSelectionMode()
    }

    LaunchedEffect(state.isSelectionMode) {
        if (!state.isSelectionMode) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        requestExitSelectionMode()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "${selectedTaskIds.size} selected",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SelectionActionButton(
                    modifier = Modifier.weight(1f),
                    enabled = hasSelectedTasks,
                    onClick = { openCategoryDialog() },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = "Change category"
                        )
                    }
                )

                SelectionActionButton(
                    modifier = Modifier.weight(1f),
                    enabled = hasSelectedTasks,
                    onClick = { showDeleteConfirmDialog = true },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete selected tasks"
                        )
                    }
                )

                SelectionActionButton(
                    modifier = Modifier.weight(1f),
                    enabled = hasSelectedTasks,
                    onClick = { showCompleteConfirmDialog = true },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Mark selected tasks as completed"
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
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

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    SelectionSectionHeader(
                        title = "IN PROGRESS",
                        isSelected = isInProgressSelected,
                        onToggle = {
                            viewModel.onEvent(TaskListEvent.ToggleSectionSelection(inProgressTaskIds))
                        }
                    )
                }

                items(inProgressTasks, key = { it.task.id }) { taskItem ->
                    TaskItemCard(
                        taskItem = taskItem,
                        isSelectionMode = true,
                        isSelected = taskItem.task.id in selectedTaskIds,
                        isAnimatingCompletion = false,
                        isShowingSubtasks = state.isShowingSubtasks,
                        isPriorityMenuExpanded = state.activePriorityTaskId == taskItem.task.id,
                        onClick = {
                            viewModel.onEvent(TaskListEvent.ToggleTaskSelection(taskItem.task.id))
                        },
                        onCompletionClick = {
                            viewModel.onEvent(TaskListEvent.ToggleTaskSelection(taskItem.task.id))
                        },
                        onPriorityClick = {
                            viewModel.onEvent(TaskListEvent.OpenPriorityMenu(taskItem.task.id))
                        },
                        onPriorityDismiss = {
                            viewModel.onEvent(TaskListEvent.DismissPriorityMenu)
                        },
                        onPrioritySelected = { priority ->
                            viewModel.onEvent(
                                TaskListEvent.SetTaskPriority(taskItem.task.id, priority)
                            )
                        },
                        onSubtaskCompletionClick = {}
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    SelectionSectionHeader(
                        title = "COMPLETED",
                        isSelected = isCompletedSelected,
                        onToggle = {
                            viewModel.onEvent(TaskListEvent.ToggleSectionSelection(completedTaskIds))
                        }
                    )
                }

                items(completedTasks, key = { it.task.id }) { taskItem ->
                    TaskItemCard(
                        taskItem = taskItem,
                        isSelectionMode = true,
                        isSelected = taskItem.task.id in selectedTaskIds,
                        isAnimatingCompletion = false,
                        isShowingSubtasks = state.isShowingSubtasks,
                        isPriorityMenuExpanded = state.activePriorityTaskId == taskItem.task.id,
                        onClick = {
                            viewModel.onEvent(TaskListEvent.ToggleTaskSelection(taskItem.task.id))
                        },
                        onCompletionClick = {
                            viewModel.onEvent(TaskListEvent.ToggleTaskSelection(taskItem.task.id))
                        },
                        onPriorityClick = {
                            viewModel.onEvent(TaskListEvent.OpenPriorityMenu(taskItem.task.id))
                        },
                        onPriorityDismiss = {
                            viewModel.onEvent(TaskListEvent.DismissPriorityMenu)
                        },
                        onPrioritySelected = { priority ->
                            viewModel.onEvent(
                                TaskListEvent.SetTaskPriority(taskItem.task.id, priority)
                            )
                        },
                        onSubtaskCompletionClick = {}
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Change category") },
            text = {
                val optionHeight = 48.dp
                val optionsCount = state.categories.size + 1
                val maxVisibleItems = 6
                val maxHeight = optionHeight * minOf(optionsCount, maxVisibleItems)

                LazyColumn(
                    modifier = Modifier.heightIn(max = maxHeight)
                ) {
                    item {
                        CategoryOption(
                            text = "No Category",
                            isSelected = hasPendingCategorySelection && pendingCategoryId == null,
                            onClick = {
                                hasPendingCategorySelection = true
                                pendingCategoryId = null
                            }
                        )
                    }

                    items(state.categories, key = { it.id }) { category ->
                        CategoryOption(
                            text = category.name,
                            isSelected = hasPendingCategorySelection && pendingCategoryId == category.id,
                            onClick = {
                                hasPendingCategorySelection = true
                                pendingCategoryId = category.id
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = hasPendingCategorySelection,
                    onClick = {
                        showCategoryDialog = false
                        viewModel.onEvent(TaskListEvent.ChangeSelectedTasksCategory(pendingCategoryId))
                    }
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete selected tasks?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.onEvent(TaskListEvent.DeleteSelectedTasks)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCompleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteConfirmDialog = false },
            title = { Text("Mark selected tasks as completed?") },
            text = { Text("Already completed tasks are ignored.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCompleteConfirmDialog = false
                        viewModel.onEvent(TaskListEvent.CompleteSelectedTasks)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SelectionActionButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        val alpha = if (enabled) 1f else 0.4f
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(4.dp)) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            ) {
                icon()
            }
        }
    }
}

@Composable
private fun SelectionSectionHeader(
    title: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF98A2B3),
                            shape = RoundedCornerShape(5.dp)
                        )
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(5.dp)
                        )
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Section selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected category",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
