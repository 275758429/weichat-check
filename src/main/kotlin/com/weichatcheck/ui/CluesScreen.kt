package com.weichatcheck.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CluesScreen(viewModel: AppViewModel) {
    val clues by viewModel.clues.collectAsState()
    var filterGroup by remember { mutableStateOf("") }
    var filterKeyword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.refreshClues()
    }

    val filtered = clues.filter { clue ->
        (filterGroup.isBlank() || clue.groupName.contains(filterGroup)) &&
        (filterKeyword.isBlank() || clue.hitKeyword.contains(filterKeyword))
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("检索记录", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = filterGroup,
                onValueChange = { filterGroup = it },
                label = { Text("筛选群名") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = filterKeyword,
                onValueChange = { filterKeyword = it },
                label = { Text("筛选关键词") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { viewModel.refreshClues() }) {
                Text("刷新")
            }
            Button(
                onClick = {
                    val csv = viewModel.exportClues()
                    val file = File("clues_export_${System.currentTimeMillis()}.csv")
                    file.writeText(csv)
                    viewModel.showToast("已导出: ${file.name}")
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
            ) {
                Icon(Icons.Default.Share, contentDescription = "导出")
                Spacer(Modifier.width(4.dp))
                Text("导出")
            }
            Button(
                onClick = { viewModel.clearClues() },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.error)
            ) {
                Icon(Icons.Default.Clear, contentDescription = "清空")
                Spacer(Modifier.width(4.dp))
                Text("清空")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("共 ${filtered.size} 条记录", style = MaterialTheme.typography.body2)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(filtered) { clue ->
                Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(clue.groupName, style = MaterialTheme.typography.subtitle2, color = MaterialTheme.colors.primary)
                            Text(
                                Instant.ofEpochMilli(clue.createdAt).atZone(ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                                style = MaterialTheme.typography.caption
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("${clue.senderName} · ${clue.sendTime}", style = MaterialTheme.typography.caption)
                        Spacer(Modifier.height(4.dp))
                        Text(clue.hitContent, style = MaterialTheme.typography.body1)
                        Spacer(Modifier.height(4.dp))
                        Text("命中: ${clue.hitKeyword} (${clue.matchType})", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.secondary)
                    }
                }
            }
        }
    }
}
