package com.nguyenmanhkien.taskmanager.features.tasks.data.local.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao.CategoryDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class DatabaseCallback(
    private val categoryDao: CategoryDao
) : RoomDatabase.Callback() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch {
            categoryDao.upsertCategories(PredefinedCategories.list)
        }
    }
}