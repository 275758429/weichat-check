package com.weichatcheck.ui

import com.weichatcheck.data.ClueDao
import com.weichatcheck.data.ConfigDao
import com.weichatcheck.engine.*
import com.weichatcheck.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

class AppViewModel(
    private val clueDao: ClueDao,
    private val configDao: ConfigDao,
    private val scanEngine: ScanEngine,
    private val pushEngine: PushEngine
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _groups = MutableStateFlow<List<String>>(emptyList())
    val groups: StateFlow<List<String>> = _groups

    private val _keywords = MutableStateFlow<List<KeywordConfig>>(emptyList())
    val keywords: StateFlow<List<KeywordConfig>> = _keywords

    private val _scanInterval = MutableStateFlow(300)
    val scanInterval: StateFlow<Int> = _scanInterval

    private val _slideDelayMin = MutableStateFlow(800)
    val slideDelayMin: StateFlow<Int> = _slideDelayMin

    private val _slideDelayMax = MutableStateFlow(2500)
    val slideDelayMax: StateFlow<Int> = _slideDelayMax

    private val _pushEnabled = MutableStateFlow(false)
    val pushEnabled: StateFlow<Boolean> = _pushEnabled

    private val _pushUrl = MutableStateFlow("")
    val pushUrl: StateFlow<String> = _pushUrl

    // Forward settings
    private val _forwardTarget = MutableStateFlow("")
    val forwardTarget: StateFlow<String> = _forwardTarget

    // Filter settings
    private val _minMessageLength = MutableStateFlow(0)
    val minMessageLength: StateFlow<Int> = _minMessageLength

    private val _excludeQuoted = MutableStateFlow(false)
    val excludeQuoted: StateFlow<Boolean> = _excludeQuoted

    private val _clues = MutableStateFlow<List<Clue>>(emptyList())
    val clues: StateFlow<List<Clue>> = _clues

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    val scanState: StateFlow<ScanState> = scanEngine.state
    val currentGroup: StateFlow<String> = scanEngine.currentGroup
    val scanProgress: StateFlow<Float> = scanEngine.progress
    val todayHits: StateFlow<Int> = scanEngine.todayHits

    init {
        loadConfig()
        refreshClues()
    }

    fun loadConfig() {
        _groups.value = configDao.getJson("target_groups", emptyList())
        _keywords.value = configDao.getJson("keywords", emptyList())
        _scanInterval.value = configDao.getInt("scan_interval_sec", 300)
        _slideDelayMin.value = configDao.getInt("slide_delay_min_ms", 800)
        _slideDelayMax.value = configDao.getInt("slide_delay_max_ms", 2500)
        _pushEnabled.value = configDao.getBoolean("push_enabled", false)
        _pushUrl.value = configDao.getString("push_url", "")
        _forwardTarget.value = configDao.getString("forward_target", "")
        _minMessageLength.value = configDao.getInt("min_message_length", 0)
        _excludeQuoted.value = configDao.getBoolean("exclude_quoted", false)

        pushEngine.enabled = _pushEnabled.value
        pushEngine.updateEndpoint(_pushUrl.value)

        scanEngine.config = ScanConfig(
            targetGroups = _groups.value,
            keywords = _keywords.value,
            scanIntervalSec = _scanInterval.value.toLong(),
            slideDelayMinMs = _slideDelayMin.value.toLong(),
            slideDelayMaxMs = _slideDelayMax.value.toLong(),
            forwardTarget = _forwardTarget.value,
            minMessageLength = _minMessageLength.value,
            excludeQuotedMessages = _excludeQuoted.value
        )
    }

    fun saveConfig() {
        configDao.setJson("target_groups", _groups.value)
        configDao.setJson("keywords", _keywords.value)
        configDao.set("scan_interval_sec", _scanInterval.value.toString())
        configDao.set("slide_delay_min_ms", _slideDelayMin.value.toString())
        configDao.set("slide_delay_max_ms", _slideDelayMax.value.toString())
        configDao.set("push_enabled", _pushEnabled.value.toString())
        configDao.set("push_url", _pushUrl.value)
        configDao.set("forward_target", _forwardTarget.value)
        configDao.set("min_message_length", _minMessageLength.value.toString())
        configDao.set("exclude_quoted", _excludeQuoted.value.toString())

        scanEngine.config = ScanConfig(
            targetGroups = _groups.value,
            keywords = _keywords.value,
            scanIntervalSec = _scanInterval.value.toLong(),
            slideDelayMinMs = _slideDelayMin.value.toLong(),
            slideDelayMaxMs = _slideDelayMax.value.toLong(),
            forwardTarget = _forwardTarget.value,
            minMessageLength = _minMessageLength.value,
            excludeQuotedMessages = _excludeQuoted.value
        )
        pushEngine.enabled = _pushEnabled.value
        pushEngine.updateEndpoint(_pushUrl.value)
    }

    fun addGroup(name: String) {
        if (name.isBlank()) return
        _groups.value += name
        saveConfig()
    }

    fun removeGroup(name: String) {
        _groups.value -= name
        saveConfig()
    }

    fun addKeyword(config: KeywordConfig) {
        if (config.text.isBlank()) return
        _keywords.value += config
        saveConfig()
    }

    fun removeKeyword(config: KeywordConfig) {
        _keywords.value -= config
        saveConfig()
    }

    fun startScan() {
        if (_groups.value.isEmpty()) {
            showToast("请先添加监控群聊")
            return
        }
        if (_keywords.value.isEmpty()) {
            showToast("请先添加关键词")
            return
        }
        scanEngine.start()
    }

    fun stopScan() {
        scanEngine.stop()
    }

    fun setPushEnabled(enabled: Boolean) {
        _pushEnabled.value = enabled
        saveConfig()
    }

    fun setPushUrl(url: String) {
        _pushUrl.value = url
        saveConfig()
    }

    fun setForwardTarget(target: String) {
        _forwardTarget.value = target.trim()
        saveConfig()
    }

    fun setMinMessageLength(length: Int) {
        _minMessageLength.value = maxOf(0, length)
        saveConfig()
    }

    fun setExcludeQuoted(exclude: Boolean) {
        _excludeQuoted.value = exclude
        saveConfig()
    }

    fun testPush() {
        scope.launch {
            val result = pushEngine.testConnection()
            showToast(result.fold({ "推送测试成功" }, { "推送测试失败: ${it.message}" }))
        }
    }

    fun refreshClues() {
        _clues.value = clueDao.getAll()
    }

    fun getCluesByGroup(group: String): List<Clue> = clueDao.getByGroup(group)
    fun getCluesByKeyword(keyword: String): List<Clue> = clueDao.getByKeyword(keyword)

    fun clearClues() {
        clueDao.clearAll()
        refreshClues()
        showToast("记录已清空")
    }

    fun exportClues(): String {
        val all = clueDao.getAll()
        val sb = StringBuilder("群名称,发送人,发送时间,命中内容,命中关键词,匹配模式,记录时间\n")
        all.forEach { clue ->
            sb.append(""""${clue.groupName}","${clue.senderName}","${clue.sendTime}","${clue.hitContent}","${clue.hitKeyword}","${clue.matchType}","${clue.createdAt}""").append("\n")
        }
        return sb.toString()
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        scope.launch {
            delay(3000)
            _toastMessage.value = null
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun cleanup() {
        scope.cancel()
        scanEngine.cleanup()
    }
}
