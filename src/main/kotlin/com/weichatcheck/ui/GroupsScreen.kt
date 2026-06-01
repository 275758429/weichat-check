package com.weichatcheck.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GroupsScreen(viewModel: AppViewModel) {
    val groups by viewModel.groups.collectAsState()
    var newGroup by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("群聊管理", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newGroup,
                onValueChange = { newGroup = it },
                label = { Text("群名称") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    viewModel.addGroup(newGroup.trim())
                    newGroup = ""
                },
                enabled = newGroup.isNotBlank()
            ) {
                Text("添加")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("已添加群 (${groups.size})", style = MaterialTheme.typography.h6)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(groups) { group ->
                Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(group)
                        IconButton(onClick = { viewModel.removeGroup(group) }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                }
            }
        }
    }
}
