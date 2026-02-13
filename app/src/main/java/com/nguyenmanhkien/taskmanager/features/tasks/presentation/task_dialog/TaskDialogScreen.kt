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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components.ActionIconButton
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components.AddButton
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components.CategoryButton
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components.CategoryListPopup
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components.SubtasksColumn
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components.TransparentHintTextField
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel


@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    initialCategoryId: Int? = null,
    viewModel: TaskDialogViewModel = koinViewModel()
) {
    val state by viewModel.dialogState
    val snackbarHostState = remember { SnackbarHostState() }
    val titleFocusRequester = remember { FocusRequester() }
    val subtaskFocusRequesters = remember(state.subtasks.size) {
        List(state.subtasks.size) { FocusRequester() }
    }

    LaunchedEffect(Unit) {
        if (initialCategoryId != null) {
            viewModel.setInitialCategoryId(initialCategoryId)
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
                    snackbarHostState.showSnackbar(message = event.message)
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

                        ActionIconButton(icon = Icons.Default.DateRange)

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
        }
    }
}

@Preview(apiLevel = 34, showBackground = true)
@Composable
fun PreviewTaskScreen() {
    MaterialTheme {
        AddTaskDialog(onDismiss = {})
    }
}

