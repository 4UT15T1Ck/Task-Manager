package com.nguyenmanhkien.taskmanager.features.tasks.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun DatePicker(
    modifier: Modifier = Modifier,
    initialDateMillis: Long = startOfDay(System.currentTimeMillis()),
    onDateSelected: (Long) -> Unit,
) {
    var selectedDateMillis by remember(initialDateMillis) {
        mutableLongStateOf(startOfDay(initialDateMillis))
    }
    // Drives which month the calendar shows — updated by both chips and calendar navigation
    var displayedMonthMillis by remember(initialDateMillis) {
        mutableLongStateOf(startOfDay(initialDateMillis))
    }

    LaunchedEffect(selectedDateMillis) {
        onDateSelected(selectedDateMillis)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CalendarPicker(
            selectedDateMillis = selectedDateMillis,
            displayedMonthMillis = displayedMonthMillis,
            onMonthChanged = { displayedMonthMillis = it },
            onDateSelected = {
                selectedDateMillis = startOfDay(it)
                displayedMonthMillis = startOfDay(it)
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        DayChipRow(
            selectedDateMillis = selectedDateMillis,
            onDaySelected = {
                selectedDateMillis = startOfDay(it)
                displayedMonthMillis = startOfDay(it) // moves calendar to chip's month
            },
        )
    }
}

@Composable
private fun DayChipRow(
    selectedDateMillis: Long,
    onDaySelected: (Long) -> Unit,
) {
    val today = remember { Calendar.getInstance() }
    val tomorrow = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
    }
    val threeDaysLater = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 3) }
    }
    val thisSunday = remember {
        Calendar.getInstance().apply {
            val currentDayOfWeek = get(Calendar.DAY_OF_WEEK)
            val daysUntilSunday = if (currentDayOfWeek == Calendar.SUNDAY) 7
            else Calendar.SUNDAY - currentDayOfWeek + 7
            add(Calendar.DAY_OF_MONTH, daysUntilSunday)
        }
    }
    val nextMonday = remember {
        Calendar.getInstance().apply {
            val currentDayOfWeek = get(Calendar.DAY_OF_WEEK)
            val daysUntilMonday = if (currentDayOfWeek < Calendar.MONDAY)
                Calendar.MONDAY - currentDayOfWeek
            else
                7 - currentDayOfWeek + Calendar.MONDAY
            add(Calendar.DAY_OF_MONTH, daysUntilMonday)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            FilterChip(
                selected = isSameDate(selectedDateMillis, today.timeInMillis),
                onClick = { onDaySelected(today.timeInMillis) },
                label = { Text("Today") },
            )
            FilterChip(
                selected = isSameDate(selectedDateMillis, tomorrow.timeInMillis),
                onClick = { onDaySelected(tomorrow.timeInMillis) },
                label = { Text("Tomorrow") },
            )
            FilterChip(
                selected = isSameDate(selectedDateMillis, threeDaysLater.timeInMillis),
                onClick = { onDaySelected(threeDaysLater.timeInMillis) },
                label = { Text("3 Days Later") },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            FilterChip(
                selected = isSameDate(selectedDateMillis, thisSunday.timeInMillis),
                onClick = { onDaySelected(thisSunday.timeInMillis) },
                label = { Text("This Sunday") },
            )
            FilterChip(
                selected = isSameDate(selectedDateMillis, nextMonday.timeInMillis),
                onClick = { onDaySelected(nextMonday.timeInMillis) },
                label = { Text("Next Monday") },
            )
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

@Preview(apiLevel = 34, showBackground = true)
@Composable
fun PreviewDatePickerComponent() {
    MaterialTheme {
        DatePicker(onDateSelected = { })
    }
}