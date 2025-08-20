package com.app.taskmanager.core.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.taskmanager.core.domain.models.Subtask
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<Subtask>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: Subtask)

    @Update
    suspend fun updateSubtask(subtask: Subtask)

    @Delete
    suspend fun deleteSubtask(subtask: Subtask)

    @Query("SELECT * FROM ${Subtask.TABLE_NAME} " +
            "WHERE ${Subtask.PARENT_TASK_ID_COLUMN} = :taskId")
    fun getAllSubtasksByTaskId(taskId: Int): Flow<List<Subtask>>
}