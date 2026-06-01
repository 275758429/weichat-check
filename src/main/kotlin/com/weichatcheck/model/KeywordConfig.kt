package com.weichatcheck.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MatchType {
    @SerialName("contains") CONTAINS,
    @SerialName("regex") REGEX,
    @SerialName("fuzzy") FUZZY
}

@Serializable
data class KeywordConfig(
    val text: String,
    val type: MatchType,
    val tolerance: Int = 1
)
