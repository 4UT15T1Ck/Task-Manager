package com.nguyenmanhkien.taskmanager.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.nguyenmanhkien.taskmanager.core.data.db.AppDatabase
import com.nguyenmanhkien.taskmanager.core.data.repository.ApiPreferencesRepositoryImpl
import com.nguyenmanhkien.taskmanager.core.data.repository.TaskPreferencesRepositoryImpl
import com.nguyenmanhkien.taskmanager.core.data.repository.UserPreferencesRepositoryImpl
import com.nguyenmanhkien.taskmanager.core.domain.repository.ApiPreferencesRepository
import com.nguyenmanhkien.taskmanager.core.domain.repository.TaskPreferencesRepository
import com.nguyenmanhkien.taskmanager.core.domain.repository.UserPreferencesRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
private val Context.taskDataStore: DataStore<Preferences> by preferencesDataStore(name = "task_preferences")
private val Context.apiDataStore: DataStore<Preferences> by preferencesDataStore(name = "api_preferences")

@ComponentScan("com.nguyenmanhkien.taskmanager")
@Module
class CoreFactoryModule {

    @Single
    fun createRoomDatabase(
        context: Context
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME)
            .build()
    }

    @Single
    fun createHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = Logger.ANDROID
                level = LogLevel.ALL
            }
        }
    }

    @Single
    @Named("userDataStore")
    fun provideUserDataStore(context: Context): DataStore<Preferences> {
        return context.userDataStore
    }

    @Single
    fun provideUserPreferencesRepository(@Named("userDataStore") dataStore: DataStore<Preferences>): UserPreferencesRepository {
        return UserPreferencesRepositoryImpl(dataStore)
    }

    @Single
    @Named("taskDataStore")
    fun provideTaskDataStore(context: Context): DataStore<Preferences> {
        return context.taskDataStore
    }

    @Single
    fun provideTaskPreferencesRepository(@Named("taskDataStore") dataStore: DataStore<Preferences>): TaskPreferencesRepository {
        return TaskPreferencesRepositoryImpl(dataStore)
    }

    @Single
    @Named("apiDataStore")
    fun provideApiDataStore(context: Context): DataStore<Preferences> {
        return context.apiDataStore
    }

    @Single
    fun provideApiPreferencesRepository(@Named("apiDataStore") dataStore: DataStore<Preferences>): ApiPreferencesRepository {
        return ApiPreferencesRepositoryImpl(dataStore)
    }
}
