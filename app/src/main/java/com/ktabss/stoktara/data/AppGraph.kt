package com.ktabss.stoktara.data

/**
 * Tiny service locator so ViewModels can be created with plain `viewModel()`.
 * When Room arrives: build the Room-backed repository here (pass applicationContext from an Application class).
 */
object AppGraph {
    val repository: InventoryRepository by lazy { InMemoryInventoryRepository() }
}
