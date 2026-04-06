package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import org.koin.core.annotation.Factory

@Factory
class DuplicateTaskWithSubtasks(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: Int): Int? {
        val sourceTask = repository.getTaskById(taskId) ?: return null
        val sourceSubtasks = repository.getSubtasksOnce(taskId)

        val now = System.currentTimeMillis()
        val duration = sourceTask.startAt?.let { startAt ->
            sourceTask.dueAt?.let { dueAt -> (dueAt - startAt).coerceAtLeast(0L) }
        }

        val duplicatedParent = sourceTask.copy(
            id = 0,
            title = sourceTask.title + " - duplicate",
            parentId = null,
            status = TaskStatus.IN_PROGRESS,
            startAt = now,
            dueAt = duration?.let { now + it },
            completionDate = null,
            createdAt = now,
            updatedAt = now,
            googleTaskId = null
        )

        val duplicatedParentId = repository.upsertTask(duplicatedParent).toInt()

        val duplicatedSubtasks = sourceSubtasks.map { subtask ->
            subtask.copy(
                id = 0,
                parentId = duplicatedParentId,
                status = TaskStatus.IN_PROGRESS,
                completionDate = null,
                createdAt = now,
                updatedAt = now,
                googleTaskId = null
            )
        }

        if (duplicatedSubtasks.isNotEmpty()) {
            repository.upsertTasks(duplicatedSubtasks)
        }

        return duplicatedParentId
    }
}


