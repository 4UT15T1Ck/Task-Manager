package com.app.taskmanager.core.data.db.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.taskmanager.core.data.db.dao.CategoryDao
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
            categoryDao.insertCategories(PredefinedCategories.list)
        }
    }
}