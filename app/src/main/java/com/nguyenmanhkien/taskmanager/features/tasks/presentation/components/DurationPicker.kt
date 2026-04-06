package com.nguyenmanhkien.taskmanager.features.tasks.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private const val MILLIS_PER_MINUTE = 60_000L

@Composable
fun DurationPicker(
    modifier: Modifier = Modifier,
    initialDurationMillis: Long? = null,
    onDurationSelected: (Long) -> Unit,
) {
    var selectedDurationMillis by remember(initialDurationMillis) {
        mutableLongStateOf(initialDurationMillis ?: 0L)
    }

    val durationOptions = remember {
        listOf(
            "None" to (0 * MILLIS_PER_MINUTE),
            "15 minutes" to (15 * MILLIS_PER_MINUTE),
            "30 minutes" to (30 * MILLIS_PER_MINUTE),
            "1 hour" to (60 * MILLIS_PER_MINUTE),
            "2 hours" to (120 * MILLIS_PER_MINUTE),
            "3 hours" to (180 * MILLIS_PER_MINUTE),
            "4 hours" to (240 * MILLIS_PER_MINUTE),
            "8 hours" to (480 * MILLIS_PER_MINUTE),
        )
    }

    LaunchedEffect(selectedDurationMillis) {
        if (selectedDurationMillis >= 0L) {
            onDurationSelected(selectedDurationMillis)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (rowIndex in 0 until 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val leftIndex = rowIndex
                if (leftIndex < durationOptions.size) {
                    val (label, duration) = durationOptions[leftIndex]
                    FilterChip(
                        selected = selectedDurationMillis == duration,
                        onClick = { selectedDurationMillis = duration },
                        label = {
                            Text(
                                text = label,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                val rightIndex = rowIndex + 4
                if (rightIndex < durationOptions.size) {
                    val (label, duration) = durationOptions[rightIndex]
                    FilterChip(
                        selected = selectedDurationMillis == duration,
                        onClick = { selectedDurationMillis = duration },
                        label = {
                            Text(
                                text = label,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Preview(apiLevel = 34, showBackground = true)
@Composable
fun PreviewDurationPickerComponent() {
    MaterialTheme {
        DurationPicker(onDurationSelected = { })
    }
}
