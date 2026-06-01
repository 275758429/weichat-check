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
import com.weichatcheck.model.KeywordConfig
import com.weichatcheck.model.MatchType

@Composable
fun KeywordsScreen(viewModel: AppViewModel) {
    val keywords by viewModel.keywords.collectAsState()
    var newText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MatchType.CONTAINS) }
    var tolerance by remember { mutableStateOf("1") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("关键词管理", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                label = { Text("关键词") },
                modifier = Modifier.weight(1f)
            )
            var expanded by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { expanded = true }) {
                    Text(when (selectedType) {
                        MatchType.CONTAINS -> "包含"
                        MatchType.REGEX -> "正则"
                        MatchType.FUZZY -> "模糊"
                    })
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    MatchType.entries.forEach { type ->
                        DropdownMenuItem(onClick = { selectedType = type; expanded = false }) {
                            Text(when (type) {
                                MatchType.CONTAINS -> "包含匹配"
                                MatchType.REGEX -> "正则匹配"
                                MatchType.FUZZY -> "模糊匹配"
                            })
                        }
                    }
                }
            }
            if (selectedType == MatchType.FUZZY) {
                OutlinedTextField(
                    value = tolerance,
                    onValueChange = { tolerance = it.filter { c -> c.isDigit() } },
                    label = { Text("容错") },
                    modifier = Modifier.width(80.dp)
                )
            }
            Button(
                onClick = {
                    viewModel.addKeyword(KeywordConfig(
                        text = newText.trim(),
                        type = selectedType,
                        tolerance = tolerance.toIntOrNull() ?: 1
                    ))
                    newText = ""
                },
                enabled = newText.isNotBlank()
            ) {
                Text("添加")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("已添加关键词 (${keywords.size})", style = MaterialTheme.typography.h6)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(keywords) { kw ->
                Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(kw.text, style = MaterialTheme.typography.body1)
                            Text(
                                when (kw.type) {
                                    MatchType.CONTAINS -> "包含匹配"
                                    MatchType.REGEX -> "正则匹配"
                                    MatchType.FUZZY -> "模糊匹配 (容错=${kw.tolerance})"
                                },
                                style = MaterialTheme.typography.caption
                            )
                        }
                        IconButton(onClick = { viewModel.removeKeyword(kw) }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                }
            }
        }
    }
}
