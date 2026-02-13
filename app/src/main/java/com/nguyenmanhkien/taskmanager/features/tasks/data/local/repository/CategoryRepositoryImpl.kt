package com.nguyenmanhkien.taskmanager.features.tasks.data.local.repository

import com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao.CategoryDao
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.SyncStatus
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return dao.getAllCategories()
    }

    override fun getCategoriesBySyncStatus(syncStatus: SyncStatus): Flow<List<Category>> {
        return dao.getCategoriesBySyncStatus(syncStatus)
    }

    override suspend fun getCategoryById(id: Int): Category? {
        return dao.getCategoryById(id)
    }

    override suspend fun getCategoryByGoogleId(googleId: String): Category? {
        return dao.getCategoryByGoogleTaskListId(googleId)
    }

    override suspend fun upsertCategory(category: Category) {
        dao.upsertCategory(category)
    }

    override suspend fun upsertCategories(categories: List<Category>) {
        dao.upsertCategories(categories)
    }

    override suspend fun deleteCategory(category: Category) {
        dao.deleteCategory(category)
    }

    override suspend fun updateSyncStatus(id: Int, syncStatus: SyncStatus) {
        dao.setCategorySyncStatus(id, syncStatus)
    }
}