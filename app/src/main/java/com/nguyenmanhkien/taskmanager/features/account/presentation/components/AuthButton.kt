package com.nguyenmanhkien.taskmanager.features.account.presentation.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

// TODO: fix this ugly button
@Composable
fun AuthButton(
    onClick: () -> Unit,
    text: String,
) {
    Button(onClick = onClick) {
        Text(text = text)
    }
}