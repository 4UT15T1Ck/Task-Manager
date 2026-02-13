package com.nguyenmanhkien.taskmanager.features.tasks.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = Category.TABLE_NAME)
data class Category(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID_COLUMN)
    val id: Int = 0,

    @ColumnInfo(name = NAME_COLUMN)
    val name: String,

    @ColumnInfo(name = COLOR_COLUMN)
    val color: Int,

    @ColumnInfo(name = SYNC_STATUS_COLUMN)
    val syncStatus: SyncStatus = SyncStatus.CREATED,

    @ColumnInfo(name = GOOGLE_TASK_LIST_ID_COLUMN)
    val googleTaskListId: String? = null,

    @ColumnInfo(name = CREATED_AT_COLUMN)
    val createdAt: Long,
) {
    companion object{
        const val TABLE_NAME = "categories"
        const val ID_COLUMN = "_id"
        const val NAME_COLUMN = "name"
        const val COLOR_COLUMN = "color"
        const val SYNC_STATUS_COLUMN = "sync_status"
        const val GOOGLE_TASK_LIST_ID_COLUMN = "google_task_list_id"
        const val CREATED_AT_COLUMN = "created_at"
    }
}