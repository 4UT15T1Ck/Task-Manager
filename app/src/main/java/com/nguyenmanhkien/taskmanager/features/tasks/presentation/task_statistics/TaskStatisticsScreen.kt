package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nguyenmanhkien.taskmanager.core.domain.model.UserPreferences
import com.nguyenmanhkien.taskmanager.core.domain.model.Theme
import com.nguyenmanhkien.taskmanager.core.domain.model.ViewType

data class TaskStatistics(
    val completedTasks: Int,
    val pendingTasks: Int,
    val dailyCompletionData: List<Pair<String, Int>>, // Day of week to task count
    val categoryDistribution: Map<String, Int>,
    val tasksNext7Days: List<String>
)

@Composable
fun TaskStatisticsScreen(
    userPreferences: UserPreferences? = null,
    onProfileClick: () -> Unit = {}
) {
    // Mock data
    val mockStats = remember {
        TaskStatistics(
            completedTasks = 0,
            pendingTasks = 2,
            dailyCompletionData = listOf(
                "Sun" to 0, "Mon" to 0, "Tue" to 0,
                "Wed" to 0, "Thu" to 0, "Fri" to 0, "Sat" to 0
            ),
            categoryDistribution = mapOf(
                "xuuz" to 1,
                "No Category" to 1
            ),
            tasksNext7Days = emptyList()
        )
    }

    val mockUserPreferences = userPreferences ?: UserPreferences(
        theme = Theme.LIGHT,
        defaultView = ViewType.DEFAULT,
        syncEnabled = false,
        email = "user@example.com",
        displayName = "User Name",
        photoUrl = null,
        notificationsEnabled = true
    )

    var weekOffset by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile section (small header)
        item {
            ProfileHeader(
                userPreferences = mockUserPreferences,
                onClick = onProfileClick
            )
        }

        // Upgrade to PRO banner
        item {
            ProUpgradeBanner()
        }

        // Tasks Overview section
        item {
            Text(
                text = "Tasks Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            TaskOverviewCard(
                completedTasks = mockStats.completedTasks,
                pendingTasks = mockStats.pendingTasks
            )
        }

        // Completion of Daily Tasks Chart
        item {
            DailyCompletionChart(
                data = mockStats.dailyCompletionData,
                weekOffset = weekOffset,
                onPreviousWeek = { weekOffset-- },
                onNextWeek = { weekOffset++ }
            )
        }

        // Tasks in Next 7 Days
        item {
            TasksNext7DaysSection(tasks = mockStats.tasksNext7Days)
        }

        // Pending Tasks in Categories (Pie Chart)
        item {
            CategoryDistributionSection(
                categoryDistribution = mockStats.categoryDistribution,
                weekOffset = weekOffset
            )
        }
    }
}

@Composable
fun ProfileHeader(
    userPreferences: UserPreferences,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile picture placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userPreferences.displayName?.firstOrNull()?.toString() ?: "U",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userPreferences.displayName ?: "User",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = userPreferences.email ?: "",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ProUpgradeBanner() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB3D4FF)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to upgrade screen */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Upgrade to PRO",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Unlock all PRO features",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👑",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PRO",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B93F2)
                    )
                }
            }
        }
    }
}

@Composable
fun TaskOverviewCard(
    completedTasks: Int,
    pendingTasks: Int
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Completed Tasks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = completedTasks.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Completed Tasks",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(Color.LightGray)
            )

            // Pending Tasks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pendingTasks.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Pending Tasks",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DailyCompletionChart(
    data: List<Pair<String, Int>>,
    weekOffset: Int,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with date range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Completion of Daily Tasks",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPreviousWeek, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Week",
                            tint = Color.Gray
                        )
                    }
                    Text(
                        text = "12/28-1/3",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    IconButton(onClick = onNextWeek, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Week",
                            tint = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simple bar chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                if (data.all { it.second == 0 }) {
                    Text(
                        text = "No task data",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                } else {
                    // Simple chart visualization
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        data.forEach { (day, count) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height((count * 15).dp.coerceAtLeast(2.dp))
                                        .background(Color(0xFF6B93F2), RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = day,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TasksNext7DaysSection(tasks: List<String>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tasks in Next 7 Days",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = "No upcoming tasks",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                tasks.forEach { task ->
                    Text(
                        text = "• $task",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDistributionSection(
    categoryDistribution: Map<String, Int>,
    weekOffset: Int
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pending Tasks in Categories",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "▼",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "In 30 days",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pie chart and legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simple pie chart representation (circle)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xFF6B93F2), CircleShape)
                        .border(4.dp, Color(0xFF9BB7F5), CircleShape)
                )

                // Legend
                Column(
                    modifier = Modifier.weight(1f).padding(start = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryDistribution.forEach { (category, count) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        if (category == "xuuz") Color(0xFF4169E1) else Color(0xFF9BB7F5),
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$category  $count",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, apiLevel = 34)
@Composable
fun PreviewTaskStatisticsScreen() {
    MaterialTheme {
        TaskStatisticsScreen()
    }
}

