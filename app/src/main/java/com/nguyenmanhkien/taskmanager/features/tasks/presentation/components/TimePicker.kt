package com.nguyenmanhkien.taskmanager.features.tasks.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Calendar
import kotlin.math.roundToInt

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60
private const val HOURS_PER_DAY = 24
private const val WHEEL_LOOP_MULTIPLIER = 200
private val WHEEL_ITEM_HEIGHT = 50.dp

@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    initialTimeOffsetMillis: Long = currentTimeOffsetMillis(),
    onTimeSelected: (timeOffsetMillis: Long) -> Unit,
) {
    val normalizedOffset = initialTimeOffsetMillis.coerceIn(0L, (HOURS_PER_DAY * MINUTES_PER_HOUR * MILLIS_PER_MINUTE) - MILLIS_PER_MINUTE)
    var selectedHour by remember(normalizedOffset) {
        mutableIntStateOf((normalizedOffset / (MINUTES_PER_HOUR * MILLIS_PER_MINUTE)).toInt())
    }
    var selectedMinute by remember(normalizedOffset) {
        mutableIntStateOf(((normalizedOffset / MILLIS_PER_MINUTE) % MINUTES_PER_HOUR).toInt())
    }
    val chipTimes = remember {
        mapOf(
            0 to (7 to 0),
            1 to (9 to 0),
            2 to (10 to 0),
            3 to (12 to 0),
            4 to (14 to 0),
            5 to (16 to 0),
            6 to (18 to 0),
        )
    }
    var selectedTimeChip by remember { mutableStateOf(chipIdForTime(selectedHour, selectedMinute, chipTimes)) }

    LaunchedEffect(selectedHour, selectedMinute) {
        onTimeSelected(toTimeOffsetMillis(selectedHour, selectedMinute))
        selectedTimeChip = chipIdForTime(selectedHour, selectedMinute, chipTimes)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TimePickerWithChips(
            selectedHour = selectedHour,
            selectedMinute = selectedMinute,
            selectedTimeChip = selectedTimeChip,
            onHourChanged = {
                selectedHour = it
                selectedTimeChip = null
            },
            onMinuteChanged = {
                selectedMinute = it
                selectedTimeChip = null
            },
            onChipSelected = { chipId, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                selectedTimeChip = chipId
            }
        )
    }
}

private fun currentTimeOffsetMillis(): Long {
    val calendar = Calendar.getInstance()
    return toTimeOffsetMillis(
        hour = calendar.get(Calendar.HOUR_OF_DAY),
        minute = calendar.get(Calendar.MINUTE),
    )
}

private fun toTimeOffsetMillis(hour: Int, minute: Int): Long {
    return (hour * MINUTES_PER_HOUR + minute) * MILLIS_PER_MINUTE
}

private fun chipIdForTime(
    hour: Int,
    minute: Int,
    chipTimes: Map<Int, Pair<Int, Int>>,
): Int? {
    return chipTimes.entries.firstOrNull { (_, time) ->
        time.first == hour && time.second == minute
    }?.key
}

@Composable
private fun TimePickerWithChips(
    selectedHour: Int,
    selectedMinute: Int,
    selectedTimeChip: Int?,
    onHourChanged: (Int) -> Unit,
    onMinuteChanged: (Int) -> Unit,
    onChipSelected: (chipId: Int, hour: Int, minute: Int) -> Unit,
) {
    val timeChips = remember {
        listOf(
            -1 to ("Now" to null),
            0 to ("07:00" to (7 to 0)),
            1 to ("09:00" to (9 to 0)),
            2 to ("10:00" to (10 to 0)),
            3 to ("12:00" to (12 to 0)),
            4 to ("14:00" to (14 to 0)),
            5 to ("16:00" to (16 to 0)),
            6 to ("18:00" to (18 to 0))
        )
    }

    val leftChips = timeChips.take(4)
    val rightChips = timeChips.drop(4)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(160.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            leftChips.forEach { (id, chip) ->
                TimeChip(
                    id = id,
                    label = chip.first,
                    selected = selectedTimeChip == id,
                    onClick = {
                        val selection = chip.second ?: run {
                            val now = Calendar.getInstance()
                            now.get(Calendar.HOUR_OF_DAY) to now.get(Calendar.MINUTE)
                        }
                        onChipSelected(id, selection.first, selection.second)
                    }
                )
            }
        }

        ScrollableTimePicker(
            selectedHour = selectedHour,
            selectedMinute = selectedMinute,
            onHourChanged = onHourChanged,
            onMinuteChanged = onMinuteChanged,
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            rightChips.forEach { (id, chip) ->
                TimeChip(
                    id = id,
                    label = chip.first,
                    selected = selectedTimeChip == id,
                    onClick = {
                        val selection = chip.second ?: (0 to 0)
                        onChipSelected(id, selection.first, selection.second)
                    }
                )
            }
        }
    }
}

@Composable
private fun TimeChip(
    id: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                modifier = Modifier.width(50.dp),
                textAlign = TextAlign.Center,
            )
        },
        modifier = Modifier.size(width = 70.dp, height = 30.dp),
    )
}

@Composable
private fun ScrollableTimePicker(
    selectedHour: Int,
    selectedMinute: Int,
    onHourChanged: (Int) -> Unit,
    onMinuteChanged: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.height(150.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WheelPicker(
            items = (0..23).toList(),
            selectedItem = selectedHour,
            onItemSelected = onHourChanged,
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        WheelPicker(
            items = (0..59).toList(),
            selectedItem = selectedMinute,
            onItemSelected = onMinuteChanged,
        )
    }
}

@Composable
private fun WheelPicker(
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
) {
    val cycleSize = items.size
    val virtualSize = cycleSize * WHEEL_LOOP_MULTIPLIER
    val middleCycle = (WHEEL_LOOP_MULTIPLIER / 2) * cycleSize
    val centerSlotOffset = 1
    val initialIndex = middleCycle + (selectedItem - centerSlotOffset).mod(cycleSize)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val itemHeightPx = with(LocalDensity.current) { WHEEL_ITEM_HEIGHT.toPx().roundToInt() }
    var lastEmittedValue by remember { mutableIntStateOf(selectedItem) }
    val currentOnItemSelected by rememberUpdatedState(onItemSelected)

    fun snappedVirtualIndex(): Int {
        val base = listState.firstVisibleItemIndex
        val offset = listState.firstVisibleItemScrollOffset
        val step = if (offset >= itemHeightPx / 2) 1 else 0
        return (base + centerSlotOffset + step).coerceIn(0, virtualSize - 1)
    }

    fun emitIfChanged(value: Int) {
        if (value != lastEmittedValue) {
            lastEmittedValue = value
            currentOnItemSelected(value)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { items[snappedVirtualIndex().mod(cycleSize)] }
            .distinctUntilChanged()
            .collect { value -> emitIfChanged(value) }
    }

    LaunchedEffect(listState, itemHeightPx) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                if (!isScrolling) {
                    val targetCenterIndex = snappedVirtualIndex()
                    val targetFirstVisibleIndex = (targetCenterIndex - centerSlotOffset).coerceIn(0, virtualSize - 1)
                    if (targetFirstVisibleIndex != listState.firstVisibleItemIndex || listState.firstVisibleItemScrollOffset != 0) {
                        listState.scrollToItem(targetFirstVisibleIndex)
                    }
                    val settledValue = items[snappedVirtualIndex().mod(cycleSize)]
                    emitIfChanged(settledValue)

                }
            }
    }

    LaunchedEffect(selectedItem) {
        if (selectedItem != lastEmittedValue) {
            lastEmittedValue = selectedItem
        }
        if (items[snappedVirtualIndex().mod(cycleSize)] == selectedItem) return@LaunchedEffect
        val currentIndex = listState.firstVisibleItemIndex
        val targetIndex = nearestVirtualIndex(
            currentIndex = currentIndex,
            selectedItem = (selectedItem - centerSlotOffset).mod(cycleSize),
            cycleSize = cycleSize,
            virtualSize = virtualSize,
        )
        if (targetIndex != currentIndex) {
            listState.scrollToItem(targetIndex)
        }
    }

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(150.dp),
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.height(150.dp),
            userScrollEnabled = true,
        ) {
            items(virtualSize) { index ->
                val item = items[index.mod(cycleSize)]
                val selectedVirtualIndex = snappedVirtualIndex()

                val isSelected = index == selectedVirtualIndex
                val isPrevious = index == selectedVirtualIndex - 1
                val isNext = index == selectedVirtualIndex + 1

                Text(
                    text = String.format("%02d", item),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WHEEL_ITEM_HEIGHT)
                        .alpha(
                            when {
                                isSelected -> 1f
                                isPrevious || isNext -> 0.3f
                                else -> 0f
                            }
                        ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

private fun nearestVirtualIndex(
    currentIndex: Int,
    selectedItem: Int,
    cycleSize: Int,
    virtualSize: Int,
): Int {
    val currentCycleStart = (currentIndex / cycleSize) * cycleSize
    val candidates = listOf(
        currentCycleStart + selectedItem,
        currentCycleStart + selectedItem + cycleSize,
        currentCycleStart + selectedItem - cycleSize,
    ).map { it.coerceIn(0, virtualSize - 1) }

    return candidates.minBy { kotlin.math.abs(it - currentIndex) }
}

@Preview(apiLevel = 34, showBackground = true)
@Composable
fun PreviewTimePickerComponent() {
    MaterialTheme {
        TimePicker(onTimeSelected = { })
    }
}
