package com.nguyenmanhkien.taskmanager.features.tasks.data.remote

import com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto.GoogleTaskListCreateRequestDto
import com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto.GoogleTaskListResponseDto
import com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto.GoogleTaskListWrapperDto
import com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto.GoogleTaskRequestDto
import com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto.GoogleTaskResponseDto
import com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto.GoogleTaskWrapperDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.koin.core.annotation.Single

@Single
class TaskServiceImpl(private val client: HttpClient) : TaskService {

    override suspend fun getTaskLists(): GoogleTaskListWrapperDto {
        return try {
            client.get(HttpRoutes.TASK_LISTS).body()
        } catch (e: Exception) {
            println("Error getting TaskLists: ${e.message}")
            GoogleTaskListWrapperDto()
        }
    }

    override suspend fun createTaskList(request: GoogleTaskListCreateRequestDto): GoogleTaskListResponseDto? {
        return try {
            client.post(HttpRoutes.TASK_LISTS) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            println("Error creating TaskList: ${e.message}")
            null
        }
    }

    override suspend fun getTasksInList(taskListId: String): GoogleTaskWrapperDto {
        return try {
            client.get(HttpRoutes.tasksInList(taskListId)).body()
        } catch (e: Exception) {
            println("Error getting Tasks in list $taskListId: ${e.message}")
            GoogleTaskWrapperDto()
        }
    }

    override suspend fun createTaskInList(
        taskListId: String,
        request: GoogleTaskRequestDto
    ): GoogleTaskResponseDto? {
        return try {
            client.post(HttpRoutes.tasksInList(taskListId)) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            println("Error creating Task in list $taskListId: ${e.message}")
            null
        }
    }

    override suspend fun updateTaskInList(
        taskListId: String,
        taskId: String,
        request: GoogleTaskRequestDto
    ): GoogleTaskResponseDto? {
        return try {
            client.put(HttpRoutes.taskById(taskListId, taskId)) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            println("Error updating Task $taskId: ${e.message}")
            null
        }
    }

    override suspend fun deleteTaskInList(taskListId: String, taskId: String): Boolean {
        return try {
            val response = client.delete(HttpRoutes.taskById(taskListId, taskId))
            response.status == HttpStatusCode.NoContent
        } catch (e: Exception) {
            println("Error deleting Task $taskId: ${e.message}")
            false
        }
    }
}