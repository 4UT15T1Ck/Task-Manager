package com.app.taskmanager.core.domain.models

data class UserPreferences(
    val theme: Theme,
    val defaultView: ViewType,
    val syncEnabled: Boolean,
    val isLoggedIn: Boolean,
    val notificationsEnabled: Boolean
)

enum class Theme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System Default");

    companion object {
        fun fromString(value: String?): Theme {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: SYSTEM
        }
    }
}

enum class ViewType(val displayName: String) {
    DEFAULT("Default"),
    DAILY("Daily"),
    MONTHLY("Monthly");

    companion object {
        fun fromString(value: String?): ViewType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: DEFAULT
        }
    }
}