package com.nguyenmanhkien.taskmanager.features.tasks.domain.use_case.categories

import org.koin.core.annotation.Single

@Single
data class CategoryUseCases(
    val deleteCategory: DeleteCategory,
    val getCategories: GetCategories,
    val upsertCategory: UpsertCategory,
)