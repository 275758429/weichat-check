package com.weichatcheck.engine

import com.weichatcheck.data.ClueDao
import com.weichatcheck.model.Clue
import com.weichatcheck.model.ScanState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScanEngine(
    private val weChatUIA: WeChatUIA,
    private val keywordMatcher: KeywordMatcher,
    private val clueDao: ClueDao,
    private val pushEngine: PushEngine,
    private val delayer: RandomDelayer
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var scanJob: Job? = null

    private val _state = MutableStateFlow(ScanState.IDLE)
    val state: StateFlow<ScanState> = _state

    private val _currentGroup = MutableStateFlow("")
    val currentGroup: StateFlow<String> = _currentGroup

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _todayHits = MutableStateFlow(0)
    val todayHits: StateFlow<Int> = _todayHits

    var config: ScanConfig = ScanConfig()
        set(value) {
            field = value
            delayer.minMs = value.slideDelayMinMs
            delayer.maxMs = value.slideDelayMaxMs
        }

    fun start() {
        if (scanJob?.isActive == true) return
        _state.value = ScanState.RUNNING
        scanJob = scope.launch {
            runScanLoop()
        }
    }

    fun stop() {
        scanJob?.cancel()
        scanJob = null
        _state.value = ScanState.IDLE
        _currentGroup.value = ""
        _progress.value = 0f
    }

    fun pause() {
        scanJob?.cancel()
        scanJob = null
        _state.value = ScanState.PAUSED
    }

    private suspend fun runScanLoop() {
        var consecutiveErrors = 0

        while (isActive && _state.value == ScanState.RUNNING) {
            try {
                val groups = config.targetGroups
                if (groups.isEmpty() || config.keywords.isEmpty()) {
                    delay(config.scanIntervalSec * 1000)
                    continue
                }

                if (!weChatUIA.openWeChat()) {
                    consecutiveErrors++
                    if (consecutiveErrors >= 3) {
                        _state.value = ScanState.ERROR
                        break
                    }
                    delay(5000)
                    continue
                }
                consecutiveErrors = 0

                groups.forEachIndexed { index, groupName ->
                    if (!isActive) return@forEachIndexed
                    _currentGroup.value = groupName
                    _progress.value = index.toFloat() / groups.size

                    scanGroup(groupName)
                }

                _progress.value = 1f
                _currentGroup.value = ""

                delay(config.scanIntervalSec * 1000)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                consecutiveErrors++
                if (consecutiveErrors >= 3) {
                    _state.value = ScanState.ERROR
                    break
                }
                delay(5000)
            }
        }
    }

    private suspend fun scanGroup(groupName: String) {
        try {
            if (!weChatUIA.openChat(groupName)) {
                return
            }

            val seenMessages = mutableSetOf<String>()

            repeat(config.scrollCount) {
                if (!isActive) return@repeat

                val messages = weChatUIA.extractMessages()
                if (messages.isEmpty()) return@repeat

                var newFound = false
                for (msg in messages) {
                    val key = "${msg.sender}:${msg.time}:${msg.content}"
                    if (key in seenMessages) continue
                    seenMessages.add(key)
                    newFound = true

                    // Apply message filters
                    if (shouldFilterMessage(msg.content)) continue

                    val match = keywordMatcher.match(msg.content, config.keywords)
                    if (match != null) {
                        val clue = Clue(
                            groupName = groupName,
                            senderName = msg.sender,
                            sendTime = msg.time,
                            hitContent = msg.content,
                            hitKeyword = match.keyword,
                            matchType = match.matchType
                        )
                        clueDao.insert(clue)
                        _todayHits.value++

                        if (pushEngine.enabled) {
                            pushEngine.push(clue)
                        }

                        // Forward to WeChat contact if configured
                        if (config.forwardTarget.isNotBlank()) {
                            weChatUIA.sendMessageTo(
                                config.forwardTarget,
                                formatForwardMessage(clue, groupName)
                            )
                        }
                    }
                }

                if (!newFound) return@repeat

                weChatUIA.scrollUp()
                delayer.delay()
            }
        } catch (_: Exception) {
        }
    }

    private fun shouldFilterMessage(text: String): Boolean {
        // Filter by minimum length
        if (config.minMessageLength > 0 && text.length < config.minMessageLength) {
            return true
        }
        // Filter quoted messages (WeChat quote format)
        if (config.excludeQuotedMessages) {
            val quotePattern = Regex("""^「.*」\n- - - - - - - - - - - - - -""")
            if (quotePattern.containsMatchIn(text)) {
                return true
            }
        }
        return false
    }

    private fun formatForwardMessage(clue: Clue, sourceGroup: String): String {
        return buildString {
            appendLine("检测到关键字: ${clue.hitKeyword}")
            appendLine("来源群: $sourceGroup")
            appendLine("发送者: ${clue.senderName}")
            appendLine("发送时间: ${clue.sendTime}")
            appendLine("消息内容:")
            append(clue.hitContent)
        }
    }

    private val isActive: Boolean
        get() = scanJob?.isActive == true

    fun cleanup() {
        scope.cancel()
    }
}
