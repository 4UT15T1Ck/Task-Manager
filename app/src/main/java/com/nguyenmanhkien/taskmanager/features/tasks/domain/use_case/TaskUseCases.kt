package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case

import com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks.TaskCRUD
import com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.calendar.CalendarUseCases
import com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.categories.CategoryUseCases

import org.koin.core.annotation.Single

@Single
data class TaskUseCases(
    val taskCRUD: TaskCRUD,
    val calendar: CalendarUseCases,
    val category: CategoryUseCases
)