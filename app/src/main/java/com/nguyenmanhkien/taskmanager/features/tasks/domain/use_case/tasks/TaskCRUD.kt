package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import org.koin.core.annotation.Single

@Single
data class TaskCRUD(
    val deleteTask: DeleteTask,
    val getTaskDetail: GetTaskDetail,
    val getFilteredTasks: GetFilteredTasks,
    val upsertTask: UpsertTask,
    val setPriority: SetPriority,
    val toggleTaskCompletion: ToggleTaskCompletion
)