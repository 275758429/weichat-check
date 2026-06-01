package com.weichatcheck.engine

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND

class WindowWatcher {
    private val user32 = if (isWindows()) User32.INSTANCE else null

    fun findWeChatWindow(): HWND? {
        if (user32 == null) return null
        return user32.FindWindow(null, "微信") ?: user32.FindWindow(null, "WeChat")
    }

    fun isWeChatRunning(): Boolean = findWeChatWindow() != null

    fun isWindowResponsive(hwnd: HWND): Boolean {
        if (user32 == null) return false
        return user32.IsWindow(hwnd)
    }

    fun getWindowRect(hwnd: HWND): Rect? {
        if (user32 == null) return null
        val rect = com.sun.jna.platform.win32.WinDef.RECT()
        return if (user32.GetWindowRect(hwnd, rect)) {
            Rect(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top)
        } else null
    }

    data class Rect(val x: Int, val y: Int, val width: Int, val height: Int)

    private fun isWindows(): Boolean {
        return System.getProperty("os.name").lowercase().contains("win")
    }
}
