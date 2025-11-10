package com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.SyncStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskCompletionType
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.ID_COLUMN} = :id " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'"
    )
    suspend fun getTaskById(id: Int): Task

    @Transaction
    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.SYNC_STATUS_COLUMN} != 'DELETED' " +
                "AND ${Task.PARENT_ID_COLUMN} IS NULL " +
                "ORDER BY ${Task.DUE_AT_COLUMN} ASC"
    )
    fun getTopLevelTasks(): Flow<List<Task>>

    @Query("SELECT * FROM ${Task.TABLE_NAME}" +
            " WHERE ${Task.PARENT_ID_COLUMN} = :taskId " +
            "ORDER BY ${Task.CREATED_AT_COLUMN} ASC")
    fun getSubtasksForTask(taskId: Int): Flow<List<Task>>

    @Delete
    suspend fun deleteTask(task: Task)

    @Delete
    suspend fun deleteTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.STATUS_COLUMN} = :status " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'"
    )
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.PRIORITY_COLUMN} = :priority " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'"
    )
    fun getTasksByPriority(priority: TaskPriority): Flow<List<Task>>

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.COMPLETION_TYPE_COLUMN} = :completionType " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'"
    )
    fun getTasksByCompletionType(completionType: TaskCompletionType): Flow<List<Task>>

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.SYNC_STATUS_COLUMN} = :syncStatus"
    )
    fun getTasksBySyncStatus(syncStatus: SyncStatus): Flow<List<Task>>

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.CATEGORY_ID_COLUMN} = :categoryId " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'"
    )
    fun getTasksByCategoryId(categoryId: Int): Flow<List<Task>>

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.GOOGLE_TASK_ID_COLUMN} = :googleCalendarTaskId "
                + " AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'"
    )
    suspend fun getTaskByGoogleCalendarEventId(googleCalendarTaskId: Int): Task

    @Query(
        "SELECT ${Task.ID_COLUMN} FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.STATUS_COLUMN} = 'IN_PROGRESS' " +
                "AND ${Task.DUE_AT_COLUMN} < :currentTime " +
                "ORDER BY ${Task.DUE_AT_COLUMN} ASC"
    )
    suspend fun getOverdueInProgressTasks(currentTime: Long): List<Int>

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.DUE_AT_COLUMN} >= :startTime AND ${Task.START_AT_COLUMN} <= :endTime "
                + "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'"
    )
    fun getTasksInTimeRange(startTime: Long, endTime: Long): Flow<List<Task>>

    @Query(
        "WITH RECURSIVE DateRange(day) AS (" +
                "SELECT :startTime " +
                "UNION ALL " +
                "SELECT day + 86400000 FROM DateRange WHERE day < :endTime )" +
                "SELECT day FROM DateRange " +
                "WHERE EXISTS (" +
                "SELECT 1 FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.START_AT_COLUMN} <= (day + 86399999) AND ${Task.DUE_AT_COLUMN} >= day" +
                " AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED' )"
    )
    fun getDaysWithTasksInRange(startTime: Long, endTime: Long): Flow<List<Long>>

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.STATUS_COLUMN} = :status" +
                " WHERE ${Task.ID_COLUMN} = :id"
    )
    suspend fun setTaskStatus(id: Int, status: TaskStatus)

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.SYNC_STATUS_COLUMN} = :syncStatus" +
                " WHERE ${Task.ID_COLUMN} = :id"
    )
    suspend fun setTaskSyncStatus(id: Int, syncStatus: SyncStatus)
}