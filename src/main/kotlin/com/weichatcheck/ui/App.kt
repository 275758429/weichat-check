package com.weichatcheck.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.weichatcheck.model.ScanState
import com.weichatcheck.ui.components.ToastMessage

@Composable
fun App(viewModel: AppViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WeChat Monitor") },
                actions = {
                    val state by viewModel.scanState.collectAsState()
                    when (state) {
                        ScanState.IDLE, ScanState.PAUSED, ScanState.ERROR -> {
                            Button(onClick = { viewModel.startScan() }) {
                                Text("开始扫描")
                            }
                        }
                        ScanState.RUNNING -> {
                            Button(
                                onClick = { viewModel.stopScan() },
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.error)
                            ) {
                                Text("停止扫描")
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->
        Row(Modifier.padding(padding).fillMaxSize()) {
            NavSidebar(
                currentScreen = currentScreen,
                onScreenChange = { currentScreen = it },
                modifier = Modifier.width(200.dp).fillMaxHeight()
            )
            Box(Modifier.weight(1f).fillMaxHeight()) {
                when (currentScreen) {
                    Screen.DASHBOARD -> DashboardScreen(viewModel)
                    Screen.GROUPS -> GroupsScreen(viewModel)
                    Screen.KEYWORDS -> KeywordsScreen(viewModel)
                    Screen.CLUES -> CluesScreen(viewModel)
                    Screen.PUSH -> PushScreen(viewModel)
                }
            }
        }
    }

    val toast by viewModel.toastMessage.collectAsState()
    toast?.let { message ->
        ToastMessage(message, onDismiss = { viewModel.clearToast() })
    }
}

enum class Screen(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("状态概览", Icons.Default.Home),
    GROUPS("群聊管理", Icons.Default.Person),
    KEYWORDS("关键词", Icons.Default.Search),
    CLUES("检索记录", Icons.AutoMirrored.Filled.List),
    PUSH("推送设置", Icons.Default.Notifications)
}
