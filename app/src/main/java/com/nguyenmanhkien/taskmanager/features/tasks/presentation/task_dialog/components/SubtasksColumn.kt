package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.unit.dp
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.TaskTextFieldState
import kotlinx.coroutines.delay

@Composable
fun SubtasksColumn(
    subtasks: List<TaskTextFieldState>,
    onSubtaskChange: (Int, String) -> Unit,
    onSubtaskFocusChange: (Int, FocusState) -> Unit,
    onRemoveSubtask: (Int) -> Unit,
    modifier: Modifier = Modifier,
    focusRequesters: List<FocusRequester> = emptyList()
) {
    // Calculate height: max 3 items visible, then scroll
    val itemHeight = 52.dp
    val maxVisibleItems = 3
    val actualItems = subtasks.size
    val maxHeight = if (actualItems > maxVisibleItems) {
        itemHeight * maxVisibleItems
    } else {
        itemHeight * actualItems
    }

    val listState = rememberLazyListState()

    // Scroll to and focus the last item when a new subtask is added
    LaunchedEffect(subtasks.size) {
        if (subtasks.isNotEmpty() && focusRequesters.isNotEmpty()) {
            val lastIndex = subtasks.size - 1
            // Scroll to the last item first
            if (subtasks.size > maxVisibleItems) {
                listState.animateScrollToItem(lastIndex)
            }
            // Small delay to ensure scrolling completes before focusing
            delay(100)
            focusRequesters.lastOrNull()?.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(subtasks.size) { index ->
                SubtaskField(
                    text = subtasks[index].text,
                    hint = subtasks[index].hint,
                    isHintVisible = subtasks[index].isHintVisible,
                    onValueChange = { value -> onSubtaskChange(index, value) },
                    onFocusChange = { focusState -> onSubtaskFocusChange(index, focusState) },
                    onRemove = { onRemoveSubtask(index) },
                    focusRequester = focusRequesters.getOrNull(index)
                )
            }
        }
    }
}

