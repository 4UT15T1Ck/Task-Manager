package com.app.taskmanager.core.domain.models

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

    @ColumnInfo(name = REMOVABLE_COLUMN)
    val isRemovable: Boolean = true,

    @ColumnInfo(name = CREATED_AT_COLUMN)
    val createdAt: Long,
) {
    companion object{
        const val TABLE_NAME = "categories"
        const val ID_COLUMN = "_id"
        const val NAME_COLUMN = "name"
        const val COLOR_COLUMN = "color"
        const val REMOVABLE_COLUMN = "removable"
        const val CREATED_AT_COLUMN = "created_at"
    }
}