package com.nguyenmanhkien.taskmanager.features.tasks.presentation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.EndCondition
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.MonthlyPattern
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.RecurrenceDay
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.RecurrenceRule
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.RecurrenceType
import kotlinx.coroutines.delay
import java.util.Calendar

private enum class EndConditionUi {
    ENDLESS, ON_DATE, REPEAT_COUNT
}

@Composable
fun RecurrencePicker(
    modifier: Modifier = Modifier,
    initialRule: RecurrenceRule? = null,
    taskStartAt: Long,
    onRuleSelected: (RecurrenceRule?) -> Unit,
    onCancel: () -> Unit,
) {
    val taskCalendar = remember(taskStartAt) {
        Calendar.getInstance().apply { timeInMillis = taskStartAt }
    }

    // ── State ────────────────────────────────────────────────────────────
    var selectedType by remember { mutableStateOf(initialRule?.type) }
    var interval by remember { mutableIntStateOf(initialRule?.interval ?: 1) }

    val selectedDays = remember {
        mutableStateListOf<RecurrenceDay>().apply {
            if (initialRule?.type == RecurrenceType.WEEKLY && initialRule.daysOfWeek.isNotEmpty()) {
                addAll(initialRule.daysOfWeek)
            } else {
                add(RecurrenceDay.fromCalendarValue(taskCalendar.get(Calendar.DAY_OF_WEEK)))
            }
        }
    }

    var monthlyPattern by remember {
        mutableStateOf(initialRule?.monthlyPattern ?: MonthlyPattern.SAME_DATE)
    }

    var endConditionType by remember {
        mutableStateOf(
            when (initialRule?.endCondition) {
                is EndCondition.Endless, null -> EndConditionUi.ENDLESS
                is EndCondition.OnDate -> EndConditionUi.ON_DATE
                is EndCondition.RepeatCount -> EndConditionUi.REPEAT_COUNT
            }
        )
    }

    val tomorrow = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
    }
    var onDateMs by remember {
        mutableLongStateOf(
            (initialRule?.endCondition as? EndCondition.OnDate)?.timestamp ?: tomorrow
        )
    }
    var repeatCount by remember {
        mutableIntStateOf(
            (initialRule?.endCondition as? EndCondition.RepeatCount)?.count ?: 2
        )
    }

    var showDateDialog by remember { mutableStateOf(false) }
    var showCountDialog by remember { mutableStateOf(false) }

    // ── Build and emit ───────────────────────────────────────────────────
    fun buildRuleOrNull(): RecurrenceRule? {
        val type = selectedType ?: return null
        if (interval < 1) return null

        val days = if (type == RecurrenceType.WEEKLY) {
            if (selectedDays.isEmpty()) return null
            selectedDays.toSet()
        } else emptySet()

        val monthly = if (type == RecurrenceType.MONTHLY) monthlyPattern
        else MonthlyPattern.SAME_DATE

        val endCondition = when (endConditionType) {
            EndConditionUi.ENDLESS -> EndCondition.Endless
            EndConditionUi.ON_DATE -> EndCondition.OnDate(onDateMs)
            EndConditionUi.REPEAT_COUNT -> EndCondition.RepeatCount(repeatCount)
        }

        return RecurrenceRule(
            type = type,
            interval = interval,
            daysOfWeek = days,
            monthlyPattern = monthly,
            endCondition = endCondition,
        )
    }

    LaunchedEffect(
        selectedType, interval, selectedDays.toList(),
        monthlyPattern, endConditionType, onDateMs, repeatCount
    ) {
        onRuleSelected(buildRuleOrNull())
    }

    // ── UI ───────────────────────────────────────────────────────────────
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 1. Type selector grid (2×3)
        TypeSelectorGrid(
            selectedType = selectedType,
            onTypeSelected = { selectedType = it }
        )

        if (selectedType != null) {
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Interval section
            IntervalSection(
                interval = interval,
                type = selectedType!!,
                onIntervalChange = { interval = it }
            )

            // 3. Repeat on (WEEKLY / MONTHLY)
            if (selectedType == RecurrenceType.WEEKLY) {
                Spacer(modifier = Modifier.height(16.dp))
                WeeklyDaySelector(
                    selectedDays = selectedDays,
                    onDayToggled = { day ->
                        if (day in selectedDays) {
                            if (selectedDays.size > 1) selectedDays.remove(day)
                        } else {
                            selectedDays.add(day)
                        }
                    }
                )
            }

            if (selectedType == RecurrenceType.MONTHLY) {
                Spacer(modifier = Modifier.height(16.dp))
                MonthlyPatternSelector(
                    taskCalendar = taskCalendar,
                    selectedPattern = monthlyPattern,
                    onPatternSelected = { monthlyPattern = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Repeat ends
            EndConditionSection(
                selectedType = endConditionType,
                onNeverSelected = { endConditionType = EndConditionUi.ENDLESS },
                onDateChipClick = { showDateDialog = true },
                onCountChipClick = { showCountDialog = true },
            )
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────
    if (showDateDialog) {
        EndDatePickerDialog(
            initialDateMs = onDateMs,
            onDateConfirmed = { ms ->
                onDateMs = ms
                endConditionType = EndConditionUi.ON_DATE
                showDateDialog = false
            },
            onDismiss = { showDateDialog = false }
        )
    }

    if (showCountDialog) {
        RepeatCountDialog(
            initialCount = repeatCount,
            onCountConfirmed = { count ->
                repeatCount = count
                endConditionType = EndConditionUi.REPEAT_COUNT
                showCountDialog = false
            },
            onDismiss = { showCountDialog = false }
        )
    }
}

// ─── Type selector (2 rows × 3 columns) ─────────────────────────────────────

@Composable
private fun TypeSelectorGrid(
    selectedType: RecurrenceType?,
    onTypeSelected: (RecurrenceType?) -> Unit,
) {
    val options: List<Pair<RecurrenceType?, String>> = listOf(
        null to "None",
        RecurrenceType.HOURLY to "Hourly",
        RecurrenceType.DAILY to "Daily",
        RecurrenceType.WEEKLY to "Weekly",
        RecurrenceType.MONTHLY to "Monthly",
        RecurrenceType.YEARLY to "Yearly",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.take(3).forEach { (type, label) ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(label) },
                modifier = Modifier.weight(1f),
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.drop(3).forEach { (type, label) ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(label) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ─── Interval section ────────────────────────────────────────────────────────

@Composable
private fun IntervalSection(
    interval: Int,
    type: RecurrenceType,
    onIntervalChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Repeat every", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(1.dp, Color.DarkGray, RoundedCornerShape(25))
                .height(34.dp)
                .padding(horizontal = 2.dp)
        ) {
        RepeatingIconButton(
            onClick = { if (interval > 1) onIntervalChange(interval - 1) },
            enabled = interval > 1,
            modifier = Modifier.size(30.dp),
        ) {
            Icon(
                Icons.Filled.Remove,
                contentDescription = "Decrease",
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = "$interval ${typeUnitLabel(type, interval)}",
            modifier = Modifier.padding(horizontal = 2.dp),
            style = MaterialTheme.typography.titleSmall,
        )
        RepeatingIconButton(
            onClick = { if (interval < 100) onIntervalChange(interval + 1) },
            enabled = interval < 100,
            modifier = Modifier.size(30.dp),
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Increase",
                modifier = Modifier.size(16.dp),
            )
        }
        }
    }
}

private fun typeUnitLabel(type: RecurrenceType, count: Int): String {
    val singular = when (type) {
        RecurrenceType.HOURLY -> "hour"
        RecurrenceType.DAILY -> "day"
        RecurrenceType.WEEKLY -> "week"
        RecurrenceType.MONTHLY -> "month"
        RecurrenceType.YEARLY -> "year"
    }
    return if (count == 1) singular else singular + "s"
}

// ─── Weekly day selector ─────────────────────────────────────────────────────

@Composable
private fun WeeklyDaySelector(
    selectedDays: List<RecurrenceDay>,
    onDayToggled: (RecurrenceDay) -> Unit,
) {
    val displayOrder = listOf(
        RecurrenceDay.MONDAY, RecurrenceDay.TUESDAY, RecurrenceDay.WEDNESDAY,
        RecurrenceDay.THURSDAY, RecurrenceDay.FRIDAY, RecurrenceDay.SATURDAY,
        RecurrenceDay.SUNDAY,
    )

    Text("Repeat on", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        displayOrder.forEach { day ->
            WeeklyDayChip(
                label = day.shortLabel(),
                selected = day in selectedDays,
                onClick = { onDayToggled(day) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WeeklyDayChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = if (selected) colors.secondaryContainer else colors.surface
    val contentColor = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant
    val borderColor = if (selected) colors.secondaryContainer else colors.outline

    Surface(
        modifier = modifier.height(40.dp),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = selected,
                    role = Role.Checkbox,
                    onValueChange = { onClick() },
                )
                .padding(horizontal = 2.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun RecurrenceDay.shortLabel(): String = when (this) {
    RecurrenceDay.MONDAY -> "Mon"
    RecurrenceDay.TUESDAY -> "Tue"
    RecurrenceDay.WEDNESDAY -> "Wed"
    RecurrenceDay.THURSDAY -> "Thu"
    RecurrenceDay.FRIDAY -> "Fri"
    RecurrenceDay.SATURDAY -> "Sat"
    RecurrenceDay.SUNDAY -> "Sun"
}

// ─── Monthly pattern selector ────────────────────────────────────────────────

@Composable
private fun MonthlyPatternSelector(
    taskCalendar: Calendar,
    selectedPattern: MonthlyPattern,
    onPatternSelected: (MonthlyPattern) -> Unit,
) {
    val dayOfMonth = taskCalendar.get(Calendar.DAY_OF_MONTH)
    val maxDay = taskCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val options = remember(dayOfMonth, maxDay) {
        buildList {
            add(MonthlyPattern.SAME_DATE to "Same date")
            add(MonthlyPattern.SAME_WEEK_DAY_PATTERN to "Same weekday")
            if (dayOfMonth == 1) add(MonthlyPattern.START_OF_MONTH to "Start of month")
            if (dayOfMonth == maxDay) add(MonthlyPattern.END_OF_MONTH to "End of month")
        }
    }

    Text("Repeat on", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (pattern, label) ->
            FilterChip(
                selected = selectedPattern == pattern,
                onClick = { onPatternSelected(pattern) },
                label = { Text(label) },
            )
        }
    }
}

// ─── End condition section ───────────────────────────────────────────────────

@Composable
private fun EndConditionSection(
    selectedType: EndConditionUi,
    onNeverSelected: () -> Unit,
    onDateChipClick: () -> Unit,
    onCountChipClick: () -> Unit,
) {
    Text("Repeat ends", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedType == EndConditionUi.ENDLESS,
            onClick = onNeverSelected,
            label = { Text("Never") },
        )
        FilterChip(
            selected = selectedType == EndConditionUi.ON_DATE,
            onClick = onDateChipClick,
            label = { Text("A date") },
        )
        FilterChip(
            selected = selectedType == EndConditionUi.REPEAT_COUNT,
            onClick = onCountChipClick,
            label = { Text("Repeat count") },
        )
    }
}

// ─── End-date picker dialog ──────────────────────────────────────────────────

@Composable
private fun EndDatePickerDialog(
    initialDateMs: Long,
    onDateConfirmed: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedDate by remember { mutableLongStateOf(initialDateMs) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick end date") },
        text = {
            CalendarPicker(
                selectedDateMillis = selectedDate,
                onDateSelected = { selectedDate = it },
            )
        },
        confirmButton = {
            TextButton(onClick = { onDateConfirmed(selectedDate) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

// ─── Repeat-count dialog ─────────────────────────────────────────────────────

@Composable
private fun RepeatCountDialog(
    initialCount: Int,
    onCountConfirmed: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var count by remember { mutableIntStateOf(initialCount) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Repeat count") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RepeatingIconButton(
                    onClick = { if (count > 2) count-- },
                    enabled = count > 2,
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                }
                Text(
                    text = "$count counts",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
                RepeatingIconButton(
                    onClick = { if (count < 100) count++ },
                    enabled = count < 100,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCountConfirmed(count) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

// ─── Repeating icon button (tap + long-press repeat) ─────────────────────────

@Composable
private fun RepeatingIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed, enabled) {
        if (isPressed && enabled) {
            delay(400L)
            while (true) {
                currentOnClick()
                delay(80L)
            }
        }
    }

    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
    ) {
        content()
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(apiLevel = 34, showBackground = true)
@Composable
fun PreviewRecurrencePickerComponent() {
    MaterialTheme {
        RecurrencePicker(
            initialRule = RecurrenceRule.default(RecurrenceType.WEEKLY, System.currentTimeMillis()),
            taskStartAt = System.currentTimeMillis(),
            onRuleSelected = {},
            onCancel = {}
        )
    }
}
