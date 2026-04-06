package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskWithSubtasks
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val COMPLETION_ICON_ANIMATION_MS = 500

@Composable
fun TaskItemCard(
    taskItem: TaskWithSubtasks,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    isAnimatingCompletion: Boolean,
    isShowingSubtasks: Boolean,
    isPriorityMenuExpanded: Boolean,
    onClick: () -> Unit,
    onCompletionClick: () -> Unit,
    onPriorityClick: () -> Unit,
    onPriorityDismiss: () -> Unit,
    onPrioritySelected: (TaskPriority) -> Unit,
    onSubtaskCompletionClick: (Task) -> Unit
) {
    val isCompleted = taskItem.task.status == TaskStatus.COMPLETED
    val showCompletedIcon = isAnimatingCompletion || isCompleted
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (isSelectionMode) {
                    SelectionSquare(
                        isSelected = isSelected,
                        onClick = onCompletionClick
                    )
                } else {
                    AnimatedContent(
                        targetState = showCompletedIcon,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(COMPLETION_ICON_ANIMATION_MS)) togetherWith
                                fadeOut(animationSpec = tween(COMPLETION_ICON_ANIMATION_MS))
                        }
                    ) { checked ->
                        Icon(
                            imageVector = if (checked) {
                                Icons.Filled.CheckCircle
                            } else {
                                Icons.Outlined.RadioButtonUnchecked
                            },
                            contentDescription = "Toggle completion",
                            tint = if (checked) MaterialTheme.colorScheme.primary else Color(0xFF98A2B3),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable(onClick = onCompletionClick)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(if (isSelectionMode) 10.dp else 16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = taskItem.task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1D2939),
                            textDecoration = if (isCompleted) {
                                TextDecoration.LineThrough
                            } else {
                                TextDecoration.None
                            },
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Box {
                            Text(
                                text = priorityLabel(taskItem.task.priority),
                                style = MaterialTheme.typography.labelMedium,
                                color = priorityColor(taskItem.task.priority),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable(onClick = onPriorityClick)
                            )

                            DropdownMenu(
                                expanded = isPriorityMenuExpanded,
                                onDismissRequest = onPriorityDismiss
                            ) {
                                TaskPriority.entries.forEach { priority ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = priorityLabel(priority),
                                                color = priorityColor(priority)
                                            )
                                        },
                                        onClick = { onPrioritySelected(priority) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TaskMetadataRow(taskItem = taskItem)

                    if (isShowingSubtasks && !isSelectionMode && taskItem.hasSubtasks) {
                        Spacer(modifier = Modifier.height(12.dp))
                        taskItem.subtasks.forEach { subtask ->
                            SubtaskListItem(
                                task = subtask,
                                onCompletionClick = { onSubtaskCompletionClick(subtask) }
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun SelectionSquare(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(24.dp)
            .border(
                width = 1.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF98A2B3),
                shape = RoundedCornerShape(6.dp)
            )
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun TaskMetadataRow(taskItem: TaskWithSubtasks) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = formatTaskStart(taskItem.task.startAt),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF667085)
        )

        taskDurationLabel(taskItem.task)?.let { durationLabel ->
            Text(
                text = durationLabel,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF667085)
            )
        }

        if (taskItem.hasReminder) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Has reminder",
                tint = Color(0xFF98A2B3),
                modifier = Modifier.size(14.dp)
            )
        }

        if (taskItem.isRecurring) {
            Icon(
                imageVector = Icons.Filled.Repeat,
                contentDescription = "Recurring task",
                tint = Color(0xFF98A2B3),
                modifier = Modifier.size(14.dp)
            )
        }

        if (taskItem.hasSubtasks) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CallSplit,
                    contentDescription = "Subtasks",
                    tint = Color(0xFF98A2B3),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${taskItem.completedSubtaskCount}/${taskItem.subtaskCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF667085)
                )
            }
        }
    }
}

private fun priorityLabel(priority: TaskPriority): String {
    return when (priority) {
        TaskPriority.NO_PRIORITY -> "NONE"
        TaskPriority.LOW -> "LOW"
        TaskPriority.MEDIUM -> "MED"
        TaskPriority.HIGH -> "HIGH"
    }
}

private fun priorityColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.NO_PRIORITY -> Color(0xFF98A2B3)
        TaskPriority.LOW -> Color(0xFF1F7A3D)
        TaskPriority.MEDIUM -> Color(0xFFF79009)
        TaskPriority.HIGH -> Color(0xFFD92D20)
    }
}

private fun formatTaskStart(startAt: Long?): String {
    if (startAt == null) {
        return "No start"
    }
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(startAt))
}

private fun taskDurationLabel(task: Task): String? {
    val startAt = task.startAt ?: return null
    val dueAt = task.dueAt ?: return null
    val durationMinutes = ((dueAt - startAt) / 60_000L).coerceAtLeast(0L)
    if (durationMinutes <= 0L) {
        return null
    }
    val hours = durationMinutes / 60
    val minutes = durationMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}
