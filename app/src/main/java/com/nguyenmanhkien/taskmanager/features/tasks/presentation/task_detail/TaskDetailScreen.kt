package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.RecurrenceRule
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.RecurrenceType
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.components.DatePicker
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.components.DurationPicker
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.components.RecurrencePicker
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.components.TimePicker
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MAX_SUBTASKS = 20
private const val SUBTASK_PLACEHOLDER = "subtask"

@Composable
fun TaskDetailScreen(
    taskId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (Int) -> Unit,
    viewModel: TaskDetailViewModel = koinViewModel()
) {
    val state by viewModel.state
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(taskId) {
        viewModel.start(taskId)
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                TaskDetailViewModel.UiEvent.NavigateBack -> onNavigateBack()
                is TaskDetailViewModel.UiEvent.NavigateToTaskDetail -> {
                    onNavigateToTaskDetail(event.taskId)
                }
            }
        }
    }

    val taskWithSubtasks = state.taskWithSubtasks ?: return

    val parentTask = taskWithSubtasks.task
    val isCompleted = parentTask.status == TaskStatus.COMPLETED
    val coroutineScope = rememberCoroutineScope()
    val contentScrollState = rememberScrollState()

    val subtaskFocusRequesters = remember { mutableStateMapOf<Int, FocusRequester>() }
    val subtaskBringIntoViewRequesters =
        remember { mutableStateMapOf<Int, BringIntoViewRequester>() }
    val subtaskIds = remember(taskWithSubtasks.subtasks) { taskWithSubtasks.subtasks.map { it.id } }
    LaunchedEffect(state.pendingSubtaskFocusId, subtaskIds) {
        val subtaskId = state.pendingSubtaskFocusId ?: return@LaunchedEffect
        val requester = subtaskFocusRequesters[subtaskId] ?: return@LaunchedEffect
        val bringIntoViewRequester = subtaskBringIntoViewRequesters[subtaskId]
        requester.requestFocus()
        bringIntoViewRequester?.bringIntoView()
        delay(180)
        bringIntoViewRequester?.bringIntoView()
        viewModel.onEvent(TaskDetailEvent.ClearPendingSubtaskFocus)
    }

    val canAddSubtask = !isCompleted && taskWithSubtasks.subtasks.size < MAX_SUBTASKS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    viewModel.onEvent(TaskDetailEvent.CommitTitle)
                }
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Box {
                IconButton(onClick = { viewModel.onEvent(TaskDetailEvent.ToggleMenu) }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Task options")
                }
                DropdownMenu(
                    expanded = state.isMenuExpanded,
                    onDismissRequest = { viewModel.onEvent(TaskDetailEvent.DismissMenu) }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (isCompleted) "Mark as In Progress" else "Mark as Completed"
                            )
                        },
                        onClick = { viewModel.onEvent(TaskDetailEvent.ToggleTaskCompletion) }
                    )
                    DropdownMenuItem(
                        text = { Text("Make a Copy") },
                        onClick = { viewModel.onEvent(TaskDetailEvent.DuplicateTask) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { viewModel.onEvent(TaskDetailEvent.RequestTaskDeletion) }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(contentScrollState)
        ) {

            Box {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF1F4F8),
                    modifier = Modifier
                        .clickable(enabled = !isCompleted) {
                            viewModel.onEvent(TaskDetailEvent.ToggleCategoryPicker)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.categories.firstOrNull { it.id == parentTask.categoryId }?.name
                                ?: "No Category"
                        )
                    }
                }

                DropdownMenu(
                    expanded = state.isCategoryPickerVisible,
                    onDismissRequest = { viewModel.onEvent(TaskDetailEvent.ToggleCategoryPicker) }
                ) {
                    DropdownMenuItem(
                        text = { Text("No Category") },
                        onClick = { viewModel.onEvent(TaskDetailEvent.SelectCategory(null)) }
                    )
                    state.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = { viewModel.onEvent(TaskDetailEvent.SelectCategory(category.id)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = state.titleInput,
                onValueChange = { viewModel.onEvent(TaskDetailEvent.UpdateTitle(it)) },
                readOnly = isCompleted,
                singleLine = true,
                colors = borderlessTextFieldColors(),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            viewModel.onEvent(TaskDetailEvent.CommitTitle)
                        }
                    }
            )

            Spacer(modifier = Modifier.height(8.dp))

            taskWithSubtasks.subtasks.forEach { subtask ->
                val requester = subtaskFocusRequesters.getOrPut(subtask.id) { FocusRequester() }
                val bringIntoViewRequester = subtaskBringIntoViewRequesters.getOrPut(subtask.id) {
                    BringIntoViewRequester()
                }
                val isSubtaskCompleted = subtask.status == TaskStatus.COMPLETED

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.onEvent(
                                TaskDetailEvent.ToggleSubtaskCompletion(
                                    subtask.id
                                )
                            )
                        },
                        enabled = !isCompleted
                    ) {
                        Icon(
                            imageVector = if (isSubtaskCompleted || isCompleted) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.RadioButtonUnchecked
                            },
                            contentDescription = "Toggle subtask completion",
                            tint = if (isSubtaskCompleted || isCompleted) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                    }

                    TextField(
                        value = state.subtaskInputs[subtask.id]
                            ?: if (subtask.title == SUBTASK_PLACEHOLDER) "" else subtask.title,
                        onValueChange = {
                            viewModel.onEvent(TaskDetailEvent.UpdateSubtaskTitle(subtask.id, it))
                        },
                        readOnly = isCompleted,
                        singleLine = true,
                        colors = borderlessTextFieldColors(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (isSubtaskCompleted || isCompleted) {
                                TextDecoration.LineThrough
                            } else {
                                TextDecoration.None
                            }
                        ),
                        placeholder = { Text(SUBTASK_PLACEHOLDER, color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                viewModel.onEvent(TaskDetailEvent.SubmitSubtaskOnNext(subtask.id))
                            },
                            onDone = { viewModel.onEvent(TaskDetailEvent.CommitSubtaskTitle(subtask.id)) }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(requester)
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        bringIntoViewRequester.bringIntoView()
                                        delay(180)
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                } else {
                                    viewModel.onEvent(TaskDetailEvent.CommitSubtaskTitle(subtask.id))
                                }
                            }
                    )

                    if (!isCompleted) {
                        IconButton(onClick = {
                            viewModel.onEvent(
                                TaskDetailEvent.DeleteSubtask(
                                    subtask.id
                                )
                            )
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Delete subtask")
                        }
                    }

                }
            }

            if (!isCompleted) {
                TextButton(
                    enabled = canAddSubtask,
                    onClick = { viewModel.onEvent(TaskDetailEvent.AddSubtask) }
                ) {
                    Text("+ Add Sub-task")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            DateTimeSummaryRow(
                label = "Time",
                summary = formatTime(parentTask.startAt),
                enabled = !isCompleted,
                onClick = {
                    viewModel.onEvent(
                        TaskDetailEvent.ShowPickerDialog(
                            TaskDetailPickerSection.TIME
                        )
                    )
                }
            )
            HorizontalDivider()
            DateTimeSummaryRow(
                label = "Date",
                summary = formatDate(parentTask.startAt),
                enabled = !isCompleted,
                onClick = {
                    viewModel.onEvent(
                        TaskDetailEvent.ShowPickerDialog(
                            TaskDetailPickerSection.DATE
                        )
                    )
                }
            )
            HorizontalDivider()
            DateTimeSummaryRow(
                label = "Duration",
                summary = formatDuration(parentTask.startAt, parentTask.dueAt),
                enabled = !isCompleted,
                onClick = {
                    viewModel.onEvent(
                        TaskDetailEvent.ShowPickerDialog(
                            TaskDetailPickerSection.DURATION
                        )
                    )
                }
            )
            HorizontalDivider()
            DateTimeSummaryRow(
                label = "Reminder",
                summary = "No",
                enabled = !isCompleted,
                onClick = {
                    viewModel.onEvent(
                        TaskDetailEvent.ShowPickerDialog(
                            TaskDetailPickerSection.REMINDER
                        )
                    )
                }
            )
            HorizontalDivider()
            DateTimeSummaryRow(
                label = "Repeat Task",
                summary = formatRepeat(parentTask.rrule),
                enabled = !isCompleted,
                onClick = {
                    viewModel.onEvent(
                        TaskDetailEvent.ShowPickerDialog(
                            TaskDetailPickerSection.REPEAT
                        )
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isCompleted) {
                        viewModel.onEvent(TaskDetailEvent.OpenDescriptionEditor)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Description", style = MaterialTheme.typography.titleMedium)
                if (!isCompleted) {
                    Text("EDIT", color = MaterialTheme.colorScheme.primary)
                }
            }
            if (parentTask.description.isNullOrBlank()) {
                Text(
                    text = "No description",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Text(
                    text = parentTask.description,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
            }
        }
    }

    state.activePickerSection?.let { section ->
        val now = System.currentTimeMillis()
        val startAt = parentTask.startAt ?: now
        val initialDuration = if (parentTask.startAt != null && parentTask.dueAt != null) {
            (parentTask.dueAt - parentTask.startAt).coerceAtLeast(0L)
        } else {
            null
        }

        val initialDateAt = startOfDay(startAt)
        val initialTimeOffset = timeOffsetMillis(startAt)

        Dialog(
            onDismissRequest = { viewModel.onEvent(TaskDetailEvent.HidePickerDialog) },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 760.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    when (section) {
                        TaskDetailPickerSection.TIME -> {
                            TimePicker(
                                initialTimeOffsetMillis = initialTimeOffset,
                                onTimeSelected = {
                                    viewModel.onEvent(
                                        TaskDetailEvent.SelectTimeOffset(
                                            it
                                        )
                                    )
                                }
                            )
                        }

                        TaskDetailPickerSection.DATE -> {
                            DatePicker(
                                initialDateMillis = initialDateAt,
                                onDateSelected = {
                                    viewModel.onEvent(
                                        TaskDetailEvent.SelectDateAtStartOfDay(
                                            it
                                        )
                                    )
                                }
                            )
                        }

                        TaskDetailPickerSection.DURATION -> {
                            DurationPicker(
                                initialDurationMillis = initialDuration,
                                onDurationSelected = {
                                    viewModel.onEvent(
                                        TaskDetailEvent.SelectDuration(
                                            it
                                        )
                                    )
                                }
                            )
                        }

                        TaskDetailPickerSection.REMINDER -> {
                            ReminderPickerContent()
                        }

                        TaskDetailPickerSection.REPEAT -> {
                            RecurrencePicker(
                                initialRule = RecurrenceRule.fromJson(parentTask.rrule),
                                taskStartAt = startAt,
                                onRuleSelected = { rule ->
                                    viewModel.onEvent(TaskDetailEvent.SetRecurrence(rule?.toJson()))
                                },
                                onCancel = { viewModel.onEvent(TaskDetailEvent.HidePickerDialog) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.onEvent(TaskDetailEvent.HidePickerDialog) }) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }

    if (state.isDeletionRequest) {
        Dialog(
            onDismissRequest = { viewModel.onEvent(TaskDetailEvent.CancelTaskDeletion) },
            properties = DialogProperties(
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 760.dp)
            ) {
                Column (modifier = Modifier.padding(16.dp)) {
                    Text("Are you sure you want to delete this task?")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.onEvent(TaskDetailEvent.CancelTaskDeletion) }) {
                            Text("Cancel")
                        }
                        TextButton(onClick = { viewModel.onEvent(TaskDetailEvent.ConfirmTaskDeletion) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }

    if (state.isDescriptionEditorVisible) {
        TaskDescriptionEditor(
            title = parentTask.title,
            description = state.descriptionInput,
            onDescriptionChange = { viewModel.onEvent(TaskDetailEvent.UpdateDescription(it)) },
            onClose = { viewModel.onEvent(TaskDetailEvent.SaveDescriptionAndCloseEditor) }
        )
    }
}

@Composable
private fun DateTimeSummaryRow(
    label: String,
    summary: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Surface(
            color = Color(0xFFF1F4F8),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = summary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TaskDescriptionEditor(
    title: String,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            TextField(
                value = description,
                onValueChange = onDescriptionChange,
                colors = borderlessTextFieldColors(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = {
                    Text("Description")
                }
            )
        }
    }
}

@Composable
private fun ReminderPickerContent() {
    Text(
        text = "Reminder picker is pending. This section is reserved for reminder presets.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
private fun borderlessTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent
)

private fun formatDate(startAt: Long?): String {
    if (startAt == null) return "No"
    return DateFormat.getDateInstance(DateFormat.SHORT).format(Date(startAt))
}

private fun formatTime(startAt: Long?): String {
    if (startAt == null) return "No"
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(startAt))
}

private fun formatDuration(startAt: Long?, dueAt: Long?): String {
    if (startAt == null || dueAt == null) return "No"
    val durationMs = (dueAt - startAt).coerceAtLeast(0L)
    if (durationMs == 0L) return "No"

    val totalMinutes = (durationMs / 60_000L).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

private fun formatRepeat(recurrenceJson: String?): String {
    val rule = RecurrenceRule.fromJson(recurrenceJson) ?: return "No"
    val typeLabel = when (rule.type) {
        RecurrenceType.HOURLY -> "Hourly"
        RecurrenceType.DAILY -> "Daily"
        RecurrenceType.WEEKLY -> "Weekly"
        RecurrenceType.MONTHLY -> "Monthly"
        RecurrenceType.YEARLY -> "Yearly"
    }
    return if (rule.interval == 1) typeLabel else "$typeLabel x${rule.interval}"
}

private fun startOfDay(timestamp: Long): Long {
    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

private fun timeOffsetMillis(timestamp: Long): Long {
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    val hours = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(java.util.Calendar.MINUTE)
    return ((hours * 60) + minutes) * 60_000L
}