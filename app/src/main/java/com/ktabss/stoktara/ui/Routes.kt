package com.ktabss.stoktara.ui.navigation

object Routes {
    const val INTRO = "intro"
    const val HOME = "home"
    const val INVENTORY = "inventory"
    const val SCAN = "scan"
    const val SCAN_MATCHED = "scan/matched/{itemId}" // "Barcode Matched" screen after a scan / SKU lookup
    const val ALERTS = "alerts"
    const val ACTIVITY = "activity"
    const val SETTINGS = "settings"          // Business Profile (owner) / Your Profile (staff)
    const val ADD_ITEM = "item/add"
    const val EDIT_ITEM = "item/edit/{itemId}"

    fun editItem(itemId: Long) = "item/edit/$itemId"
    fun scanMatched(itemId: Long) = "scan/matched/$itemId"
}
