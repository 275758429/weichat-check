package com.weichatcheck.engine.uia

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.OleAuto
import com.sun.jna.platform.win32.Variant
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference

/**
 * Represents a UIA automation element.
 * Wraps an IUIAutomationElement COM interface pointer.
 */
class UIAElement internal constructor(val pointer: Pointer) {

    fun getName(): String = getStringProperty(UIAConstants.UIA_NamePropertyId)

    fun getClassName(): String = getStringProperty(UIAConstants.UIA_ClassNamePropertyId)

    fun getAutomationId(): String = getStringProperty(UIAConstants.UIA_AutomationIdPropertyId)

    fun getControlType(): Int = getIntProperty(UIAConstants.UIA_ControlTypePropertyId)

    fun getValue(): String = getStringProperty(UIAConstants.UIA_ValueValuePropertyId)

    fun getBoundingRectangle(): Rect? {
        val result = callMethod(10) // get_CurrentBoundingRectangle
        return if (COMUtils.SUCCEEDED(result)) {
            // Read double[4] from result pointer
            null // Simplified for now
        } else null
    }

    fun findFirst(scope: Int, condition: UIACondition): UIAElement? {
        val elementRef = PointerByReference()
        val vtable = pointer.getPointer(0)
        val findFirstFunc = vtable.getPointer(6 * Native.POINTER_SIZE.toLong()) // IUIAutomationElement::FindFirst
        val func = com.sun.jna.Function.getFunction(findFirstFunc)
        val hr = func.invokeInt(
            arrayOf(pointer, scope, condition.pointer, elementRef)
        )
        return if (COMUtils.SUCCEEDED(hr) && elementRef.value != null) {
            UIAElement(elementRef.value)
        } else null
    }

    fun findAll(scope: Int, condition: UIACondition): List<UIAElement> {
        val result = mutableListOf<UIAElement>()
        val arrayRef = PointerByReference()
        val vtable = pointer.getPointer(0)
        val findAllFunc = vtable.getPointer(7 * Native.POINTER_SIZE.toLong())
        val func = com.sun.jna.Function.getFunction(findAllFunc)
        val hr = func.invokeInt(
            arrayOf(pointer, scope, condition.pointer, arrayRef)
        )
        if (COMUtils.SUCCEEDED(hr) && arrayRef.value != null) {
            val array = UIAElementArray(arrayRef.value)
            val length = array.length
            for (i in 0 until length) {
                array.getElement(i)?.let { result.add(it) }
            }
            array.release()
        }
        return result
    }

    fun getChildren(): List<UIAElement> {
        return UIAContext.automation?.createTrueCondition()?.let { condition ->
            val children = findAll(UIAConstants.TreeScope_Children, condition)
            condition.release()
            children
        } ?: emptyList()
    }

    fun getClickablePoint(): Pair<Int, Int>? {
        val xRef = WinDef.BOOLByReference()
        val yRef = WinDef.BOOLByReference()
        val point = IntByReference()
        // Simplified - would need proper POINT struct
        return null
    }

    fun click() {
        val rect = getBoundingRectangle() ?: return
        val x = rect.x + rect.width / 2
        val y = rect.y + rect.height / 2
        val robot = java.awt.Robot()
        robot.mouseMove(x, y)
        robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK)
        robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK)
    }

    fun sendText(text: String) {
        click()
        Thread.sleep(100)
        val robot = java.awt.Robot()
        for (char in text) {
            val keyCode = char.uppercaseChar().code
            if (keyCode in java.awt.event.KeyEvent.VK_A..java.awt.event.KeyEvent.VK_Z) {
                robot.keyPress(keyCode)
                robot.keyRelease(keyCode)
            }
            robot.delay(30)
        }
    }

    fun release() {
        // Release COM reference
        val vtable = pointer.getPointer(0)
        val releaseFunc = vtable.getPointer(2 * Native.POINTER_SIZE.toLong()) // IUnknown::Release
        val func = com.sun.jna.Function.getFunction(releaseFunc)
        func.invokeInt(arrayOf(pointer))
    }

    private fun getStringProperty(propertyId: Int): String {
        return try {
            val vtable = pointer.getPointer(0)
            val getPropFunc = vtable.getPointer(5 * Native.POINTER_SIZE.toLong()) // get_CurrentPropertyValue
            val func = com.sun.jna.Function.getFunction(getPropFunc)
            val variant = Variant.VARIANT()
            val hr = func.invokeInt(arrayOf(pointer, propertyId, variant))
            if (COMUtils.SUCCEEDED(hr)) {
                val strVal = variant.stringValue()
                OleAuto.INSTANCE.VariantClear(variant)
                strVal ?: ""
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun getIntProperty(propertyId: Int): Int {
        return try {
            val vtable = pointer.getPointer(0)
            val getPropFunc = vtable.getPointer(5 * Native.POINTER_SIZE.toLong())
            val func = com.sun.jna.Function.getFunction(getPropFunc)
            val variant = Variant.VARIANT()
            val hr = func.invokeInt(arrayOf(pointer, propertyId, variant))
            if (COMUtils.SUCCEEDED(hr)) {
                val intVal = variant.intValue()
                OleAuto.INSTANCE.VariantClear(variant)
                intVal
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    private fun callMethod(index: Int): Int {
        return try {
            val vtable = pointer.getPointer(0)
            val funcPtr = vtable.getPointer(index * Native.POINTER_SIZE.toLong())
            val func = com.sun.jna.Function.getFunction(funcPtr)
            func.invokeInt(arrayOf(pointer))
        } catch (e: Exception) {
            -1
        }
    }

    data class Rect(val x: Int, val y: Int, val width: Int, val height: Int)

    companion object {
        fun fromNative(pointer: Pointer?): UIAElement? {
            return pointer?.let { UIAElement(it) }
        }
    }
}
