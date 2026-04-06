package com.nguyenmanhkien.taskmanager.features.tasks.domain.model

data class TaskWithSubtasks(
    val task: Task,
    val subtasks: List<Task>,
    val hasReminder: Boolean = false
) {
    val isFullyCompleted: Boolean
        get() = subtasks.isNotEmpty() && subtasks.all { it.status == TaskStatus.COMPLETED }

    val progress: Float
        get() = if (subtasks.isEmpty()) 0f else subtasks.count { it.status == TaskStatus.COMPLETED }.toFloat() / subtasks.size

    val subtaskCount: Int
        get() = subtasks.size

    val completedSubtaskCount: Int
        get() = subtasks.count { it.status == TaskStatus.COMPLETED }

    val hasSubtasks: Boolean
        get() = subtasks.isNotEmpty()

    val isRecurring: Boolean
        get() = !task.rrule.isNullOrBlank()
}
