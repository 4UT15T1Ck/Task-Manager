package com.app.taskmanager.core.data.db.seed

import android.graphics.Color
import com.app.taskmanager.core.domain.models.Category

object PredefinedCategories {
    val list = listOf(
        Category(
            id = 1,
            name = "Work",
            color = Color.parseColor("#D32F2F"), // red
            isRemovable = false,
            createdAt = 1
        ),
        Category(
            id = 2,
            name = "Travel",
            color = Color.parseColor("#1976D2"), // blue
            isRemovable = false,
            createdAt = 2
        ),
        Category(
            id = 3,
            name = "Finance",
            color = Color.parseColor("#388E3C"), // green
            isRemovable = false,
            createdAt = 3
        ),
        Category(
            id = 4,
            name = "Health",
            color = Color.parseColor("#FBC02D"), // yellow
            isRemovable = false,
            createdAt = 4
        ),
    )
}