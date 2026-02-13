package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TimeFilter
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components.CategoryChip
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components.CollapsibleSectionHeader
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components.TaskItemCard
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components.TimeFilterChip
import org.koin.androidx.compose.koinViewModel

@Composable
fun TaskListScreen(
    onTaskClick: (Int) -> Unit = {},
    onCategorySelected: (Int?) -> Unit = {},
    viewModel: TaskListViewModel = koinViewModel()
) {
    val state by viewModel.state
    val inProgressTasks = state.tasks.filter { it.status == TaskStatus.IN_PROGRESS }
    val completedTasks = state.tasks.filter { it.status == TaskStatus.COMPLETED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Horizontal Category Chips
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

            items(count = state.categories.size) { index ->
                val category = state.categories[index]
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

        // Time Filter Chips Row
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

        // Task List with collapsible sections
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // IN PROGRESS Section
            item {
                CollapsibleSectionHeader(
                    title = "IN PROGRESS",
                    isExpanded = state.isPendingTasksVisible,
                    onToggle = { viewModel.onEvent(TaskListEvent.TogglePendingTasksVisibility) }
                )
            }

            if (state.isPendingTasksVisible) {
                items(inProgressTasks.size) { index ->
                    TaskItemCard(
                        task = inProgressTasks[index],
                        onClick = { onTaskClick(inProgressTasks[index].id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // COMPLETED Section
            item {
                CollapsibleSectionHeader(
                    title = "COMPLETED",
                    isExpanded = state.isCompletedTasksVisible,
                    onToggle = { viewModel.onEvent(TaskListEvent.ToggleCompletedTasksVisibility) }
                )
            }

            if (state.isCompletedTasksVisible) {
                items(completedTasks.size) { index ->
                    TaskItemCard(
                        task = completedTasks[index],
                        onClick = { onTaskClick(completedTasks[index].id) }
                    )
                }
            }
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