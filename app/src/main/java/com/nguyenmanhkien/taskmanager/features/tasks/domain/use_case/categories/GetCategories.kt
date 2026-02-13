package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.categories

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetCategories(private val repository: CategoryRepository) {
    operator fun invoke(): Flow<List<Category>> {
        return repository.getAllCategories()
    }
}