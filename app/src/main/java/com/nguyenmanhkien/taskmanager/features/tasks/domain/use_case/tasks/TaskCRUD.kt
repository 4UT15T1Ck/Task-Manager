package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import org.koin.core.annotation.Single

@Single
data class TaskCRUD(
    val bulkDeleteTasks: BulkDeleteTasks,
    val deleteTask: DeleteTask,
    val getTaskDetail: GetTaskDetail,
    val getFilteredTasks: GetFilteredTasks,
    val getFilteredTasksWithSubtasks: GetFilteredTasksWithSubtasks,
    val getParentTaskCountsByCategory: GetParentTaskCountsByCategory,
    val setCategory: SetTaskCategory,
    val setStatus: SetTaskStatus,
    val setTaskAndSubtasksStatus: SetTaskAndSubtasksStatus,
    val upsertTask: UpsertTask,
    val setPriority: SetPriority,
    val toggleTaskCompletion: ToggleTaskCompletion,
    val duplicateTaskWithSubtasks: DuplicateTaskWithSubtasks
)
