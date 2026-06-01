package com.weichatcheck.engine.uia

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.OleAuto
import com.sun.jna.platform.win32.Variant
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.PointerByReference

/**
 * Wraps IUIAutomationCondition COM interface.
 */
class UIACondition(val pointer: Pointer) {
    fun release() {
        try {
            val vtable = pointer.getPointer(0)
            val releaseFunc = vtable.getPointer(2 * Native.POINTER_SIZE.toLong())
            val func = com.sun.jna.Function.getFunction(releaseFunc)
            func.invokeInt(arrayOf(pointer))
        } catch (_: Exception) {}
    }

    companion object {
        fun createPropertyCondition(propertyId: Int, value: Variant.VARIANT): UIACondition? {
            val automation = UIAContext.automation ?: return null
            return automation.createPropertyCondition(propertyId, value)
        }

        fun createControlTypeCondition(controlType: Int): UIACondition? {
            val variant = Variant.VARIANT(controlType)
            return createPropertyCondition(UIAConstants.UIA_ControlTypePropertyId, variant)
        }

        fun createNameCondition(name: String): UIACondition? {
            val variant = Variant.VARIANT(name)
            return createPropertyCondition(UIAConstants.UIA_NamePropertyId, variant)
        }
    }
}

/**
 * Wraps IUIAutomation COM interface - the main entry point.
 */
class UIAAutomation private constructor(val pointer: Pointer) {

    fun getRootElement(): UIAElement? {
        return try {
            val vtable = pointer.getPointer(0)
            val getRootFunc = vtable.getPointer(5 * Native.POINTER_SIZE.toLong())
            val func = com.sun.jna.Function.getFunction(getRootFunc)
            val elementRef = PointerByReference()
            val hr = func.invokeInt(arrayOf(pointer, elementRef))
            if (COMUtils.SUCCEEDED(hr) && elementRef.value != null) {
                UIAElement(elementRef.value)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun getElementFromHandle(hwnd: com.sun.jna.platform.win32.WinDef.HWND): UIAElement? {
        return try {
            val vtable = pointer.getPointer(0)
            val funcPtr = vtable.getPointer(6 * Native.POINTER_SIZE.toLong())
            val func = com.sun.jna.Function.getFunction(funcPtr)
            val elementRef = PointerByReference()
            val hr = func.invokeInt(arrayOf(pointer, hwnd.pointer, elementRef))
            if (COMUtils.SUCCEEDED(hr) && elementRef.value != null) {
                UIAElement(elementRef.value)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun createTrueCondition(): UIACondition? {
        return try {
            val vtable = pointer.getPointer(0)
            val funcPtr = vtable.getPointer(21 * Native.POINTER_SIZE.toLong())
            val func = com.sun.jna.Function.getFunction(funcPtr)
            val conditionRef = PointerByReference()
            val hr = func.invokeInt(arrayOf(pointer, conditionRef))
            if (COMUtils.SUCCEEDED(hr) && conditionRef.value != null) {
                UIACondition(conditionRef.value)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun createPropertyCondition(propertyId: Int, value: Variant.VARIANT): UIACondition? {
        return try {
            val vtable = pointer.getPointer(0)
            val funcPtr = vtable.getPointer(15 * Native.POINTER_SIZE.toLong())
            val func = com.sun.jna.Function.getFunction(funcPtr)
            val conditionRef = PointerByReference()
            val hr = func.invokeInt(arrayOf(pointer, propertyId, value, conditionRef))
            if (COMUtils.SUCCEEDED(hr) && conditionRef.value != null) {
                UIACondition(conditionRef.value)
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

    companion object {
        fun create(): UIAAutomation? {
            return try {
                val ref = PointerByReference()
                val hr = com.sun.jna.platform.win32.Ole32.INSTANCE.CoCreateInstance(
                    Guid.GUID.fromString(UIAConstants.CLSID_CUIAutomation),
                    null,
                    1,
                    Guid.GUID.fromString(UIAConstants.IID_IUIAutomation),
                    ref
                )
                if (COMUtils.SUCCEEDED(hr) && ref.value != null) {
                    UIAAutomation(ref.value)
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}
