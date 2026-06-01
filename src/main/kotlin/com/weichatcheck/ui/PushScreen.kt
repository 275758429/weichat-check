package com.weichatcheck.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PushScreen(viewModel: AppViewModel) {
    val enabled by viewModel.pushEnabled.collectAsState()
    val url by viewModel.pushUrl.collectAsState()
    var urlInput by remember { mutableStateOf(url) }

    LaunchedEffect(url) { urlInput = url }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("推送设置", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = enabled,
                        onCheckedChange = { viewModel.setPushEnabled(it) }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(if (enabled) "推送已启用" else "推送已禁用")
                }

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("推送 URL") },
                    placeholder = { Text("https://example.com/api/clues") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled
                )

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.setPushUrl(urlInput.trim()) },
                        enabled = enabled && urlInput.isNotBlank()
                    ) {
                        Text("保存")
                    }
                    Button(
                        onClick = { viewModel.testPush() },
                        enabled = enabled && urlInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary)
                    ) {
                        Text("测试推送")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("推送格式", style = MaterialTheme.typography.h6)
        Spacer(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            Text(
                """POST <你的URL>
Content-Type: application/json

{
  "groupName": "群名称",
  "senderName": "发送人",
  "sendTime": "10:30",
  "hitContent": "命中内容",
  "hitKeyword": "优惠",
  "matchType": "contains",
  "createdAt": 1717123456789
}""",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.caption
            )
        }
    }
}
