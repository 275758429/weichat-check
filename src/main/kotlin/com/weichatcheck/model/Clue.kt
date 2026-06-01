package com.weichatcheck.model

data class Clue(
    val id: Long = 0,
    val groupName: String,
    val senderName: String,
    val sendTime: String,
    val hitContent: String,
    val hitKeyword: String,
    val matchType: MatchType,
    val createdAt: Long = System.currentTimeMillis(),
    val pushed: Boolean = false
)
