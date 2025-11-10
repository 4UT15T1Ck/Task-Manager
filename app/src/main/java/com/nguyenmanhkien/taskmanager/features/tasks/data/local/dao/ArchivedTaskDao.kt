package com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.ArchivedTask
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchivedTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchivedTasks(archivedTasks: List<ArchivedTask>)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchivedTask(archivedTask: ArchivedTask)

    @Delete
    suspend fun deleteArchivedTask(archivedTask: ArchivedTask)

    @Delete
    suspend fun deleteArchivedTasks(archivedTasks: List<ArchivedTask>)

    @Query(
        "SELECT * FROM ${ArchivedTask.TABLE_NAME} " +
                "WHERE ${ArchivedTask.CATEGORY_ID_COLUMN} = :categoryId"
    )
    fun getArchivedTasksByCategoryId(categoryId: Int): Flow<List<ArchivedTask>>

    @Query(
        "SELECT * FROM ${ArchivedTask.TABLE_NAME} " +
                " ORDER BY ${ArchivedTask.DUE_AT_COLUMN} ASC"
    )
    fun getArchivedTasks(): Flow<List<ArchivedTask>>

    @Query(
        "SELECT * FROM ${ArchivedTask.TABLE_NAME} " +
                " WHERE ${ArchivedTask.STATUS_COLUMN} = :status"
    )
    fun getArchivedTasksByStatus(status: TaskStatus): Flow<List<ArchivedTask>>

    @Query(
        "SELECT * FROM ${ArchivedTask.TABLE_NAME} " +
                " WHERE ${ArchivedTask.DUE_AT_COLUMN} >= :startTime " +
                "AND ${ArchivedTask.START_AT_COLUMN} <= :endTime "
    )
    fun getArchivedTasksInTimeRange(startTime: Long, endTime: Long): Flow<List<ArchivedTask>>

    @Query(
        "WITH RECURSIVE DateRange(day) AS (" +
                "SELECT :startTime " +
                "UNION ALL " +
                "SELECT day + 86400000 FROM DateRange WHERE day < :endTime" +
                ") " +
                "SELECT day FROM DateRange " +
                "WHERE EXISTS (" +
                "SELECT 1 FROM ${ArchivedTask.TABLE_NAME} " +
                " WHERE ${ArchivedTask.START_AT_COLUMN} <= (day + 86399999) " +
                "AND ${ArchivedTask.DUE_AT_COLUMN} >= day )"
    )
    fun getDaysWithArchivedTasksInRange(startTime: Long, endTime: Long): Flow<List<Long>>
}