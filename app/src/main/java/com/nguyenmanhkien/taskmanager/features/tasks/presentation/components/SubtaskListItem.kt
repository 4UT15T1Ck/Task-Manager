package com.nguyenmanhkien.taskmanager.features.tasks.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus

@Composable
fun SubtaskListItem(
    task: Task,
    onCompletionClick: () -> Unit
) {
    val isCompleted = task.status == TaskStatus.COMPLETED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = "Toggle subtask completion",
            tint = if (isCompleted) MaterialTheme.colorScheme.primary else Color(0xFF98A2B3),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onCompletionClick)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF344054),
            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}
