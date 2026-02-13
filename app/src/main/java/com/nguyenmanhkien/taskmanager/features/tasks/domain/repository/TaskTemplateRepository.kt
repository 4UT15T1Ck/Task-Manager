package com.nguyenmanhkien.taskmanager.features.tasks.domain.repository

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskTemplate
import kotlinx.coroutines.flow.Flow

interface TaskTemplateRepository {

    fun getAllTemplates(): Flow<List<TaskTemplate>>

    suspend fun getTemplateById(id: Int): TaskTemplate?

    suspend fun upsertTemplates(templates: List<TaskTemplate>)
}