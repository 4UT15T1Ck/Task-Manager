package com.nguyenmanhkien.taskmanager.features.tasks.data.local.seed

import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.TaskTemplate

object PredefinedTaskTemplates {
    val list = listOf(
        TaskTemplate(
            id = 1,
            title = "Drink more water",
            description = "Need more tear supply",
            createdAt = 1
        ),
        TaskTemplate(
            id = 2,
            title = "Go to bed early",
            description = "Go find your crush in the dreamland",
            createdAt = 2
        ),
        TaskTemplate(
            id = 3,
            title = "Get up early",
            description = "Early birds get to do the worms.",
            createdAt = 3
        ),
        TaskTemplate(
            id = 4,
            title = "Go shopping",
            description = "The eggs are on sale, gotta grab some..",
            createdAt = 4
        ),
        TaskTemplate(
            id = 5,
            title = "Study",
            description = "EXAM TOMORROW STUDY NOW!!!!",
            createdAt = 5
        ),
        TaskTemplate(
            id = 6,
            title = "Exercise",
            description = "Need some work on that muscle to pull things together",
            createdAt = 6
        ),
        TaskTemplate(
            id = 7,
            title = "Less screen time",
            description = "Go touch some grass",
            createdAt = 7
        ),
    )
}