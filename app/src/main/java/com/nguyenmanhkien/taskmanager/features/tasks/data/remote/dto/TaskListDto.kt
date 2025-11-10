package com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleTaskListResponseDto(val id: String, val title: String?)

@Serializable
data class GoogleTaskListCreateRequestDto(val title: String?)

