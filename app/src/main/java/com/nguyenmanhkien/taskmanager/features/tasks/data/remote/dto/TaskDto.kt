package com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class GoogleTaskResponseDto(
    val id: String,
    val title: String?,
    val status: String,
    val due: String?,
    val notes: String?,
    val updated: String?,
    val parent: String? = null
)

@Serializable
data class GoogleTaskRequestDto(
    val title: String?,
    val status: String,
    val due: String?,
    val notes: String?,
    val parent: String? = null
)

fun Task.toGoogleTaskRequestDto(parentGoogleTaskId: String? = null): GoogleTaskRequestDto {

    val dueString = this.dueAt?.let {
        Instant.ofEpochMilli(it).toString()
    }

    return GoogleTaskRequestDto(
        title = this.title,
        status = if (this.status == TaskStatus.COMPLETED) "completed" else "needsAction",
        due = dueString,
        notes = this.description,
        parent = parentGoogleTaskId
    )
}