package com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskTemplateDao {
    @Upsert
    suspend fun upsertTemplates(templates: List<TaskTemplate>)

    @Query("SELECT * FROM ${TaskTemplate.TABLE_NAME} ORDER BY ${TaskTemplate.TITLE_COLUMN} ASC")
    fun getAllTemplates(): Flow<List<TaskTemplate>>

    @Query("SELECT * FROM ${TaskTemplate.TABLE_NAME} WHERE ${TaskTemplate.ID_COLUMN} = :id")
    suspend fun getTemplateById(id: Int): TaskTemplate?

}
