package com.nguyenmanhkien.taskmanager.features.tasks.domain.repository

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>

    fun getCategoriesBySyncStatus(syncStatus: SyncStatus): Flow<List<Category>>

    suspend fun getCategoryById(id: Int): Category?

    suspend fun getCategoryByGoogleId(googleId: String): Category?

    suspend fun upsertCategory(category: Category)

    suspend fun upsertCategories(categories: List<Category>)

    suspend fun deleteCategory(category: Category)

    suspend fun updateSyncStatus(id: Int, syncStatus: SyncStatus)
}