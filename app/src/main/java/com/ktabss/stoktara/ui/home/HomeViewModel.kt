package com.ktabss.stoktara.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ktabss.stoktara.data.AppGraph
import com.ktabss.stoktara.data.AuditLog
import com.ktabss.stoktara.data.InventoryRepository
import com.ktabss.stoktara.data.Item
import com.ktabss.stoktara.data.StockStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val businessName: String,
    val totalItems: Int,
    val lowCount: Int,
    val outCount: Int,
    val loggedMoves: Int,
    val recent: List<AuditLog>,
) {
    val restockCount: Int get() = lowCount + outCount
}

private fun buildHomeState(name: String, items: List<Item>, logs: List<AuditLog>) = HomeUiState(
    businessName = name,
    totalItems = items.size,
    lowCount = items.count { it.status == StockStatus.LOW },
    outCount = items.count { it.status == StockStatus.OUT },
    loggedMoves = logs.size,
    recent = logs.take(3),
)

// All-default constructor => Kotlin generates a no-arg one, so plain `viewModel()` works without a factory.
class HomeViewModel(repo: InventoryRepository = AppGraph.repository) : ViewModel() {
    val state: StateFlow<HomeUiState> =
        combine(repo.businessName, repo.items, repo.auditLog) { name, items, logs ->
            buildHomeState(name, items, logs)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            buildHomeState(repo.businessName.value, repo.items.value, repo.auditLog.value),
        )
}
