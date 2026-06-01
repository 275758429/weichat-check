package com.weichatcheck.engine

import kotlin.test.Test
import kotlin.test.assertTrue

class RandomDelayerTest {
    @Test
    fun `sleep returns within configured range`() {
        val delayer = RandomDelayer(minMs = 100, maxMs = 200)
        val start = System.currentTimeMillis()
        delayer.sleep()
        val elapsed = System.currentTimeMillis() - start
        assertTrue(elapsed >= 100, "Expected >= 100ms, got $elapsed")
        assertTrue(elapsed <= 300, "Expected <= 300ms, got $elapsed")
    }
}
