package com.weichatcheck.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NavSidebar(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(8.dp)) {
        Screen.entries.forEach { screen ->
            val selected = screen == currentScreen
            Button(
                onClick = { onScreenChange(screen) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                    contentColor = if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Icon(screen.icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(screen.label)
            }
        }
    }
}
