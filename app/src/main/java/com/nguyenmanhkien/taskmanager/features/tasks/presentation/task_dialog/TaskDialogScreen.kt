package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.RecurrenceRule
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components.ActionIconButton
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components.AddButton
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components.CategoryButton
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components.CategoryListPopup
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components.SubtasksColumn
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components.TransparentHintTextField
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.date_time_dialog.DateTimeDialog
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.launch
import java.util.Calendar


@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    initialCategoryId: Int? = null,
    viewModel: TaskDialogViewModel = koinViewModel()
) {
    val state by viewModel.dialogState
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val titleFocusRequester = remember { FocusRequester() }
    val subtaskFocusRequesters = remember(state.subtasks.size) {
        List(state.subtasks.size) { FocusRequester() }
    }
    var showTimePicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (initialCategoryId != null) {
            viewModel.onEvent(TaskDialogEvent.SetInitialCategoryId(initialCategoryId))
        }
        kotlinx.coroutines.delay(100)
        titleFocusRequester.requestFocus()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onEvent(TaskDialogEvent.DismissDialog)
        }
    }


    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is TaskDialogViewModel.UiEvent.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }

                is TaskDialogViewModel.UiEvent.DismissDialog -> {
                    onDismiss()
                }
            }
        }
    }

    val selectedCategoryName by remember {
        derivedStateOf {
            state.categories.find { it.id == state.categoryId }?.name ?: "No Category"
        }
    }

    val categoryNames by remember {
        derivedStateOf {
            state.categories.map { it.name }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        val view = LocalView.current

        fun hideKeyboardAndClearFocus() {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            val screenHeight = maxHeight
            val offsetFromBottom = screenHeight * 0.5f

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -offsetFromBottom)
                    .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth(0.9f)
                    .padding(6.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)
                ) {
                    TransparentHintTextField(
                        text = state.title.text,
                        hint = state.title.hint,
                        isHintVisible = state.title.isHintVisible,
                        onValueChange = { viewModel.onEvent(TaskDialogEvent.EnterTitle(it)) },
                        onFocusChange = { viewModel.onEvent(TaskDialogEvent.ChangeTitleFocus(it)) },
                        focusRequester = titleFocusRequester
                    )

                    if (state.subtasks.isNotEmpty()) {
                        SubtasksColumn(
                            subtasks = state.subtasks,
                            onSubtaskChange = { index, value ->
                                viewModel.onEvent(TaskDialogEvent.UpdateSubtaskText(index, value))
                            },
                            onSubtaskFocusChange = { index, focusState ->
                                viewModel.onEvent(
                                    TaskDialogEvent.ChangeSubtaskFocus(
                                        index,
                                        focusState
                                    )
                                )
                            },
                            onRemoveSubtask = { index ->
                                viewModel.onEvent(TaskDialogEvent.RemoveSubtask(index))
                            },
                            focusRequesters = subtaskFocusRequesters
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            CategoryButton(
                                categoryName = selectedCategoryName,
                                onClick = { viewModel.onEvent(TaskDialogEvent.ToggleCategoryPicker) }
                            )

                            if (state.showCategoryPicker) {
                                CategoryListPopup(
                                    categories = categoryNames,
                                    selectedCategory = selectedCategoryName,
                                    onCategorySelected = { categoryName ->
                                        val categoryId =
                                            state.categories.find { it.name == categoryName }?.id
                                        viewModel.onEvent(TaskDialogEvent.SelectCategory(categoryId))
                                    },
                                    onDismiss = { viewModel.onEvent(TaskDialogEvent.ToggleCategoryPicker) }
                                )
                            }
                        }

                        ActionIconButton(
                            icon = Icons.Default.DateRange,
                            onClick = {
                                coroutineScope.launch {
                                    hideKeyboardAndClearFocus()
                                    kotlinx.coroutines.delay(300)
                                    showTimePicker = true
                                }
                            }
                        )

                        ActionIconButton(
                            icon = Icons.Default.CheckCircle,
                            onClick = { viewModel.onEvent(TaskDialogEvent.AddSubtask) }
                        )

                        ActionIconButton(icon = Icons.Default.Menu)

                    }

                    AddButton(onClick = { viewModel.onEvent(TaskDialogEvent.SaveTask) })
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }

            if (showTimePicker) {
                val now = System.currentTimeMillis()
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.02f),
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.02f),
                                )
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showTimePicker = false }
                        )
                )
                DateTimeDialog(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    initialDateAt = state.dateAtStartOfDay ?: startOfDay(now),
                    initialTimeOffsetMillis = state.timeOffsetMillis ?: timeOffsetMillis(now),
                    initialDurationMillis = state.durationMillis,
                    initialRule = RecurrenceRule.fromJson(state.rrule),
                    onDateSelected = { timestamp ->
                        viewModel.onEvent(TaskDialogEvent.SelectDateAtStartOfDay(timestamp))
                    },
                    onTimeSelected = { offsetMillis ->
                        viewModel.onEvent(TaskDialogEvent.SelectTimeOffset(offsetMillis))
                    },
                    onDurationSelected = { durationMillis ->
                        viewModel.onEvent(TaskDialogEvent.SelectDuration(durationMillis))
                    },
                    onRecurrenceSelected = { rule ->
                        viewModel.onEvent(TaskDialogEvent.SetRecurrence(rule?.toJson()))
                    },
                    onDismiss = { showTimePicker = false },
                    onDone = { showTimePicker = false },
                )
            }
        }
    }
}

private fun startOfDay(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun timeOffsetMillis(timestamp: Long): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    return ((calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE)) * 60_000L
}

@Preview(apiLevel = 34, showBackground = true)
@Composable
fun PreviewTaskScreen() {
    MaterialTheme {
        AddTaskDialog(onDismiss = {})
    }
}

