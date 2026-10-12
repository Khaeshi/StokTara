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
    const val EDIT_ITEM_DETAILS = "item/edit/details/{itemId}"

    const val SCAN_BARCODE = "item/scan_barcode"
    const val CAPTURE_PHOTO = "item/capture_photo"

    fun editItem(itemId: Long) = "item/edit/$itemId"
    fun editItemDetails(itemId: Long) = "item/edit/details/$itemId"
    fun scanMatched(itemId: Long) = "scan/matched/$itemId"
}
