package com.weichatcheck

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.weichatcheck.data.*
import com.weichatcheck.engine.*
import com.weichatcheck.ui.App
import com.weichatcheck.ui.AppViewModel

fun main() = application {
    val database = Database()
    val clueDao = ClueDao(database)
    val configDao = ConfigDao(database)
    val pushEngine = PushEngine()
    val keywordMatcher = KeywordMatcher()
    val delayer = RandomDelayer()
    val windowWatcher = WindowWatcher()
    val ocrFallback = OCRFallback()
    val weChatUIA = WeChatUIA(windowWatcher, ocrFallback)
    val scanEngine = ScanEngine(weChatUIA, keywordMatcher, clueDao, pushEngine, delayer)
    val viewModel = AppViewModel(clueDao, configDao, scanEngine, pushEngine)

    Window(
        onCloseRequest = {
            viewModel.cleanup()
            exitApplication()
        },
        title = "WeChat Monitor",
        state = rememberWindowState(width = 900.dp, height = 700.dp)
    ) {
        App(viewModel)
    }
}
