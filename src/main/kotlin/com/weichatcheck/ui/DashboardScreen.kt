package com.weichatcheck.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.weichatcheck.model.ScanState

@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val state by viewModel.scanState.collectAsState()
    val currentGroup by viewModel.currentGroup.collectAsState()
    val progress by viewModel.scanProgress.collectAsState()
    val todayHits by viewModel.todayHits.collectAsState()
    val groups by viewModel.groups.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("状态概览", style = MaterialTheme.typography.h4)

        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
            Column(Modifier.padding(16.dp)) {
                Text("运行状态", style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(8.dp))
                when (state) {
                    ScanState.IDLE -> StatusBadge("待机中", MaterialTheme.colors.secondary)
                    ScanState.RUNNING -> StatusBadge("扫描中", MaterialTheme.colors.primary)
                    ScanState.PAUSED -> StatusBadge("已暂停", androidx.compose.ui.graphics.Color(0xFFFFA000))
                    ScanState.ERROR -> StatusBadge("出错", MaterialTheme.colors.error)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
            Column(Modifier.padding(16.dp)) {
                Text("扫描进度", style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(8.dp))
                if (state == ScanState.RUNNING && currentGroup.isNotEmpty()) {
                    Text("当前群: $currentGroup")
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                } else {
                    Text("未在扫描")
                    LinearProgressIndicator(progress = 0f, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("监控群数", groups.size.toString(), Modifier.weight(1f))
            StatCard("今日命中", todayHits.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(color = color.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = color)
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = 4.dp) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.body2)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.h3)
        }
    }
}
