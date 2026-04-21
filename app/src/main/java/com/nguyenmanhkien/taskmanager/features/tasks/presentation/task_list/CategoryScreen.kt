package com.nguyenmanhkien.taskmanager.features.tasks.presentation.task_list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import com.nguyenmanhkien.taskmanager.features.tasks.domain.model.Category
import org.koin.androidx.compose.koinViewModel

private const val DefaultCategoryColor = 0xFF6B93F2.toInt()
private const val CategoryTitleMaxLength = 36
private val CategoryColorOptions = listOf(
	0xFFF26B3A.toInt(),
	0xFFE8C547.toInt(),
	0xFF86B84B.toInt(),
	0xFF4FA49B.toInt(),
	0xFF6B93F2.toInt(),
	0xFFB18BE8.toInt()
)

@Composable
fun CategoryScreen(
	onNavigateBack: () -> Unit,
	viewModel: TaskListViewModel = koinViewModel()
) {
	val state by viewModel.state
	var editingCategory by remember { mutableStateOf<Category?>(null) }
	var isEditorVisible by remember { mutableStateOf(false) }
	var categoryPendingDelete by remember { mutableStateOf<Category?>(null) }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 16.dp, vertical = 12.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			IconButton(onClick = onNavigateBack) {
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ArrowBack,
					contentDescription = "Back"
				)
			}

			Text(
				text = "Category Menu",
				style = MaterialTheme.typography.titleLarge,
				modifier = Modifier.weight(1f)
			)

			IconButton(
				onClick = {
					editingCategory = null
					isEditorVisible = true
				}
			) {
				Icon(
					imageVector = Icons.Default.Add,
					contentDescription = "Add category"
				)
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			items(state.categories, key = { it.id }) { category ->
				CategoryRow(
					category = category,
					taskCount = state.categoryTaskCounts[category.id] ?: 0,
					onEdit = {
						editingCategory = category
						isEditorVisible = true
					},
					onDelete = {
						categoryPendingDelete = category
					}
				)
			}

			item {
				TextButton(
					onClick = {
						editingCategory = null
						isEditorVisible = true
					},
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = "+ Create new category",
						color = Color(DefaultCategoryColor),
						style = MaterialTheme.typography.titleMedium
					)
				}
			}
		}
	}

	if (isEditorVisible) {
		CategoryEditorDialog(
			initialCategory = editingCategory,
			onDismiss = { isEditorVisible = false },
			onSave = { title, color ->
				val trimmedTitle = title.trim()
				if (trimmedTitle.isBlank()) {
					return@CategoryEditorDialog
				}

				val category = editingCategory?.copy(
					name = trimmedTitle,
					color = color
				) ?: Category(
					name = trimmedTitle,
					color = color,
					createdAt = System.currentTimeMillis()
				)

				viewModel.onEvent(TaskListEvent.UpsertCategory(category))
				isEditorVisible = false
				editingCategory = null
			}
		)
	}

	categoryPendingDelete?.let { category ->
		DeleteCategoryDialog(
			categoryName = category.name,
			onCancel = { categoryPendingDelete = null },
			onConfirm = {
				viewModel.onEvent(TaskListEvent.DeleteCategory(category))
				categoryPendingDelete = null
			}
		)
	}
}

@Composable
private fun CategoryRow(
	category: Category,
	taskCount: Int,
	onEdit: () -> Unit,
	onDelete: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.background(color = Color.White, shape = RoundedCornerShape(12.dp))
			.padding(horizontal = 12.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier
				.size(18.dp)
				.background(
					color = Color(if (category.color == 0) DefaultCategoryColor else category.color),
					shape = RoundedCornerShape(4.dp)
				)
		)

		Spacer(modifier = Modifier.width(10.dp))

		Text(
			text = category.name,
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier.weight(1f)
		)

		Text(
			text = taskCount.toString(),
			style = MaterialTheme.typography.bodyMedium,
			color = Color(0xFF667085)
		)

		IconButton(onClick = onEdit) {
			Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit category")
		}

		IconButton(onClick = onDelete) {
			Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete category")
		}
	}
}

@Composable
private fun CategoryEditorDialog(
	initialCategory: Category?,
	onDismiss: () -> Unit,
	onSave: (String, Int) -> Unit
) {
	var title by remember(initialCategory?.id) { mutableStateOf(initialCategory?.name.orEmpty()) }
	var selectedColor by remember(initialCategory?.id) {
		mutableStateOf(initialCategory?.color ?: DefaultCategoryColor)
	}

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(dismissOnClickOutside = true)
	) {
		Surface(
			shape = RoundedCornerShape(18.dp),
			color = Color.White,
			modifier = Modifier
				.fillMaxWidth(0.95f)
				.heightIn(max = 760.dp)
		) {
			Column(
				modifier = Modifier.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Text(
					text = if (initialCategory == null) "Create new category" else "Edit category",
					style = MaterialTheme.typography.headlineSmall,
					fontWeight = FontWeight.Bold
				)

				OutlinedTextField(
					value = title,
					onValueChange = { newValue ->
						if (newValue.length <= CategoryTitleMaxLength) {
							title = newValue
						}
					},
					placeholder = { Text("Category title") },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true
				)

				Text(
					text = "${title.length}/$CategoryTitleMaxLength",
					style = MaterialTheme.typography.bodySmall,
					color = Color(0xFF98A2B3),
					modifier = Modifier.align(Alignment.End)
				)

				Text(
					text = "Category color",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.SemiBold
				)

				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					CategoryColorOptions.forEach { colorValue ->
						val color = Color(colorValue)
						Box(
							modifier = Modifier
								.size(36.dp)
								.border(
									width = if (selectedColor == colorValue) 2.dp else 0.dp,
									color = if (selectedColor == colorValue) {
										MaterialTheme.colorScheme.primary
									} else {
										Color.Transparent
									},
									shape = CircleShape
								)
								.background(color = color, shape = CircleShape)
								.clickable { selectedColor = colorValue }
						)
					}
				}

				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End
				) {
					TextButton(onClick = onDismiss) {
						Text("Cancel")
					}
					TextButton(
						onClick = { onSave(title, selectedColor) },
						enabled = title.trim().isNotEmpty()
					) {
						Text("Save")
					}
				}
			}
		}
	}
}

@Composable
private fun DeleteCategoryDialog(
	categoryName: String,
	onCancel: () -> Unit,
	onConfirm: () -> Unit
) {
	Dialog(
		onDismissRequest = onCancel,
		properties = DialogProperties(dismissOnClickOutside = true)
	) {
		Surface(
			shape = RoundedCornerShape(18.dp),
			color = Color.White,
			modifier = Modifier
				.fillMaxWidth(0.9f)
				.heightIn(max = 760.dp)
		) {
			Column(modifier = Modifier.padding(16.dp)) {
				Text("Are you sure you want to delete '$categoryName'?")
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = "Tasks in this category will stay and become uncategorized.",
					style = MaterialTheme.typography.bodySmall,
					color = Color(0xFF667085)
				)
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End
				) {
					TextButton(onClick = onCancel) {
						Text("Cancel")
					}
					TextButton(onClick = onConfirm) {
						Text("Delete")
					}
				}
			}
		}
	}
}
