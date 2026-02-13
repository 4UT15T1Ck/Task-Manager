package com.nguyenmanhkien.taskmanager.features.tasks.data.local.repository

import com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao.TaskTemplateDao
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskTemplate
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskTemplateRepository
import kotlinx.coroutines.flow.Flow

class TaskTemplateRepositoryImpl(
    private val dao: TaskTemplateDao
) : TaskTemplateRepository {
    override fun getAllTemplates(): Flow<List<TaskTemplate>> = dao.getAllTemplates()

    override suspend fun getTemplateById(id: Int): TaskTemplate? = dao.getTemplateById(id)

    override suspend fun upsertTemplates(templates: List<TaskTemplate>) = dao.upsertTemplates(templates)

}