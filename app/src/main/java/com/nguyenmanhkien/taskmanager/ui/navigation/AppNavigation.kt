package com.nguyenmanhkien.taskmanager.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_calendar.TaskCalendarScreen
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.AddTaskDialog
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_detail.TaskDetailScreen
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.TaskListScreen
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.TaskListViewModel
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list.TaskSelectionScreen
import com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_statistics.TaskStatisticsScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val taskListViewModel: TaskListViewModel = koinViewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedBottomNavIndex = when {
        currentRoute?.startsWith(Screen.TaskListScreen.route) == true -> 1
        currentRoute?.startsWith(Screen.CalendarScreen.route) == true -> 2
        currentRoute?.startsWith(Screen.StatisticsScreen.route) == true -> 3
        else -> 1
    }

    val showBottomBar = when (currentRoute) {
        Screen.TaskListScreen.route -> true
        Screen.CalendarScreen.route -> true
        Screen.StatisticsScreen.route -> true
        else -> false
    }

    val showFAB = when (currentRoute) {
        Screen.TaskListScreen.route -> true
        Screen.CalendarScreen.route -> true
        else -> false
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Drawer empty
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    TaskManagerBottomBar(
                        selectedIndex = selectedBottomNavIndex,
                        onItemSelected = { index ->
                            if (index == 0) {
                                scope.launch { drawerState.open() }
                            } else {
                                val route = when (index) {
                                    1 -> Screen.TaskListScreen.route
                                    2 -> Screen.CalendarScreen.route
                                    3 -> Screen.StatisticsScreen.route
                                    else -> Screen.TaskListScreen.route
                                }
                                navController.navigate(route) {
                                    popUpTo(Screen.TaskListScreen.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (showFAB) {
                    FloatingActionButton(
                        onClick = {
                            showAddTaskDialog = true
                        },
                        containerColor = Color(0xFF6B93F2),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }
            },
            containerColor = Color(0xFFF8F9FA)
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.TaskListScreen.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(route = Screen.TaskListScreen.route) {
                    TaskListScreen(
                        viewModel = taskListViewModel,
                        onTaskClick = { taskId ->
                            navController.navigate(
                                Screen.TaskDetailScreen.route + "?taskId=$taskId"
                            )
                        },
                        onCategorySelected = { categoryId ->
                            selectedCategoryId = categoryId
                        },
                        onManageCategories = {
                            navController.navigate(Screen.CategoryManagementScreen.route)
                        },
                        onEnterSelectionMode = {
                            navController.navigate(Screen.TaskSelectionScreen.route)
                        }
                    )
                }

                composable(route = Screen.TaskSelectionScreen.route) {
                    TaskSelectionScreen(
                        viewModel = taskListViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onCategorySelected = { categoryId ->
                            selectedCategoryId = categoryId
                        }
                    )
                }

                composable(route = Screen.CalendarScreen.route) {
                    TaskCalendarScreen(
                        onTaskClick = { taskId ->
                            navController.navigate(
                                Screen.TaskDetailScreen.route + "?taskId=$taskId"
                            )
                        }
                    )
                }

                composable(route = Screen.StatisticsScreen.route) {
                    TaskStatisticsScreen(
                        onProfileClick = {
                            navController.navigate(Screen.AccountScreen.route)
                        }
                    )
                }

                // Task Detail Screen
                composable(
                    route = Screen.TaskDetailScreen.route + "?taskId={taskId}",
                    arguments = listOf(
                        navArgument("taskId") {
                            type = NavType.IntType
                            defaultValue = -1
                        }
                    )
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
                    TaskDetailScreen(
                        taskId = taskId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToTaskDetail = { duplicatedTaskId ->
                            navController.navigate(Screen.TaskDetailScreen.route + "?taskId=$duplicatedTaskId") {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // Settings Screen
                composable(route = Screen.SettingsScreen.route) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Account Screen
                composable(route = Screen.AccountScreen.route) {
                    AccountScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Category Management Screen
                composable(route = Screen.CategoryManagementScreen.route) {
                    CategoryManagementScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // Show AddTaskDialog when needed
        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = {
                    showAddTaskDialog = false
                    selectedCategoryId = null
                },
                initialCategoryId = selectedCategoryId
            )
        }
    }
}


@Composable
@Suppress("UNUSED_PARAMETER")
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Settings Screen")
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun AccountScreen(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Account Screen")
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun CategoryManagementScreen(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Category Management Screen")
    }
}

@Composable
fun TaskManagerBottomBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
            label = { Text("Menu") },
            selected = selectedIndex == 0,
            onClick = { onItemSelected(0) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1976D2),
                selectedTextColor = Color(0xFF1976D2),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Menu, contentDescription = "Tasks") },
            label = { Text("Tasks") },
            selected = selectedIndex == 1,
            onClick = { onItemSelected(1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1976D2),
                selectedTextColor = Color(0xFF1976D2),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
            label = { Text("Calendar") },
            selected = selectedIndex == 2,
            onClick = { onItemSelected(2) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1976D2),
                selectedTextColor = Color(0xFF1976D2),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Mine") },
            label = { Text("Mine") },
            selected = selectedIndex == 3,
            onClick = { onItemSelected(3) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1976D2),
                selectedTextColor = Color(0xFF1976D2),
                indicatorColor = Color.Transparent
            )
        )
    }
}
