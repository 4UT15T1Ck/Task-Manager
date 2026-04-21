package com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.CategoryTaskCount
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.DayTaskSummary
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.SyncStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Task
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskPriority
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Upsert
    suspend fun upsertTask(task: Task): Long

    @Upsert
    suspend fun upsertTasks(tasks: List<Task>)

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.ID_COLUMN} = :id " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'"
    )
    suspend fun getTaskById(id: Int): Task?

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.ID_COLUMN} = :id " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'"
    )
    fun getTaskByIdFlow(id: Int): Flow<Task?>

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.PARENT_ID_COLUMN} = :taskId " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED' " +
                "ORDER BY ${Task.CREATED_AT_COLUMN} ASC"
    )
    suspend fun getSubtasksForTaskOnce(taskId: Int): List<Task>


    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.PARENT_ID_COLUMN} = :taskId " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED' " +
                "ORDER BY ${Task.CREATED_AT_COLUMN} ASC"
    )
    fun getSubtasksForTaskFlow(taskId: Int): Flow<List<Task>>

    @Query(
        "SELECT ${Task.CATEGORY_ID_COLUMN} AS categoryId, COUNT(${Task.ID_COLUMN}) AS taskCount" +
                " FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.PARENT_ID_COLUMN} IS NULL" +
                " AND ${Task.CATEGORY_ID_COLUMN} IS NOT NULL" +
                " AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'" +
                " GROUP BY ${Task.CATEGORY_ID_COLUMN}"
    )
    fun getParentTaskCountsByCategory(): Flow<List<CategoryTaskCount>>

    @Transaction
    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.SYNC_STATUS_COLUMN} != 'DELETED' " +
                "AND ${Task.PARENT_ID_COLUMN} IS NULL " +
                "AND (CASE WHEN :categoryId IS NULL THEN 1 ELSE ${Task.CATEGORY_ID_COLUMN} = :categoryId END) " +
                "AND (CASE WHEN :startTime IS NULL THEN 1 ELSE ${Task.DUE_AT_COLUMN} >= :startTime END) " +
                "AND (CASE WHEN :endTime IS NULL THEN 1 ELSE ${Task.START_AT_COLUMN} <= :endTime END) " +
                "AND (CASE WHEN :searchQuery IS NULL OR :searchQuery = '' THEN 1 " +
                "ELSE (${Task.TITLE_COLUMN} LIKE '%' || :searchQuery || '%' " +
                "OR ${Task.DESCRIPTION_COLUMN} LIKE '%' || :searchQuery || '%') END) " +
                "ORDER BY ${Task.DUE_AT_COLUMN} ASC"
    )
    fun getFilteredTasks(
        categoryId: Int? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        searchQuery: String? = null
    ): Flow<List<Task>>

    @Delete
    suspend fun deleteTask(task: Task)

    @Delete
    suspend fun deleteTasks(tasks: List<Task>)

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.STATUS_COLUMN} = :status " +
                "AND ${Task.PARENT_ID_COLUMN} IS NULL " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'"
    )
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.PRIORITY_COLUMN} = :priority " +
                "AND ${Task.PARENT_ID_COLUMN} IS NULL " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'"
    )
    fun getTasksByPriority(priority: TaskPriority): Flow<List<Task>>

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.SYNC_STATUS_COLUMN} = :syncStatus " +
                "AND ${Task.PARENT_ID_COLUMN} IS NULL "
    )
    fun getTasksBySyncStatus(syncStatus: SyncStatus): Flow<List<Task>>

    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.GOOGLE_TASK_ID_COLUMN} = :googleCalendarTaskId " +
                "AND ${Task.PARENT_ID_COLUMN} IS NULL " +
                " AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED'"
    )
    suspend fun getTaskByGoogleCalendarEventId(googleCalendarTaskId: Int): Task?

    @Query(
        "SELECT ${Task.ID_COLUMN} FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.STATUS_COLUMN} = 'IN_PROGRESS' " +
                "AND ${Task.DUE_AT_COLUMN} < :currentTime " +
                "AND ${Task.PARENT_ID_COLUMN} IS NULL " +
                "ORDER BY ${Task.DUE_AT_COLUMN} ASC"
    )
    suspend fun getOverdueInProgressTasks(currentTime: Long): List<Int>


    @Query(
        "SELECT * FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.DUE_AT_COLUMN} >= :startTime " +
                "AND ${Task.START_AT_COLUMN} <= :endTime " +
                "AND ${Task.PARENT_ID_COLUMN} IS NULL " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED' " +
                "ORDER BY ${Task.START_AT_COLUMN} ASC"
    )
    fun getParentTasksInRange(startTime: Long, endTime: Long): Flow<List<Task>>

    @Query(
        "WITH RECURSIVE DateRange(day) AS (" +
                "SELECT :startTime " +
                "UNION ALL " +
                "SELECT day + 86400000 FROM DateRange WHERE day < :endTime )" +
                "SELECT day FROM DateRange " +
                "WHERE EXISTS (" +
                "SELECT 1 FROM ${Task.TABLE_NAME}" +
                " WHERE ${Task.START_AT_COLUMN} <= (day + 86399999) AND ${Task.DUE_AT_COLUMN} >= day" +
                " AND ${Task.PARENT_ID_COLUMN} IS NULL " +
                " AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED' )"
    )
    fun getDaysWithTasksInRange(startTime: Long, endTime: Long): Flow<List<Long>>

    @Query(
        "WITH RECURSIVE DateRange(day) AS (" +
                "SELECT :startTime " +
                "UNION ALL " +
                "SELECT day + 86400000 FROM DateRange WHERE day < :endTime" +
                ") " +
                "SELECT day AS dayStart, " +
                "COUNT(${Task.ID_COLUMN}) AS totalCount, " +
                "SUM(CASE WHEN ${Task.STATUS_COLUMN} = 'COMPLETED' THEN 1 ELSE 0 END) AS completedCount " +
                "FROM DateRange LEFT JOIN ${Task.TABLE_NAME} " +
                "ON ${Task.START_AT_COLUMN} <= (day + 86399999) " +
                "AND ${Task.DUE_AT_COLUMN} >= day " +
                "AND ${Task.PARENT_ID_COLUMN} IS NULL " +
                "AND ${Task.SYNC_STATUS_COLUMN} != 'DELETED' " +
                "GROUP BY day HAVING totalCount > 0 " +
                "ORDER BY day ASC"
    )
    fun getDayTaskSummariesInRange(startTime: Long, endTime: Long): Flow<List<DayTaskSummary>>

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.STATUS_COLUMN} = :status" +
                " WHERE ${Task.ID_COLUMN} = :id"
    )
    suspend fun setTaskStatus(id: Int, status: TaskStatus)

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.STATUS_COLUMN} = :status" +
                " WHERE ${Task.ID_COLUMN} IN (:ids)"
    )
    suspend fun setTasksStatus(ids: List<Int>, status: TaskStatus)

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.STATUS_COLUMN} = :status, ${Task.COMPLETION_DATE_COLUMN} = :completionDate" +
                " WHERE ${Task.ID_COLUMN} = :id"
    )
    suspend fun setTaskStatusWithCompletionDate(id: Int, status: TaskStatus, completionDate: Long?)

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.STATUS_COLUMN} = :status, ${Task.COMPLETION_DATE_COLUMN} = :completionDate" +
                " WHERE ${Task.ID_COLUMN} IN (:ids)"
    )
    suspend fun setTasksStatusWithCompletionDate(ids: List<Int>, status: TaskStatus, completionDate: Long?)

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.STATUS_COLUMN} = :status, ${Task.COMPLETION_DATE_COLUMN} = :completionDate" +
                " WHERE ${Task.PARENT_ID_COLUMN} = :parentId"
    )
    suspend fun setSubtasksStatusWithCompletionDateByParentId(parentId: Int, status: TaskStatus, completionDate: Long?)

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.PRIORITY_COLUMN} = :priority" +
                " WHERE ${Task.ID_COLUMN} = :id"
    )
    suspend fun setTaskPriority(id: Int, priority: TaskPriority)

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.SYNC_STATUS_COLUMN} = :syncStatus" +
                " WHERE ${Task.ID_COLUMN} = :id"
    )
    suspend fun setTaskSyncStatus(id: Int, syncStatus: SyncStatus)

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.SYNC_STATUS_COLUMN} = :syncStatus" +
                " WHERE ${Task.ID_COLUMN} IN (:ids)"
    )
    suspend fun setTasksSyncStatus(ids: List<Int>, syncStatus: SyncStatus)

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.SYNC_STATUS_COLUMN} = :syncStatus" +
                " WHERE ${Task.PARENT_ID_COLUMN} IN (:parentIds)"
    )
    suspend fun setSubtasksSyncStatusByParentIds(parentIds: List<Int>, syncStatus: SyncStatus)

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.CATEGORY_ID_COLUMN} = :categoryId" +
                " WHERE ${Task.ID_COLUMN} = :id"
    )
    suspend fun setTaskCategory(id: Int, categoryId: Int?)

    @Query(
        "UPDATE ${Task.TABLE_NAME}" +
                " SET ${Task.CATEGORY_ID_COLUMN} = :categoryId" +
                " WHERE ${Task.ID_COLUMN} IN (:ids)"
    )
    suspend fun setTasksCategory(ids: List<Int>, categoryId: Int?)

    @Transaction
    suspend fun setTasksAndSubtasksSyncStatus(parentIds: List<Int>, syncStatus: SyncStatus) {
        if (parentIds.isEmpty()) {
            return
        }
        setTasksSyncStatus(parentIds, syncStatus)
        setSubtasksSyncStatusByParentIds(parentIds, syncStatus)
    }

    @Transaction
    suspend fun setTaskAndSubtasksStatusWithCompletionDate(parentId: Int, status: TaskStatus, completionDate: Long?) {
        setTaskStatusWithCompletionDate(parentId, status, completionDate)
        setSubtasksStatusWithCompletionDateByParentId(parentId, status, completionDate)
    }
}
