package com.app.taskmanager.core.domain.models

data class TaskPreferences(
    val timeFormat: TimeFormat,
    val dateFormat: DateFormat,
    val reminderTimeBeforeStart: ReminderTime,
    val reminderTimeBeforeDue: ReminderTime,
    val defaultTaskDurationMinutes: Int
)

enum class TimeFormat(val pattern: String, val displayName: String) {
    H_12("hh:mm a","12-hour"),
    H_24("HH:mm","24-hour");

    companion object {
        fun fromString(value: String?): TimeFormat {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: H_12
        }
    }
}

enum class DateFormat(val pattern: String, val displayName: String) {
    MM_DD_YYYY("MM/dd/yyyy", "MM/DD/YYYY"),
    DD_MM_YYYY("dd/MM/yyyy", "DD/MM/YYYY"),
    YYYY_MM_DD("yyyy/MM/dd", "YYYY/MM/DD");
    companion object {
        fun fromString(value: String?): DateFormat {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: MM_DD_YYYY
        }
    }
}

enum class ReminderTime(val durationMinutes: Int, val displayName: String) {
    NONE(0, "At the same time"),
    MINUTES_5(5, "5 minutes before"),
    MINUTES_15(15, "15 minutes before"),
    MINUTES_30(30, "30 minutes before"),
    HOUR_1(60, "1 hour before"),
    HOUR_2(120, "2 hours before"),
    DAY_1(1440, "1 day before");

    companion object {
        fun fromMinutes(minutes: Int?): ReminderTime {
            return entries.find { it.durationMinutes == minutes } ?: MINUTES_5
        }
    }
}