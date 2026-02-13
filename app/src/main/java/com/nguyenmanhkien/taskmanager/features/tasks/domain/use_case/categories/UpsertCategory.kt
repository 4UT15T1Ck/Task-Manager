package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.categories

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.InvalidCategoryException
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.CategoryRepository
import org.koin.core.annotation.Factory

@Factory
class UpsertCategory(private val repository: CategoryRepository) {
    @Throws(InvalidCategoryException::class)
    suspend operator fun invoke(category: Category) {
        if (category.name.isBlank())
            throw InvalidCategoryException("The name of the category can't be empty.")
        repository.upsertCategory(category)
    }
}
