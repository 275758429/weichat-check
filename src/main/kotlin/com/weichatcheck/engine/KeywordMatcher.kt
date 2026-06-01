package com.weichatcheck.engine

import com.weichatcheck.model.KeywordConfig
import com.weichatcheck.model.MatchType
import kotlin.math.min

data class MatchResult(
    val keyword: String,
    val matchType: MatchType
)

class KeywordMatcher {
    fun match(text: String, configs: List<KeywordConfig>): MatchResult? {
        for (config in configs) {
            when (config.type) {
                MatchType.CONTAINS -> {
                    if (text.contains(config.text)) {
                        return MatchResult(config.text, config.type)
                    }
                }
                MatchType.REGEX -> {
                    val regex = Regex(config.text)
                    if (regex.containsMatchIn(text)) {
                        return MatchResult(config.text, config.type)
                    }
                }
                MatchType.FUZZY -> {
                    if (containsFuzzy(text, config.text, config.tolerance)) {
                        return MatchResult(config.text, config.type)
                    }
                }
            }
        }
        return null
    }

    private fun containsFuzzy(text: String, keyword: String, tolerance: Int): Boolean {
        // For languages without word boundaries (e.g., Chinese), use sliding window
        if (keyword.length > text.length) return false
        for (i in 0..text.length - keyword.length) {
            val window = text.substring(i, i + keyword.length)
            if (levenshteinDistance(window, keyword) <= tolerance) return true
        }
        // Also check space-separated words for languages with word boundaries
        val words = text.split(Regex("""\s+"""))
        return words.any { word ->
            levenshteinDistance(word, keyword) <= tolerance
        }
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        if (s1 == s2) return 0
        if (s1.isEmpty()) return s2.length
        if (s2.isEmpty()) return s1.length

        val prev = IntArray(s2.length + 1)
        val curr = IntArray(s2.length + 1)

        for (j in 0..s2.length) prev[j] = j

        for (i in 1..s1.length) {
            curr[0] = i
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                curr[j] = min(
                    min(curr[j - 1] + 1, prev[j] + 1),
                    prev[j - 1] + cost
                )
            }
            for (j in 0..s2.length) prev[j] = curr[j]
        }
        return curr[s2.length]
    }
}
