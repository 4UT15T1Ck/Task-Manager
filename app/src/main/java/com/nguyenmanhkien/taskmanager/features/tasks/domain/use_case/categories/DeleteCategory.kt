package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.categories

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.CategoryRepository
import org.koin.core.annotation.Factory

@Factory
class DeleteCategory(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category) {
        repository.deleteCategory(category)
    }
}