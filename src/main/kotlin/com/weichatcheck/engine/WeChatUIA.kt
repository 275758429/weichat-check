package com.weichatcheck.engine

import com.sun.jna.platform.win32.Ole32
import com.sun.jna.platform.win32.WinDef
import com.weichatcheck.engine.uia.*
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

class WeChatUIA(
    private val windowWatcher: WindowWatcher,
    private val ocrFallback: OCRFallback? = null
) {
    private val isWindows = System.getProperty("os.name").lowercase().contains("win")

    init {
        if (isWindows) {
            try {
                Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_APARTMENTTHREADED)
            } catch (_: Exception) {
                // Already initialized or unavailable
            }
        }
    }

    fun openWeChat(): Boolean {
        if (!isWindows) return false
        if (windowWatcher.isWeChatRunning()) return true

        val paths = listOf(
            "C:\\Program Files (x86)\\Tencent\\WeChat\\WeChat.exe",
            "C:\\Program Files\\Tencent\\WeChat\\WeChat.exe"
        )
        for (path in paths) {
            try {
                Runtime.getRuntime().exec(path)
                Thread.sleep(5000)
                if (windowWatcher.isWeChatRunning()) return true
            } catch (_: Exception) {
                continue
            }
        }
        return false
    }

    fun openChat(groupName: String): Boolean {
        if (!isWindows) return false
        val hwnd = windowWatcher.findWeChatWindow() ?: return false
        bringWindowToFront(hwnd)
        Thread.sleep(500)

        // Strategy 1: Try to find search box via UIA and type
        val automation = UIAContext.automation ?: return fallbackOpenChat(hwnd, groupName)
        val rootElement = automation.getElementFromHandle(hwnd) ?: return fallbackOpenChat(hwnd, groupName)

        // Find the search/edit control
        val searchCondition = UIACondition.createControlTypeCondition(UIAConstants.UIA_EditControlTypeId)
            ?: return fallbackOpenChat(hwnd, groupName)

        val searchBox = rootElement.findFirst(UIAConstants.TreeScope_Descendants, searchCondition)
        searchCondition.release()

        if (searchBox != null) {
            searchBox.click()
            Thread.sleep(200)
            searchBox.sendText(groupName)
            Thread.sleep(800)
            pressKey(KeyEvent.VK_ENTER)
            Thread.sleep(1000)
            searchBox.release()
            rootElement.release()
            return isInChat()
        }

        rootElement.release()
        return fallbackOpenChat(hwnd, groupName)
    }

    private fun fallbackOpenChat(hwnd: WinDef.HWND, groupName: String): Boolean {
        // Fallback: use fixed coordinates (known to work for standard WeChat layout)
        val rect = windowWatcher.getWindowRect(hwnd) ?: return false
        click(rect.x + 80, rect.y + 40)
        Thread.sleep(300)
        typeText(groupName)
        Thread.sleep(800)
        pressKey(KeyEvent.VK_ENTER)
        Thread.sleep(1000)
        return isInChat()
    }

    fun isInChat(): Boolean {
        if (!isWindows) return false

        val automation = UIAContext.automation ?: return false
        val hwnd = windowWatcher.findWeChatWindow() ?: return false
        val root = automation.getElementFromHandle(hwnd) ?: return false

        // Look for an edit control (message input area)
        val condition = UIACondition.createControlTypeCondition(UIAConstants.UIA_EditControlTypeId)
        if (condition == null) {
            root.release()
            return false
        }

        val edit = root.findFirst(UIAConstants.TreeScope_Descendants, condition)
        condition.release()
        root.release()

        val found = edit != null
        edit?.release()
        return found
    }

    fun scrollUp(): Boolean {
        if (!isWindows) return false
        val hwnd = windowWatcher.findWeChatWindow() ?: return false
        val rect = windowWatcher.getWindowRect(hwnd) ?: return false

        val centerX = rect.x + rect.width / 2
        val centerY = rect.y + rect.height / 2
        val robot = Robot()
        robot.mouseMove(centerX, centerY)
        robot.mouseWheel(5)
        return true
    }

    data class Message(
        val sender: String,
        val time: String,
        val content: String
    )

    fun extractMessages(): List<Message> {
        if (!isWindows) return emptyList()

        val messages = mutableListOf<Message>()
        val automation = UIAContext.automation
        val hwnd = windowWatcher.findWeChatWindow()

        if (automation == null || hwnd == null) {
            return ocrFallbackExtract()
        }

        val root = automation.getElementFromHandle(hwnd)
        if (root == null) {
            return ocrFallbackExtract()
        }

        try {
            // WeChat chat list is typically a List or a set of Custom/Text controls
            // Try multiple strategies

            // Strategy 1: Find List control
            val listCondition = UIACondition.createControlTypeCondition(UIAConstants.UIA_ListControlTypeId)
            val listElement = if (listCondition != null) {
                val el = root.findFirst(UIAConstants.TreeScope_Descendants, listCondition)
                listCondition.release()
                el
            } else null

            if (listElement != null) {
                val children = listElement.getChildren()
                for (child in children) {
                    val text = extractMessageFromElement(child)
                    if (text.content.isNotBlank()) {
                        messages.add(text)
                    }
                }
                listElement.release()
            }

            // Strategy 2: If no List found, find all Text controls that might be messages
            if (messages.isEmpty()) {
                val textCondition = UIACondition.createControlTypeCondition(UIAConstants.UIA_TextControlTypeId)
                if (textCondition != null) {
                    val textElements = root.findAll(UIAConstants.TreeScope_Descendants, textCondition)
                    textCondition.release()

                    for (el in textElements) {
                        val name = el.getName()
                        if (name.isNotBlank() && name.length > 2) {
                            messages.add(parseMessageText(name))
                        }
                        el.release()
                    }
                }
            }

            root.release()
        } catch (_: Exception) {
            root.release()
        }

        if (messages.isEmpty()) {
            return ocrFallbackExtract()
        }

        return messages
    }

    private fun extractMessageFromElement(element: UIAElement): Message {
        val name = element.getName()
        val automationId = element.getAutomationId()

        // WeChat message elements often have structured names or child elements
        // Try to parse from the element name first
        if (name.isNotBlank()) {
            return parseMessageText(name)
        }

        // If name is empty, try to get text from child elements
        val children = element.getChildren()
        val texts = children.map { it.getName() }.filter { it.isNotBlank() }
        children.forEach { it.release() }

        return when {
            texts.size >= 3 -> Message(texts[0], texts[1], texts.drop(2).joinToString("\n"))
            texts.size == 2 -> Message(texts[0], "", texts[1])
            texts.size == 1 -> Message("", "", texts[0])
            else -> Message("", "", "")
        }
    }

    private fun ocrFallbackExtract(): List<Message> {
        if (ocrFallback == null) return emptyList()
        val hwnd = windowWatcher.findWeChatWindow() ?: return emptyList()
        val rect = windowWatcher.getWindowRect(hwnd) ?: return emptyList()
        val ocrText = ocrFallback.recognize(rect.x, rect.y + 80, rect.width, rect.height - 150)
        return parseOCRText(ocrText)
    }

    private fun parseMessageText(text: String): Message {
        val lines = text.split("\n")
        return when {
            lines.size >= 3 -> Message(lines[0], lines[1], lines.drop(2).joinToString("\n"))
            lines.size == 2 -> Message(lines[0], "", lines[1])
            else -> Message("", "", text)
        }
    }

    private fun parseOCRText(text: String): List<Message> {
        val messages = mutableListOf<Message>()
        val lines = text.split("\n")
        var i = 0
        while (i < lines.size) {
            val sender = lines.getOrNull(i)?.trim() ?: ""
            val time = lines.getOrNull(i + 1)?.trim() ?: ""
            val content = lines.getOrNull(i + 2)?.trim() ?: ""
            if (sender.isNotBlank() && content.isNotBlank()) {
                messages.add(Message(sender, time, content))
                i += 3
            } else {
                i++
            }
        }
        return messages
    }

    private fun bringWindowToFront(hwnd: WinDef.HWND) {
        val user32 = com.sun.jna.platform.win32.User32.INSTANCE
        user32.SetForegroundWindow(hwnd)
        user32.ShowWindow(hwnd, com.sun.jna.platform.win32.WinUser.SW_RESTORE)
    }

    private fun click(x: Int, y: Int) {
        val robot = Robot()
        robot.mouseMove(x, y)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    }

    private fun typeText(text: String) {
        val robot = Robot()
        for (char in text) {
            val keyCode = char.uppercaseChar().code
            if (keyCode in KeyEvent.VK_A..KeyEvent.VK_Z) {
                if (char.isUpperCase()) {
                    robot.keyPress(KeyEvent.VK_SHIFT)
                }
                robot.keyPress(keyCode)
                robot.keyRelease(keyCode)
                if (char.isUpperCase()) {
                    robot.keyRelease(KeyEvent.VK_SHIFT)
                }
            }
            robot.delay(30)
        }
    }

    private fun pressKey(keyCode: Int) {
        val robot = Robot()
        robot.keyPress(keyCode)
        robot.keyRelease(keyCode)
    }

    /**
     * Send a message to a specific WeChat contact/group via UIA.
     * Opens the chat, types the message, and sends it.
     */
    fun sendMessageTo(contactName: String, message: String): Boolean {
        if (!isWindows || contactName.isBlank() || message.isBlank()) return false

        // Remember current chat so we can return after forwarding
        val wasInChat = isInChat()

        // Open target contact chat
        if (!openChat(contactName)) return false

        // Find input box and send message
        val automation = UIAContext.automation ?: return false
        val hwnd = windowWatcher.findWeChatWindow() ?: return false
        val root = automation.getElementFromHandle(hwnd) ?: return false

        val editCondition = UIACondition.createControlTypeCondition(UIAConstants.UIA_EditControlTypeId)
        if (editCondition == null) {
            root.release()
            return false
        }

        val inputBox = root.findFirst(UIAConstants.TreeScope_Descendants, editCondition)
        editCondition.release()
        root.release()

        if (inputBox == null) return false

        try {
            inputBox.click()
            Thread.sleep(200)

            // Type the message using clipboard for better reliability with Chinese text
            val clipboard = java.awt.datatransfer.StringSelection(message)
            java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(clipboard, null)

            val robot = Robot()
            robot.keyPress(KeyEvent.VK_CONTROL)
            robot.keyPress(KeyEvent.VK_V)
            robot.keyRelease(KeyEvent.VK_V)
            robot.keyRelease(KeyEvent.VK_CONTROL)
            Thread.sleep(200)

            pressKey(KeyEvent.VK_ENTER)
            Thread.sleep(500)

            inputBox.release()

            // Return to original chat if we were in one
            if (wasInChat) {
                // Navigate back logic would go here
            }

            return true
        } catch (_: Exception) {
            inputBox.release()
            return false
        }
    }
}
