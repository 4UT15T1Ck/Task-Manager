package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.calendar

import org.koin.core.annotation.Single

@Single
data class CalendarUseCases(
    val getTasksInDay: GetTasksInDay,
    val getDaysWithTask: GetDaysWithTask
)