package com.nguyenmanhkien.taskmanager.features.tasks.data.remote

object HttpRoutes {
    private const val BASE_URL = "https://www.googleapis.com/tasks/v1"
    const val TASK_LISTS = "$BASE_URL/users/@me/lists"

    fun taskListById(taskListId: String) = "$TASK_LISTS/$taskListId"
    fun tasksInList(taskListId: String) = "$TASK_LISTS/$taskListId/tasks"
    fun taskById(taskListId: String, taskId: String) = "$TASK_LISTS/$taskListId/tasks/$taskId"
}