package com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM ${Category.TABLE_NAME} " +
            "WHERE ${Category.SYNC_STATUS_COLUMN} != 'DELETED' " +
            "ORDER BY ${Category.CREATED_AT_COLUMN} ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query(
        "SELECT * FROM ${Category.TABLE_NAME}" +
                " WHERE ${Category.SYNC_STATUS_COLUMN} = :syncStatus"
    )
    fun getTasksBySyncStatus(syncStatus: SyncStatus): Flow<List<Category>>

    @Query(
        "SELECT * FROM ${Category.TABLE_NAME}" +
                " WHERE ${Category.ID_COLUMN} = :id"
    )
    suspend fun getCategoryById(id: Int): Category

    @Query(
        "SELECT * FROM ${Category.TABLE_NAME}" +
                " WHERE ${Category.GOOGLE_TASK_LIST_ID_COLUMN} = :googleTaskListId"
    )
    suspend fun getCategoryByGoogleTaskListId(googleTaskListId: String): Category

    @Query(
        "UPDATE ${Category.TABLE_NAME}" +
                " SET ${Category.SYNC_STATUS_COLUMN} = :syncStatus" +
                " WHERE ${Category.ID_COLUMN} = :id"
    )
    suspend fun setCategorySyncStatus(id: Int, syncStatus: SyncStatus)
}