package com.weichatcheck.engine.uia

/**
 * Windows UI Automation constants.
 * Reference: https://docs.microsoft.com/en-us/windows/win32/winauto/uiauto-automation-element-propids
 */
object UIAConstants {
    // Control Types
    const val UIA_ButtonControlTypeId = 50000
    const val UIA_CalendarControlTypeId = 50001
    const val UIA_CheckBoxControlTypeId = 50002
    const val UIA_ComboBoxControlTypeId = 50003
    const val UIA_EditControlTypeId = 50004
    const val UIA_HyperlinkControlTypeId = 50005
    const val UIA_ImageControlTypeId = 50006
    const val UIA_ListItemControlTypeId = 50007
    const val UIA_ListControlTypeId = 50008
    const val UIA_MenuControlTypeId = 50009
    const val UIA_MenuBarControlTypeId = 50010
    const val UIA_MenuItemControlTypeId = 50011
    const val UIA_ProgressBarControlTypeId = 50012
    const val UIA_RadioButtonControlTypeId = 50013
    const val UIA_ScrollBarControlTypeId = 50014
    const val UIA_SliderControlTypeId = 50015
    const val UIA_SpinnerControlTypeId = 50016
    const val UIA_StatusBarControlTypeId = 50017
    const val UIA_TabControlTypeId = 50018
    const val UIA_TabItemControlTypeId = 50019
    const val UIA_TextControlTypeId = 50020
    const val UIA_ToolBarControlTypeId = 50021
    const val UIA_ToolTipControlTypeId = 50022
    const val UIA_TreeControlTypeId = 50023
    const val UIA_TreeItemControlTypeId = 50024
    const val UIA_CustomControlTypeId = 50025
    const val UIA_GroupControlTypeId = 50026
    const val UIA_ThumbControlTypeId = 50027
    const val UIA_DataGridControlTypeId = 50028
    const val UIA_DataItemControlTypeId = 50029
    const val UIA_DocumentControlTypeId = 50030
    const val UIA_SplitButtonControlTypeId = 50031
    const val UIA_WindowControlTypeId = 50032
    const val UIA_PaneControlTypeId = 50033
    const val UIA_HeaderControlTypeId = 50034
    const val UIA_HeaderItemControlTypeId = 50035
    const val UIA_TableControlTypeId = 50036
    const val UIA_TitleBarControlTypeId = 50037
    const val UIA_SeparatorControlTypeId = 50038

    // Property IDs
    const val UIA_RuntimeIdPropertyId = 30000
    const val UIA_BoundingRectanglePropertyId = 30001
    const val UIA_ProcessIdPropertyId = 30002
    const val UIA_ControlTypePropertyId = 30003
    const val UIA_LocalizedControlTypePropertyId = 30004
    const val UIA_NamePropertyId = 30005
    const val UIA_AcceleratorKeyPropertyId = 30006
    const val UIA_AccessKeyPropertyId = 30007
    const val UIA_HasKeyboardFocusPropertyId = 30008
    const val UIA_IsKeyboardFocusablePropertyId = 30009
    const val UIA_IsEnabledPropertyId = 30010
    const val UIA_AutomationIdPropertyId = 30011
    const val UIA_ClassNamePropertyId = 30012
    const val UIA_HelpTextPropertyId = 30013
    const val UIA_ClickablePointPropertyId = 30014
    const val UIA_CulturePropertyId = 30015
    const val UIA_IsControlElementPropertyId = 30016
    const val UIA_IsContentElementPropertyId = 30017
    const val UIA_LabeledByPropertyId = 30018
    const val UIA_IsPasswordPropertyId = 30019
    const val UIA_NativeWindowHandlePropertyId = 30020
    const val UIA_ItemTypePropertyId = 30021
    const val UIA_IsOffscreenPropertyId = 30022
    const val UIA_OrientationPropertyId = 30023
    const val UIA_FrameworkIdPropertyId = 30024
    const val UIA_IsRequiredForFormPropertyId = 30025
    const val UIA_ItemStatusPropertyId = 30026
    const val UIA_IsDockPatternAvailablePropertyId = 30027
    const val UIA_IsExpandCollapsePatternAvailablePropertyId = 30028
    const val UIA_IsGridItemPatternAvailablePropertyId = 30029
    const val UIA_IsGridPatternAvailablePropertyId = 30030
    const val UIA_IsInvokePatternAvailablePropertyId = 30031
    const val UIA_IsMultipleViewPatternAvailablePropertyId = 30032
    const val UIA_IsRangeValuePatternAvailablePropertyId = 30033
    const val UIA_IsScrollPatternAvailablePropertyId = 30034
    const val UIA_IsScrollItemPatternAvailablePropertyId = 30035
    const val UIA_IsSelectionItemPatternAvailablePropertyId = 30036
    const val UIA_IsSelectionPatternAvailablePropertyId = 30037
    const val UIA_IsTablePatternAvailablePropertyId = 30038
    const val UIA_IsTableItemPatternAvailablePropertyId = 30039
    const val UIA_IsTextPatternAvailablePropertyId = 30040
    const val UIA_IsTogglePatternAvailablePropertyId = 30041
    const val UIA_IsTransformPatternAvailablePropertyId = 30042
    const val UIA_IsValuePatternAvailablePropertyId = 30043
    const val UIA_IsWindowPatternAvailablePropertyId = 30044
    const val UIA_ValueValuePropertyId = 30045
    const val UIA_ValueIsReadOnlyPropertyId = 30046

    // Pattern IDs
    const val UIA_InvokePatternId = 10000
    const val UIA_SelectionPatternId = 10001
    const val UIA_ValuePatternId = 10002
    const val UIA_RangeValuePatternId = 10003
    const val UIA_ScrollPatternId = 10004
    const val UIA_ExpandCollapsePatternId = 10005
    const val UIA_GridPatternId = 10006
    const val UIA_GridItemPatternId = 10007
    const val UIA_MultipleViewPatternId = 10008
    const val UIA_WindowPatternId = 10009
    const val UIA_SelectionItemPatternId = 10010
    const val UIA_DockPatternId = 10011
    const val UIA_TablePatternId = 10012
    const val UIA_TableItemPatternId = 10013
    const val UIA_TextPatternId = 10014
    const val UIA_TogglePatternId = 10015
    const val UIA_TransformPatternId = 10016
    const val UIA_ScrollItemPatternId = 10017
    const val UIA_ItemContainerPatternId = 10018
    const val UIA_VirtualizedItemPatternId = 10019
    const val UIA_SynchronizedInputPatternId = 10020

    // Tree Scope
    const val TreeScope_Element = 1
    const val TreeScope_Children = 2
    const val TreeScope_Descendants = 4
    const val TreeScope_Subtree = 7

    // CLSID for UI Automation
    val CLSID_CUIAutomation = "{FF48DBA4-60EF-4201-AA87-54103EEF5804}"
    val IID_IUIAutomation = "{30CBE57D-D9D0-452A-AB13-7AC5AC4825EE}"

    // ScrollAmount for ScrollPattern
    const val ScrollAmount_LargeDecrement = 0
    const val ScrollAmount_SmallDecrement = 1
    const val ScrollAmount_NoAmount = 2
    const val ScrollAmount_LargeIncrement = 3
    const val ScrollAmount_SmallIncrement = 4
}
