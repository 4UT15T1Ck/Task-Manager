package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_dialog.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup

@Composable
fun CategoryListPopup(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate height based on number of items (max 6 items)
    val itemHeight = 48.dp
    val maxItems = 6
    val itemsToShow = minOf(categories.size, maxItems)
    val totalHeight = itemHeight * itemsToShow

    Popup(
        alignment = Alignment.BottomStart,
        offset = IntOffset(0, -120), // Position above the button
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = modifier.width(140.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = totalHeight)
                    .padding(vertical = 8.dp)
            ) {
                items(categories) { category ->
                    CategoryItemInternal(
                        categoryName = category,
                        isSelected = category == selectedCategory,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryItemInternal(
    categoryName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = when {
        categoryName == "No Category" -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color(0xFF424242) // Dark gray
    }

    Text(
        text = categoryName,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        fontSize = 16.sp,
        color = textColor,
        style = MaterialTheme.typography.bodyLarge
    )
}

