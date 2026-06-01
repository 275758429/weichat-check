package com.weichatcheck.engine

import com.weichatcheck.model.KeywordConfig

data class ScanConfig(
    val targetGroups: List<String> = emptyList(),
    val keywords: List<KeywordConfig> = emptyList(),
    val scanIntervalSec: Long = 300,
    val slideDelayMinMs: Long = 800,
    val slideDelayMaxMs: Long = 2500,
    val scrollCount: Int = 5,
    val minMessageLength: Int = 0,
    val excludeQuotedMessages: Boolean = false,
    val forwardTarget: String = ""
)
