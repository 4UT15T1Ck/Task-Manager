package com.nguyenmanhkien.taskmanager.features.tasks.data.local.seed

import com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao.CategoryDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class DatabaseInitializer(
    private val categoryDao: CategoryDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize() {
        scope.launch {
            try {
                if (categoryDao.getAllCategories().first().isEmpty()) {
                    categoryDao.upsertCategories(PredefinedCategories.list)
                }
            } catch (_: Exception) {}
        }
    }
}

