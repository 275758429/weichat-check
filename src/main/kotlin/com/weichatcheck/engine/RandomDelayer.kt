package com.weichatcheck.engine

import kotlin.random.Random

class RandomDelayer(
    var minMs: Long = 800,
    var maxMs: Long = 2500
) {
    private val random = Random.Default

    fun sleep() {
        val delay = random.nextLong(minMs, maxMs + 1)
        Thread.sleep(delay)
    }

    suspend fun delay() {
        val d = random.nextLong(minMs, maxMs + 1)
        kotlinx.coroutines.delay(d)
    }
}
