package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.OrderType
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskOrderField

@Composable
fun TaskSortDialog(
    initialTaskOrderField: TaskOrderField,
    initialOrderType: OrderType,
    onCancel: () -> Unit,
    onApply: (TaskOrderField, OrderType) -> Unit
) {
    var selectedOrderField by remember(initialTaskOrderField) { mutableStateOf(initialTaskOrderField) }
    var selectedOrderType by remember(initialOrderType) { mutableStateOf(initialOrderType) }

    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort by",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Ascending",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = selectedOrderType == OrderType.ASCENDING,
                        onCheckedChange = { isAscending ->
                            selectedOrderType = if (isAscending) {
                                OrderType.ASCENDING
                            } else {
                                OrderType.DESCENDING
                            }
                        }
                    )
                }

                TaskOrderField.entries.chunked(2).forEach { rowFields ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowFields.forEach { field ->
                            SortOptionChip(
                                text = field.toLabel(),
                                isSelected = selectedOrderField == field,
                                onClick = { selectedOrderField = field },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onApply(selectedOrderField, selectedOrderType) }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun SortOptionChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color(0xFFE8E8E8)
            }
        ),
        modifier = modifier
            .height(50.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color.White else Color.DarkGray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

private fun TaskOrderField.toLabel(): String {
    return when (this) {
        TaskOrderField.DUE_DATE -> "Due date"
        TaskOrderField.CREATION_TIME -> "Creation"
        TaskOrderField.ALPHABET -> "Alphabet"
        TaskOrderField.PRIORITY -> "Priority"
    }
}

