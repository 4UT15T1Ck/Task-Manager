package com.nguyenmanhkien.taskmanager.features.tasks.presentation.date_time_dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.RecurrenceRule
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.RecurrenceType
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.components.DatePicker
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.components.DurationPicker
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.components.RecurrencePicker
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.components.TimePicker
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private enum class DateTimeSection(val title: String) {
    TIME("Time"),
    DATE("Date"),
    DURATION("Duration"),
    REMINDER("Reminder"),
    REPEAT("Repeat")
}

@Composable
fun DateTimeDialog(
    modifier: Modifier = Modifier,
    initialDateAt: Long = System.currentTimeMillis(),
    initialTimeOffsetMillis: Long = 0L,
    initialDurationMillis: Long? = null,
    initialRule: RecurrenceRule? = null,
    onDateSelected: (Long) -> Unit = {},
    onTimeSelected: (Long) -> Unit = {},
    onDurationSelected: (Long) -> Unit = {},
    onRecurrenceSelected: (RecurrenceRule?) -> Unit = {},
    onDismiss: () -> Unit = {},
    onDone: () -> Unit = onDismiss,
) {
    val selectedSection = remember { mutableStateOf(DateTimeSection.TIME) }
    val sectionSummaries =
        remember(initialDateAt, initialTimeOffsetMillis, initialDurationMillis, initialRule) {
            mapOf(
                DateTimeSection.TIME to formatTimeOffset(initialTimeOffsetMillis),
                DateTimeSection.DATE to formatLocalDate(initialDateAt),
                DateTimeSection.DURATION to formatDuration(initialDurationMillis),
                DateTimeSection.REMINDER to "Off",
                DateTimeSection.REPEAT to formatRepeat(initialRule),
            )
        }

    Surface(
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth(0.94f)
            .heightIn(max = 760.dp),
        tonalElevation = 20.dp,
    ) {
        Column(
            modifier = Modifier
                .background(color = Color.White)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedSection.value.title,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.weight(1f, fill = false)) {
                when (selectedSection.value) {
                    DateTimeSection.TIME -> {
                        TimePicker(
                            initialTimeOffsetMillis = initialTimeOffsetMillis,
                            onTimeSelected = onTimeSelected,
                        )
                    }

                    DateTimeSection.DATE -> {
                        DatePicker(
                            initialDateMillis = initialDateAt,
                            onDateSelected = onDateSelected,
                        )
                    }

                    DateTimeSection.DURATION -> {
                        DurationPicker(
                            initialDurationMillis = initialDurationMillis,
                            onDurationSelected = onDurationSelected,
                        )
                    }

                    DateTimeSection.REMINDER -> {
                        ReminderPickerPlaceholder()
                    }

                    DateTimeSection.REPEAT -> {
                        RecurrencePicker(
                            initialRule = initialRule,
                            taskStartAt = initialDateAt + initialTimeOffsetMillis,
                            onRuleSelected = onRecurrenceSelected,
                            onCancel = onDismiss,
                        )
                    }
                }
            }

            SectionButtons(
                selectedSection = selectedSection.value,
                sectionSummaries = sectionSummaries,
                onSectionSelected = { section -> selectedSection.value = section },
            )

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDone) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun SectionButtons(
    selectedSection: DateTimeSection,
    sectionSummaries: Map<DateTimeSection, String>,
    onSectionSelected: (DateTimeSection) -> Unit,
) {
    val sections = remember {
        listOf(
            DateTimeSection.TIME,
            DateTimeSection.DATE,
            DateTimeSection.DURATION,
            DateTimeSection.REMINDER,
            DateTimeSection.REPEAT,
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        sections.forEachIndexed { index, section ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSectionSelected(section) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = section.title,
                    style = if (section == selectedSection) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = if (section == selectedSection) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )

                Text(
                    text = sectionSummaries[section].orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (index != sections.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

private fun formatTimeOffset(offsetMillis: Long): String {
    val totalMinutes = (offsetMillis / 60_000L).toInt()
    val hour = totalMinutes / 60
    val minute = totalMinutes % 60
    return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}

private fun formatLocalDate(dateMillis: Long): String {
    return DateFormat.getDateInstance(DateFormat.SHORT).format(Date(dateMillis))
}

private fun formatDuration(durationMillis: Long?): String {
    if (durationMillis == null || durationMillis <= 0L) return "Not set"
    val totalMinutes = (durationMillis / 60_000L).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

private fun formatRepeat(rule: RecurrenceRule?): String {
    if (rule == null) return "None"
    val typeLabel = when (rule.type) {
        RecurrenceType.HOURLY -> "Hourly"
        RecurrenceType.DAILY -> "Daily"
        RecurrenceType.WEEKLY -> "Weekly"
        RecurrenceType.MONTHLY -> "Monthly"
        RecurrenceType.YEARLY -> "Yearly"
    }
    return if (rule.interval == 1) typeLabel else "$typeLabel x${rule.interval}"
}

@Composable
private fun ReminderPickerPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = "Reminder picker is pending. This section is reserved for reminder presets.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(apiLevel = 34, showBackground = true)
@Composable
fun PreviewTimePickerScreen() {
    MaterialTheme {
        DateTimeDialog()
    }
}