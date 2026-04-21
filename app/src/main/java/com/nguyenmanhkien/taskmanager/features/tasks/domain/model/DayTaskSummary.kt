package com.nguyenmanhkien.taskmanager.features.tasks.domain.model

data class DayTaskSummary(
    val dayStart: Long,
    val totalCount: Int,
    val completedCount: Int
)

