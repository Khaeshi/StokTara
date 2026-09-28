package com.ktabss.stoktara.ui.navigation

object Routes {
    const val HOME = "home"
    const val INVENTORY = "inventory"
    const val SCAN = "scan"
    const val ALERTS = "alerts"
    const val ACTIVITY = "activity"
    const val SETTINGS = "settings"          // Business Profile (owner) / Your Profile (staff)
    const val ADD_ITEM = "item/add"
    const val EDIT_ITEM = "item/edit/{itemId}"

    fun editItem(itemId: Long) = "item/edit/$itemId"
}
