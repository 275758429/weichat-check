package com.weichatcheck.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class KeywordConfigTest {
    private val jsonWithDefaults = Json { encodeDefaults = true }

    @Test
    fun `serializes contains keyword`() {
        val config = KeywordConfig(text = "优惠", type = MatchType.CONTAINS)
        val json = jsonWithDefaults.encodeToString(config)
        assertEquals("""{"text":"优惠","type":"contains","tolerance":1}""", json)
    }

    @Test
    fun `serializes fuzzy keyword with tolerance`() {
        val config = KeywordConfig(text = "免费", type = MatchType.FUZZY, tolerance = 1)
        val json = jsonWithDefaults.encodeToString(config)
        assertEquals("""{"text":"免费","type":"fuzzy","tolerance":1}""", json)
    }

    @Test
    fun `deserializes keyword list`() {
        val json = """[{"text":"优惠","type":"contains"},{"text":"\\d+","type":"regex"}]"""
        val list = Json.decodeFromString<List<KeywordConfig>>(json)
        assertEquals(2, list.size)
        assertEquals(MatchType.CONTAINS, list[0].type)
        assertEquals(MatchType.REGEX, list[1].type)
    }
}
