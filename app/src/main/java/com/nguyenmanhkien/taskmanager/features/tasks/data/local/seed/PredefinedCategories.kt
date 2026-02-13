package com.nguyenmanhkien.taskmanager.features.tasks.data.local.seed

import android.graphics.Color
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category

object PredefinedCategories {
    val list = listOf(
        Category(
            id = 1,
            name = "Work",
            color = Color.parseColor("#D32F2F"), // red
            createdAt = 1
        ),
        Category(
            id = 2,
            name = "Travel",
            color = Color.parseColor("#1976D2"), // blue
            createdAt = 2
        ),
        Category(
            id = 3,
            name = "Finance",
            color = Color.parseColor("#388E3C"), // green
            createdAt = 3
        ),
        Category(
            id = 4,
            name = "Health",
            color = Color.parseColor("#FBC02D"), // yellow
            createdAt = 4
        ),
    )
}