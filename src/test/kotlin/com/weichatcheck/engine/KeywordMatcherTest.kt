package com.weichatcheck.engine

import com.weichatcheck.model.KeywordConfig
import com.weichatcheck.model.MatchType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KeywordMatcherTest {
    private val matcher = KeywordMatcher()

    @Test
    fun `contains match finds keyword in text`() {
        val configs = listOf(KeywordConfig("优惠", MatchType.CONTAINS))
        val result = matcher.match("今日有优惠活动", configs)
        assertEquals("优惠", result?.keyword)
        assertEquals(MatchType.CONTAINS, result?.matchType)
    }

    @Test
    fun `contains match returns null when not found`() {
        val configs = listOf(KeywordConfig("优惠", MatchType.CONTAINS))
        assertNull(matcher.match("今天天气不错", configs))
    }

    @Test
    fun `regex match finds pattern`() {
        val configs = listOf(KeywordConfig("""\d{4}.*活动""", MatchType.REGEX))
        val result = matcher.match("2024年大活动开始啦", configs)
        assertEquals("""\d{4}.*活动""", result?.keyword)
    }

    @Test
    fun `fuzzy match with tolerance 1 matches similar word`() {
        val configs = listOf(KeywordConfig("免费", MatchType.FUZZY, tolerance = 1))
        val result = matcher.match("这个兔费领取", configs)
        assertEquals("免费", result?.keyword)
    }

    @Test
    fun `fuzzy match with tolerance 1 does not match distant word`() {
        val configs = listOf(KeywordConfig("免费", MatchType.FUZZY, tolerance = 1))
        assertNull(matcher.match("这个 completely different", configs))
    }

    @Test
    fun `checks configs in order and returns first match`() {
        val configs = listOf(
            KeywordConfig("优惠", MatchType.CONTAINS),
            KeywordConfig("免费", MatchType.CONTAINS)
        )
        val result = matcher.match("有免费活动", configs)
        assertEquals("免费", result?.keyword)
    }
}
