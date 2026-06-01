package com.weichatcheck.engine.uia

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference

/**
 * Wraps IUIAutomationElementArray COM interface.
 */
class UIAElementArray(val pointer: Pointer) {

    val length: Int
        get() = try {
            val vtable = pointer.getPointer(0)
            val lengthFunc = vtable.getPointer(3 * Native.POINTER_SIZE.toLong())
            val func = com.sun.jna.Function.getFunction(lengthFunc)
            val intRef = IntByReference()
            val hr = func.invokeInt(arrayOf(pointer, intRef))
            if (COMUtils.SUCCEEDED(hr)) intRef.value else 0
        } catch (e: Exception) {
            0
        }

    fun getElement(index: Int): UIAElement? {
        return try {
            val vtable = pointer.getPointer(0)
            val getElementFunc = vtable.getPointer(4 * Native.POINTER_SIZE.toLong())
            val func = com.sun.jna.Function.getFunction(getElementFunc)
            val elementRef = PointerByReference()
            val hr = func.invokeInt(arrayOf(pointer, index, elementRef))
            if (COMUtils.SUCCEEDED(hr) && elementRef.value != null) {
                UIAElement(elementRef.value)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun release() {
        try {
            val vtable = pointer.getPointer(0)
            val releaseFunc = vtable.getPointer(2 * Native.POINTER_SIZE.toLong())
            val func = com.sun.jna.Function.getFunction(releaseFunc)
            func.invokeInt(arrayOf(pointer))
        } catch (_: Exception) {}
    }
}
