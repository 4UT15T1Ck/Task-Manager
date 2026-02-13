package com.nguyenmanhkien.taskmanager.features.tasks.di

import com.nguyenmanhkien.taskmanager.core.data.db.AppDatabase
import com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao.CategoryDao
import com.nguyenmanhkien.taskmanager.features.tasks.data.local.dao.TaskDao
import com.nguyenmanhkien.taskmanager.features.tasks.data.local.repository.CategoryRepositoryImpl
import com.nguyenmanhkien.taskmanager.features.tasks.data.local.repository.TaskRepositoryImpl
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.CategoryRepository
import com.nguyenmanhkien.taskmanager.features.tasks.domain.repository.TaskRepository
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@ComponentScan("com.nguyenmanhkien.taskmanager.features.tasks")
@Module
class TasksFactoryModule {

    @Single
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Single
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Single
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
        return TaskRepositoryImpl(taskDao)
    }

    @Single
    fun provideCategoryRepository(categoryDao: CategoryDao): CategoryRepository {
        return CategoryRepositoryImpl(categoryDao)
    }
}

