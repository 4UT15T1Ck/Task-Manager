package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_calendar

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.components.TaskItemCard
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import org.koin.androidx.compose.koinViewModel
import java.time.ZoneId

private val CalendarBorderColor = Color(0xFF6B93F2)
private val CalendarBackground = Color(0xFFF8F9FA)
private val TodayBackground = Color(0xFFE3F2FD)
private val TodayText = Color(0xFF1976D2)
private val FallbackCategoryColor = Color(0xFF6B93F2)
private const val TOTAL_PAGER_PAGES = 2400
private const val INITIAL_PAGER_PAGE = TOTAL_PAGER_PAGES / 2

@Composable
fun TaskCalendarScreen(
    onTaskClick: (Int) -> Unit = {},
    onDateClick: (Long?) -> Unit = {},
    viewModel: TaskCalendarViewModel = koinViewModel()
) {
    val state by viewModel.state
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val pagerState = rememberPagerState(initialPage = INITIAL_PAGER_PAGE) { TOTAL_PAGER_PAGES }
    val accumulatedDrag = remember { floatArrayOf(0f) }
    val isCollapsed = state.isCalendarCollapsed
    val DRAG_THRESHOLD = 80f

    val nestedScrollConnection = remember(listState, isCollapsed) {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero

                val isAtTop = listState.firstVisibleItemIndex == 0 &&
                        listState.firstVisibleItemScrollOffset == 0

                val canCollapse = available.y < 0f && !isCollapsed
                val canExpand = available.y > 0f && isAtTop && isCollapsed

                return when {
                    canCollapse -> {
                        accumulatedDrag[0] += available.y
                        if (accumulatedDrag[0] < -DRAG_THRESHOLD) {
                            viewModel.onEvent(TaskCalendarEvent.SetCalendarCollapsed(true))
                            accumulatedDrag[0] = 0f
                        }
                        available
                    }

                    canExpand -> {
                        accumulatedDrag[0] += available.y
                        if (accumulatedDrag[0] > DRAG_THRESHOLD) {
                            viewModel.onEvent(TaskCalendarEvent.SetCalendarCollapsed(false))
                            accumulatedDrag[0] = 0f
                        }
                        available
                    }

                    else -> Offset.Zero
                }
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onEvent(
            TaskCalendarEvent.ChangeVisibleMonth(pageToMonth(pagerState.currentPage))
        )
    }

    LaunchedEffect(state.visibleMonth) {
        val targetPage = monthToPage(state.visibleMonth)
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    val categoryColors = remember(state.categories) {
        state.categories.associate { it.id to Color(it.color) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalendarBackground)
            .nestedScroll(nestedScrollConnection)
    ) {
        Box(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessLow,
                        dampingRatio = Spring.DampingRatioLowBouncy
                    )
                )
                .border(1.dp, CalendarBorderColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .background(Color.White, RoundedCornerShape(24.dp))
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { accumulatedDrag[0] = 0f },
                        onVerticalDrag = { _, dragAmount -> accumulatedDrag[0] += dragAmount },
                        onDragEnd = {
                            when {
                                accumulatedDrag[0] < -DRAG_THRESHOLD && !isCollapsed ->
                                    viewModel.onEvent(TaskCalendarEvent.SetCalendarCollapsed(true))

                                accumulatedDrag[0] > DRAG_THRESHOLD && isCollapsed ->
                                    viewModel.onEvent(TaskCalendarEvent.SetCalendarCollapsed(false))
                            }
                            accumulatedDrag[0] = 0f
                        },
                        onDragCancel = { accumulatedDrag[0] = 0f }
                    )
                }
        ) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                CalendarHeader(
                    yearMonth = state.visibleMonth,
                    onPreviousMonth = {
                        viewModel.onEvent(
                            TaskCalendarEvent.ChangeVisibleMonth(
                                state.visibleMonth.minusMonths(
                                    1
                                )
                            )
                        )
                    },
                    onNextMonth = {
                        viewModel.onEvent(
                            TaskCalendarEvent.ChangeVisibleMonth(
                                state.visibleMonth.plusMonths(
                                    1
                                )
                            )
                        )
                    }
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    val month = pageToMonth(page)
                    MonthGrid(
                        month = month,
                        selectedDate = state.selectedDate,
                        daysWithTasks = state.daysWithTasks,
                        isCollapsed = state.isCalendarCollapsed,
                        onDateTap = { tappedDate ->
                            val targetMonth = YearMonth.from(tappedDate)
                            if (targetMonth != month) {
                                val pageDelta = monthDistance(month, targetMonth)
                                viewModel.onEvent(TaskCalendarEvent.SelectSummaryDate(tappedDate))
                                viewModel.onEvent(TaskCalendarEvent.ChangeVisibleMonth(targetMonth))
                                scope.launch {
                                    pagerState.animateScrollToPage(page + pageDelta)
                                }
                            } else {
                                viewModel.onEvent(TaskCalendarEvent.ToggleDateSelection(tappedDate))
                            }
                            onDateClick(tappedDate.atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli())
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.selectedDate != null) {
                if (state.selectedDayTaskItems.isEmpty()) {
                    item {
                        EmptyTasksPlaceholder(selectedDate = state.selectedDate!!)
                    }
                } else {
                    items(state.selectedDayTaskItems, key = { it.task.id }) { taskItem ->
                        TaskItemCard(
                            modifier = Modifier
                                .padding(start = 3.dp)
                                .drawBehind {
                                    val color = categoryColors[taskItem.task.categoryId]
                                        ?: FallbackCategoryColor
                                    val radius = 15.dp.toPx()
                                    val strokeWidth = 6.dp.toPx()
                                    drawLine(
                                        color = color,
                                        start = Offset(x = 0f, y = radius),
                                        end = Offset(x = 0f, y = size.height - radius),
                                        strokeWidth = strokeWidth
                                    )
                                    drawArc(
                                        color = color,
                                        startAngle = 180f,
                                        sweepAngle = -60f,
                                        useCenter = false,
                                        topLeft = Offset(x = 0f, y = size.height - radius * 2 - strokeWidth / 3),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth)
                                    )
                                    drawArc(
                                        color = color,
                                        startAngle = 180f,
                                        sweepAngle = 60f,
                                        useCenter = false,
                                        topLeft = Offset(x = 0f, y = strokeWidth / 3),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth)
                                    )
                                },
                            taskItem = taskItem,
                            isSelectionMode = false,
                            isSelected = false,
                            isAnimatingCompletion = taskItem.task.id in state.animatingTaskIds,
                            isShowingSubtasks = false,
                            isPriorityMenuExpanded = state.activePriorityTaskId == taskItem.task.id,
                            onClick = { onTaskClick(taskItem.task.id) },
                            onCompletionClick = {
                                viewModel.onEvent(
                                    TaskCalendarEvent.ToggleTaskCompletion(
                                        taskItem.task
                                    )
                                )
                            },
                            onPriorityClick = {
                                viewModel.onEvent(TaskCalendarEvent.OpenPriorityMenu(taskItem.task.id))
                            },
                            onPriorityDismiss = {
                                viewModel.onEvent(TaskCalendarEvent.DismissPriorityMenu)
                            },
                            onPrioritySelected = { priority ->
                                viewModel.onEvent(
                                    TaskCalendarEvent.SetTaskPriority(
                                        taskItem.task.id,
                                        priority
                                    )
                                )
                            },
                            onSubtaskCompletionClick = {}
                        )
                    }
                }
            } else {
                if (state.monthSummaries.isEmpty()) {
                    item {
                        EmptyMonthSummaryPlaceholder(month = state.visibleMonth)
                    }
                } else {
                    items(state.monthSummaries, key = { it.date.toEpochDay() }) { summary ->
                        DaySummaryCard(
                            summary = summary,
                            onClick = {
                                viewModel.onEvent(TaskCalendarEvent.SelectSummaryDate(summary.date))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
        }

        Text(
            text = "${
                yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()
            } ${yearMonth.year}",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate?,
    daysWithTasks: Set<LocalDate>,
    isCollapsed: Boolean,
    onDateTap: (LocalDate) -> Unit
) {
    val days = remember(month) { buildMonthCells(month) }
    val weekCount = remember(days) { days.size / 7 }
    val selectedWeekIndex = remember(month, selectedDate) {
        val weekAnchorDate = selectedDate
            ?.takeIf { YearMonth.from(it) == month }
            ?: month.atDay(1)
        (days.indexOf(weekAnchorDate) / 7).coerceAtLeast(0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF98A2B3)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val weekRange = if (isCollapsed) {
            selectedWeekIndex..selectedWeekIndex
        } else {
            0 until weekCount
        }

        weekRange.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (weekDay in 0..6) {
                    val date = days[(week * 7) + weekDay]
                    CalendarDay(
                        date = date,
                        inCurrentMonth = YearMonth.from(date) == month,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        hasTask = date in daysWithTasks,
                        onClick = { onDateTap(date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    inCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    hasTask: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = when {
                    isSelected -> Color(0xFF6B93F2)
                    isToday -> TodayBackground
                    else -> Color.Transparent
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = when {
                    isSelected -> Color.White
                    isToday -> TodayText
                    !inCurrentMonth -> Color(0xFFB5B8C5)
                    else -> Color(0xFF101828)
                },
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (hasTask) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(if (isSelected) Color.White else Color(0xFF6B93F2), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun DaySummaryCard(
    summary: CalendarDaySummary,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = summary.date.format(
                    DateTimeFormatter.ofPattern(
                        "EEE, dd MMM",
                        Locale.getDefault()
                    )
                ),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101828)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${summary.totalCount} tasks",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475467)
            )
            Text(
                text = "${summary.completedCount} completed",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF667085)
            )
        }
    }
}

@Composable
private fun EmptyTasksPlaceholder(selectedDate: LocalDate) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No tasks for ${
                selectedDate.format(
                    DateTimeFormatter.ofPattern(
                        "EEE, dd MMM",
                        Locale.getDefault()
                    )
                )
            }",
            color = Color(0xFF667085),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EmptyMonthSummaryPlaceholder(month: YearMonth) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No task days in ${
                month.month.getDisplayName(
                    TextStyle.FULL,
                    Locale.getDefault()
                )
            }",
            color = Color(0xFF667085),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun pageToMonth(page: Int): YearMonth {
    return YearMonth.now().plusMonths((page - INITIAL_PAGER_PAGE).toLong())
}

private fun monthToPage(month: YearMonth): Int {
    return INITIAL_PAGER_PAGE + monthDistance(YearMonth.now(), month)
}

private fun monthDistance(from: YearMonth, to: YearMonth): Int {
    return ((to.year - from.year) * 12) + (to.monthValue - from.monthValue)
}

private fun buildMonthCells(month: YearMonth): List<LocalDate> {
    val firstDay = month.atDay(1)
    val firstCell = firstDay.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    val lastDay = month.atEndOfMonth()
    val lastCell = lastDay.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
    val visibleDays = java.time.temporal.ChronoUnit.DAYS.between(firstCell, lastCell).toInt() + 1
    return List(visibleDays) { firstCell.plusDays(it.toLong()) }
}

private fun monthWeekCount(month: YearMonth): Int {
    return buildMonthCells(month).size / 7
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun PreviewTaskCalendarScreen() {
    MaterialTheme {
        TaskCalendarScreen()
    }
}

