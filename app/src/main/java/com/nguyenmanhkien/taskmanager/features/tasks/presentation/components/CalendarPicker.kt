package com.nguyenmanhkien.taskmanager.features.tasks.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun CalendarPicker(
    selectedDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    // External month control — if provided, CalendarPicker uses these instead of internal state
    displayedMonthMillis: Long? = null,
    onMonthChanged: ((Long) -> Unit)? = null,
) {
    val initialCal = remember(displayedMonthMillis ?: selectedDateMillis) {
        Calendar.getInstance().apply {
            timeInMillis = displayedMonthMillis ?: selectedDateMillis
        }
    }

    // Use internal state only when external control is not provided
    var internalMonth by remember(initialCal) { mutableIntStateOf(initialCal.get(Calendar.MONTH)) }
    var internalYear by remember(initialCal) { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }

    val currentMonth: Int
    val currentYear: Int

    if (displayedMonthMillis != null) {
        val cal = Calendar.getInstance().apply { timeInMillis = displayedMonthMillis }
        currentMonth = cal.get(Calendar.MONTH)
        currentYear = cal.get(Calendar.YEAR)
    } else {
        currentMonth = internalMonth
        currentYear = internalYear
    }

    fun navigateMonth(delta: Int) {
        val tempCal = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1)
            add(Calendar.MONTH, delta)
        }
        if (onMonthChanged != null) {
            onMonthChanged(tempCal.timeInMillis)
        } else {
            internalMonth = tempCal.get(Calendar.MONTH)
            internalYear = tempCal.get(Calendar.YEAR)
        }
    }

    var swipeOffset by remember { mutableFloatStateOf(0f) }

    // Overflow days from previous month
    val prevMonthCal = Calendar.getInstance().apply {
        set(currentYear, currentMonth, 1)
        add(Calendar.MONTH, -1)
    }
    val daysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val prevMonth = prevMonthCal.get(Calendar.MONTH)
    val prevYear = prevMonthCal.get(Calendar.YEAR)

    // Next month info
    val nextMonthCal = Calendar.getInstance().apply {
        set(currentYear, currentMonth, 1)
        add(Calendar.MONTH, 1)
    }
    val nextMonth = nextMonthCal.get(Calendar.MONTH)
    val nextYear = nextMonthCal.get(Calendar.YEAR)

    val daysInMonth = getDaysInMonth(currentYear, currentMonth)
    val firstDayOfWeek = getFirstDayOfWeek(currentYear, currentMonth)
    val weeksNeeded = kotlin.math.ceil((daysInMonth + firstDayOfWeek) / 7.0).toInt()

    Column(modifier = modifier.fillMaxWidth()) {
        // Month/Year Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigateMonth(-1) }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Month"
                )
            }
            Text(
                text = "${getMonthName(currentMonth)} $currentYear",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { navigateMonth(1) }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day of Week Headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(currentMonth, currentYear) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                swipeOffset > 100 -> navigateMonth(-1)
                                swipeOffset < -100 -> navigateMonth(1)
                            }
                            swipeOffset = 0f
                        }
                    ) { change, dragAmount ->
                        swipeOffset += dragAmount
                        change.consume()
                    }
                }
        ) {
            for (week in 0 until weeksNeeded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (dayOfWeek in 0 until 7) {
                        val position = week * 7 + dayOfWeek

                        when {
                            // Overflow from previous month
                            position < firstDayOfWeek -> {
                                val overflowDay = daysInPrevMonth - (firstDayOfWeek - 1 - dayOfWeek)
                                val isSelected =
                                    isSameDate(selectedDateMillis, prevYear, prevMonth, overflowDay)
                                OverflowDayCell(
                                    modifier = Modifier.weight(1f),
                                    day = overflowDay,
                                    isSelected = isSelected,
                                    onClick = {
                                        val cal = Calendar.getInstance().apply {
                                            set(prevYear, prevMonth, overflowDay)
                                        }
                                        onDateSelected(cal.timeInMillis)
                                        navigateMonth(-1)
                                    }
                                )
                            }

                            // Overflow into next month
                            position >= firstDayOfWeek + daysInMonth -> {
                                val overflowDay = position - (firstDayOfWeek + daysInMonth) + 1
                                val isSelected =
                                    isSameDate(selectedDateMillis, nextYear, nextMonth, overflowDay)
                                OverflowDayCell(
                                    day = overflowDay,
                                    modifier = Modifier.weight(1f),
                                    isSelected = isSelected,
                                    onClick = {
                                        val cal = Calendar.getInstance().apply {
                                            set(nextYear, nextMonth, overflowDay)
                                        }
                                        onDateSelected(cal.timeInMillis)
                                        navigateMonth(1)
                                    }
                                )
                            }

                            // Current month day
                            else -> {
                                val day = position - firstDayOfWeek + 1
                                val isSelected =
                                    isSameDate(selectedDateMillis, currentYear, currentMonth, day)
                                CurrentDayCell(
                                    day = day,
                                    modifier = Modifier.weight(1f),
                                    isSelected = isSelected,
                                    onClick = {
                                        val cal = Calendar.getInstance().apply {
                                            set(currentYear, currentMonth, day)
                                        }
                                        onDateSelected(cal.timeInMillis)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentDayCell(
    day: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Text(text = day.toString(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun OverflowDayCell(
    day: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
        }
    }
}

// ─── Date helper functions ───────────────────────────────────────────────────

internal fun getMonthName(month: Int): String {
    return listOf(
        "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
        "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    )[month]
}

internal fun getDaysInMonth(year: Int, month: Int): Int {
    return Calendar.getInstance().apply { set(year, month, 1) }
        .getActualMaximum(Calendar.DAY_OF_MONTH)
}

internal fun getFirstDayOfWeek(year: Int, month: Int): Int {
    return Calendar.getInstance().apply { set(year, month, 1) }.get(Calendar.DAY_OF_WEEK) - 1
}

internal fun isSameDate(millis1: Long, millis2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

internal fun isSameDate(millis: Long, year: Int, month: Int, day: Int): Boolean {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return cal.get(Calendar.YEAR) == year &&
            cal.get(Calendar.MONTH) == month &&
            cal.get(Calendar.DAY_OF_MONTH) == day
}