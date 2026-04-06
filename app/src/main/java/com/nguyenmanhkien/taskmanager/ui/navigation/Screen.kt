package com.nguyenmanhkien.taskmanager.ui.navigation

sealed class Screen(val route: String) {
    // Main screens accessible from bottom navigation
    data object TaskListScreen : Screen("task_list_screen")
    data object TaskSelectionScreen : Screen("task_selection_screen")
    data object CalendarScreen : Screen("calendar_screen")
    data object StatisticsScreen : Screen("statistics_screen")

    // Task detail screen
    data object TaskDetailScreen : Screen("task_detail_screen")

    // Settings and other screens
    data object SettingsScreen : Screen("settings_screen")
    data object AccountScreen : Screen("account_screen")
    data object CategoryManagementScreen : Screen("category_management_screen")
}

