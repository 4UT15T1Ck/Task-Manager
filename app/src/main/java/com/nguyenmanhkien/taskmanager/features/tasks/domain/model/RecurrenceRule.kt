package com.nguyenmanhkien.taskmanager.features.tasks.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar

// ---------------------------------------------------------------------------
// Enums
// ---------------------------------------------------------------------------

@Serializable
enum class RecurrenceType {
    HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY
}

/**
 * Day-of-week constants aligned with [Calendar] field values so they can be
 * used directly when computing next-occurrence dates.
 */
@Serializable
enum class RecurrenceDay(val calendarValue: Int) {
    SUNDAY(Calendar.SUNDAY),
    MONDAY(Calendar.MONDAY),
    TUESDAY(Calendar.TUESDAY),
    WEDNESDAY(Calendar.WEDNESDAY),
    THURSDAY(Calendar.THURSDAY),
    FRIDAY(Calendar.FRIDAY),
    SATURDAY(Calendar.SATURDAY);

    companion object {
        fun fromCalendarValue(value: Int): RecurrenceDay =
            entries.first { it.calendarValue == value }
    }
}

/**
 * How a MONTHLY recurrence maps onto successive months.
 *
 * - [SAME_DATE]              – always on the same day number (e.g. the 15th).
 * - [SAME_WEEK_DAY_PATTERN]  – same week-of-month + day-of-week (e.g. 2nd Tuesday).
 * - [START_OF_MONTH]         – only available when the task starts on the 1st.
 * - [END_OF_MONTH]           – only available when the task starts on the last day of its month.
 */
@Serializable
enum class MonthlyPattern {
    SAME_DATE,
    SAME_WEEK_DAY_PATTERN,
    START_OF_MONTH,
    END_OF_MONTH
}

// ---------------------------------------------------------------------------
// End-condition sealed class
// ---------------------------------------------------------------------------

/**
 * Determines when the recurrence chain stops.
 *
 * - [Endless]      – repeats indefinitely.
 * - [OnDate]       – stops after the first occurrence whose date is after [timestamp].
 * - [RepeatCount]  – [count] remaining completions; decremented by 1 each time the
 *                    real task is completed and a successor task is created. When it
 *                    reaches 0 no new task is created and the chain ends.
 */
@Serializable
sealed class EndCondition {

    @Serializable
    @SerialName("endless")
    data object Endless : EndCondition()

    @Serializable
    @SerialName("on_date")
    data class OnDate(val timestamp: Long) : EndCondition()

    @Serializable
    @SerialName("repeat_count")
    data class RepeatCount(val count: Int) : EndCondition() {
        /** Returns a copy with [count] decremented, or null when the chain should end. */
        fun decremented(): RepeatCount? =
            if (count > 1) copy(count = count - 1) else null
    }
}

// ---------------------------------------------------------------------------
// RecurrenceRule
// ---------------------------------------------------------------------------

/**
 * Describes how a task repeats over time.
 *
 * Stored as a JSON string in [Task.rrule].  A `null` rrule means no recurrence.
 *
 * ### Field semantics
 * | Field             | Applies to     | Notes |
 * |-------------------|----------------|-------|
 * | [type]            | all            | Recurrence frequency unit |
 * | [interval]        | all            | Repeat every N units (≥ 1) |
 * | [endCondition]    | all            | When the chain stops |
 * | [daysOfWeek]      | WEEKLY only    | Which weekdays to recur on; defaults to the weekday of `startAt`; must contain ≥ 1 day |
 * | [monthlyPattern]  | MONTHLY only   | How the date maps to successive months |
 *
 * ### Start / end times
 * The time-of-day and duration of each occurrence are derived from the real
 * task's `startAt` and `dueAt` fields.  Virtual tasks shift both fields by
 * whole calendar units while preserving the time-of-day component.
 *
 * ### Completion flow
 * When the real task is marked complete:
 * 1. The real task row is updated: `rrule = null`, `status = COMPLETED`.
 * 2. A new real task row is inserted with the same details, this rule (with
 *    [EndCondition.RepeatCount] decremented), and `startAt`/`dueAt` advanced
 *    to the next occurrence.  If the decremented count reaches 0, or the next
 *    occurrence falls after an [EndCondition.OnDate] threshold, no new row is
 *    created and the chain ends.
 *
 * ### Virtual tasks
 * Any occurrence date other than the real task's own `startAt` date generates
 * a *virtual* task (in-memory only, `id = 0`).  Editing or deleting a virtual
 * task modifies/deletes the underlying real task row, affecting all future
 * occurrences.  Virtual tasks cannot be individually completed.
 */
@Serializable
data class RecurrenceRule(
    val type: RecurrenceType,
    val interval: Int = 1,
    val endCondition: EndCondition = EndCondition.Endless,
    /** Populated and enforced only when [type] == [RecurrenceType.WEEKLY]. */
    val daysOfWeek: Set<RecurrenceDay> = emptySet(),
    /** Populated and enforced only when [type] == [RecurrenceType.MONTHLY]. */
    val monthlyPattern: MonthlyPattern = MonthlyPattern.SAME_DATE
) {

    init {
        require(interval >= 1) { "interval must be ≥ 1, got $interval" }
        if (type == RecurrenceType.WEEKLY) {
            require(daysOfWeek.isNotEmpty()) { "daysOfWeek must contain at least 1 day for WEEKLY recurrence" }
        }
    }

    // ------------------------------------------------------------------
    // Serialization
    // ------------------------------------------------------------------

    /** Serialises this rule to a compact JSON string suitable for storing in [Task.rrule]. */
    fun toJson(): String = json.encodeToString(this)

    // ------------------------------------------------------------------
    // Next-occurrence helpers
    // ------------------------------------------------------------------

    /**
     * Returns the timestamp (ms) of the next occurrence after [afterTimestamp],
     * or `null` if [endCondition] prevents any further occurrences.
     *
     * Pass the real task's current `startAt` as [taskStartAt] and [afterTimestamp]
     * as the same value to get the immediate successor date.
     */
    fun nextOccurrenceAfter(taskStartAt: Long, afterTimestamp: Long): Long? {
        // Check end condition first (without knowing the exact next date for RepeatCount,
        // the caller is responsible for checking count after decrement).
        if (endCondition is EndCondition.RepeatCount && endCondition.count <= 0) return null

        val cal = Calendar.getInstance().apply { timeInMillis = afterTimestamp }

        return when (type) {
            RecurrenceType.HOURLY -> {
                cal.add(Calendar.HOUR_OF_DAY, interval)
                checkEndCondition(cal.timeInMillis)
            }

            RecurrenceType.DAILY -> {
                cal.add(Calendar.DAY_OF_YEAR, interval)
                checkEndCondition(cal.timeInMillis)
            }

            RecurrenceType.WEEKLY -> {
                nextWeeklyOccurrence(cal, afterTimestamp)
            }

            RecurrenceType.MONTHLY -> {
                nextMonthlyOccurrence(cal, taskStartAt)
            }

            RecurrenceType.YEARLY -> {
                cal.add(Calendar.YEAR, interval)
                checkEndCondition(cal.timeInMillis)
            }
        }
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private fun checkEndCondition(nextTs: Long): Long? = when (endCondition) {
        is EndCondition.Endless -> nextTs
        is EndCondition.OnDate -> if (nextTs <= endCondition.timestamp) nextTs else null
        is EndCondition.RepeatCount -> if (endCondition.count > 0) nextTs else null
    }

    private fun nextWeeklyOccurrence(fromCal: Calendar, afterTimestamp: Long): Long? {
        // Collect sorted calendar values of the selected days
        val sortedDays = daysOfWeek.map { it.calendarValue }.sorted()

        // Try to find the next matching weekday within the current or future weeks
        val searchCal = Calendar.getInstance().apply { timeInMillis = afterTimestamp }
        searchCal.add(Calendar.DAY_OF_YEAR, 1) // start searching from the day after

        repeat(interval * 7 + 7) { // search up to interval weeks + 1 buffer
            val dow = searchCal.get(Calendar.DAY_OF_WEEK)
            if (dow in sortedDays) {
                // Verify that enough weeks have passed (interval-based)
                val weeksDiff = weeksBetween(fromCal.timeInMillis, searchCal.timeInMillis)
                if (weeksDiff >= interval) {
                    return checkEndCondition(searchCal.timeInMillis)
                }
            }
            searchCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return null
    }

    private fun weeksBetween(startMs: Long, endMs: Long): Int {
        val diffMs = endMs - startMs
        return (diffMs / (7L * 24 * 60 * 60 * 1000)).toInt()
    }

    private fun nextMonthlyOccurrence(fromCal: Calendar, taskStartAt: Long): Long? {
        val originCal = Calendar.getInstance().apply { timeInMillis = taskStartAt }
        val resultCal = Calendar.getInstance().apply { timeInMillis = fromCal.timeInMillis }
        resultCal.add(Calendar.MONTH, interval)

        when (monthlyPattern) {
            MonthlyPattern.SAME_DATE -> {
                val targetDay = originCal.get(Calendar.DAY_OF_MONTH)
                val maxDay = resultCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                resultCal.set(Calendar.DAY_OF_MONTH, minOf(targetDay, maxDay))
            }

            MonthlyPattern.SAME_WEEK_DAY_PATTERN -> {
                val targetWeekInMonth = originCal.get(Calendar.WEEK_OF_MONTH)
                val targetDow = originCal.get(Calendar.DAY_OF_WEEK)
                resultCal.set(Calendar.DAY_OF_MONTH, 1)
                resultCal.set(Calendar.WEEK_OF_MONTH, targetWeekInMonth)
                resultCal.set(Calendar.DAY_OF_WEEK, targetDow)
                // Clamp to the result month in case the pattern overshoots
                if (resultCal.get(Calendar.MONTH) != (fromCal.get(Calendar.MONTH) + interval).let {
                        val tmp =
                            Calendar.getInstance().apply { timeInMillis = fromCal.timeInMillis }
                        tmp.add(Calendar.MONTH, interval)
                        tmp.get(Calendar.MONTH)
                    }) {
                    // Fall back to last matching weekday in that month
                    resultCal.add(Calendar.WEEK_OF_MONTH, -1)
                }
            }

            MonthlyPattern.START_OF_MONTH -> {
                resultCal.set(Calendar.DAY_OF_MONTH, 1)
            }

            MonthlyPattern.END_OF_MONTH -> {
                resultCal.set(
                    Calendar.DAY_OF_MONTH,
                    resultCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
            }
        }

        return checkEndCondition(resultCal.timeInMillis)
    }

    // ------------------------------------------------------------------
    // Companion
    // ------------------------------------------------------------------

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        /**
         * Deserialises a [RecurrenceRule] from the JSON string stored in [Task.rrule].
         * Returns `null` if [jsonString] is null or blank.
         */
        fun fromJson(jsonString: String?): RecurrenceRule? {
            if (jsonString.isNullOrBlank()) return null
            return runCatching { json.decodeFromString<RecurrenceRule>(jsonString) }.getOrNull()
        }

        /**
         * Convenience factory: builds the default rule for the given [type] and [taskStartAt].
         *
         * - WEEKLY → [daysOfWeek] defaults to the weekday of [taskStartAt].
         * - MONTHLY → [monthlyPattern] defaults based on the day-of-month of [taskStartAt].
         */
        fun default(type: RecurrenceType, taskStartAt: Long): RecurrenceRule {
            val cal = Calendar.getInstance().apply { timeInMillis = taskStartAt }
            return when (type) {
                RecurrenceType.WEEKLY -> RecurrenceRule(
                    type = type,
                    daysOfWeek = setOf(RecurrenceDay.fromCalendarValue(cal.get(Calendar.DAY_OF_WEEK)))
                )

                RecurrenceType.MONTHLY -> {
                    val dom = cal.get(Calendar.DAY_OF_MONTH)
                    val maxDom = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val pattern = when {
                        dom == 1 -> MonthlyPattern.START_OF_MONTH
                        dom == maxDom -> MonthlyPattern.END_OF_MONTH
                        else -> MonthlyPattern.SAME_DATE
                    }
                    RecurrenceRule(type = type, monthlyPattern = pattern)
                }

                else -> RecurrenceRule(type = type)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Extension on Task
// ---------------------------------------------------------------------------

/**
 * Parses [Task.rrule] into a [RecurrenceRule], or returns `null` if this task
 * has no recurrence rule or the stored JSON is malformed.
 */
fun Task.recurrenceRule(): RecurrenceRule? = RecurrenceRule.fromJson(rrule)

/**
 * Returns `true` if this task is an active recurring task (has a valid rrule).
 */
val Task.isRecurring: Boolean get() = recurrenceRule() != null

/**
 * Produces a virtual copy of this recurring task for [occurrenceDateMillis].
 *
 * The time-of-day component of [startAt] and [dueAt] is preserved; only the
 * date portion is replaced.  The returned task has [Task.id] == 0 and
 * [Task.rrule] == null so it is clearly distinguishable from the real row.
 */
fun Task.asVirtualOccurrence(occurrenceDateMillis: Long): Task {
    fun shiftToDate(original: Long?, targetDateMs: Long): Long? {
        original ?: return null
        val origCal = Calendar.getInstance().apply { timeInMillis = original }
        val targetCal = Calendar.getInstance().apply { timeInMillis = targetDateMs }
        return Calendar.getInstance().apply {
            set(
                targetCal.get(Calendar.YEAR),
                targetCal.get(Calendar.MONTH),
                targetCal.get(Calendar.DAY_OF_MONTH),
                origCal.get(Calendar.HOUR_OF_DAY),
                origCal.get(Calendar.MINUTE),
                origCal.get(Calendar.SECOND)
            )
            set(Calendar.MILLISECOND, origCal.get(Calendar.MILLISECOND))
        }.timeInMillis
    }

    return copy(
        id = 0,
        startAt = shiftToDate(startAt, occurrenceDateMillis),
        dueAt = shiftToDate(dueAt, occurrenceDateMillis),
        rrule = null,          // virtual — not a DB entity
        syncStatus = SyncStatus.CREATED
    )
}

