package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.tasks

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.InvalidTaskException
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.SyncStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import org.koin.core.annotation.Factory
import kotlin.jvm.Throws

@Factory
class UpsertTask(private val repository: TaskRepository) {
    @Throws(InvalidTaskException::class)
    suspend operator fun invoke(task: Task): Long {
        if (task.title.isEmpty())
            throw InvalidTaskException("The title of the task can't be empty.")
        val taskToSave = task.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = if (task.id == 0) SyncStatus.CREATED else SyncStatus.UPDATED
        )
        return repository.upsertTask(taskToSave)
    }
}