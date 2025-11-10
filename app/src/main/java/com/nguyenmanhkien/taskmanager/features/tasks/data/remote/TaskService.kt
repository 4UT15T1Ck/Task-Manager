package com.nguyenmanhkien.taskmanager.features.tasks.data.remote

import com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto.GoogleTaskListCreateRequestDto
import com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto.GoogleTaskListResponseDto
import com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto.GoogleTaskListWrapperDto
import com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto.GoogleTaskRequestDto
import com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto.GoogleTaskResponseDto
import com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto.GoogleTaskWrapperDto

interface TaskService {
    suspend fun getTaskLists(): GoogleTaskListWrapperDto
    suspend fun createTaskList(request: GoogleTaskListCreateRequestDto): GoogleTaskListResponseDto?

    suspend fun getTasksInList(taskListId: String): GoogleTaskWrapperDto
    suspend fun createTaskInList(
        taskListId: String,
        request: GoogleTaskRequestDto
    ): GoogleTaskResponseDto?

    suspend fun updateTaskInList(
        taskListId: String,
        taskId: String,
        request: GoogleTaskRequestDto
    ): GoogleTaskResponseDto?

    suspend fun deleteTaskInList(taskListId: String, taskId: String): Boolean
}