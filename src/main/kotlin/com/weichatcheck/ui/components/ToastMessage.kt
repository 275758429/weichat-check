package com.weichatcheck.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ToastMessage(message: String, onDismiss: () -> Unit) {
    LaunchedEffect(message) {
        delay(3000)
        onDismiss()
    }

    Box(Modifier.fillMaxSize().padding(bottom = 32.dp), contentAlignment = Alignment.BottomCenter) {
        Surface(
            color = MaterialTheme.colors.surface,
            elevation = 8.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(message, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
        }
    }
}
