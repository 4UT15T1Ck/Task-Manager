package com.nguyenmanhkien.taskmanager.features.tasks.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleTaskWrapperDto(val items: List<GoogleTaskResponseDto> = emptyList())
@Serializable
data class GoogleTaskListWrapperDto(val items: List<GoogleTaskListResponseDto> = emptyList())
