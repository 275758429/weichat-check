package com.weichatcheck.engine.uia

/**
 * Global context holding the UIA automation instance.
 * Initialized once per application lifetime.
 */
object UIAContext {
    private var _automation: UIAAutomation? = null
    private val isWindows = System.getProperty("os.name").lowercase().contains("win")

    val automation: UIAAutomation?
        get() {
            if (_automation == null && isWindows) {
                _automation = UIAAutomation.create()
            }
            return _automation
        }

    fun shutdown() {
        _automation?.release()
        _automation = null
    }
}
