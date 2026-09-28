package com.ktabss.stoktara.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktabss.stoktara.data.AppGraph
import com.ktabss.stoktara.data.InventoryRepository
import com.ktabss.stoktara.data.Item
import com.ktabss.stoktara.data.needsRestocking
import com.ktabss.stoktara.ui.components.EmptyNote
import com.ktabss.stoktara.ui.components.ScreenHeader
import com.ktabss.stoktara.ui.components.StatusPill
import com.ktabss.stoktara.ui.components.groupedRowsCard
import com.ktabss.stoktara.ui.theme.Amber
import com.ktabss.stoktara.ui.theme.InkSoft
import com.ktabss.stoktara.ui.theme.Line
import com.ktabss.stoktara.ui.theme.Placeholder
import com.ktabss.stoktara.ui.theme.RowFill
import com.ktabss.stoktara.ui.util.formatQtyWithUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AlertsViewModel(repo: InventoryRepository = AppGraph.repository) : ViewModel() {
    val alerts: StateFlow<List<Item>> = repo.items
        .map { it.needsRestocking() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repo.items.value.needsRestocking())
}

/** UC6: count + top 5 items running low, "show all" reveals the full list. */
private const val PREVIEW_LIMIT = 5

@Composable
fun AlertsScreen(onBack: () -> Unit, viewModel: AlertsViewModel = viewModel()) {
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    var showAll by rememberSaveable { mutableStateOf(false) }
    val visible = if (showAll) alerts else alerts.take(PREVIEW_LIMIT)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Low Stocks Alert", onBack = onBack)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (alerts.isEmpty()) {
                EmptyNote("Nothing needs restocking right now.")
            } else {
                val subtitle = if (alerts.size == 1) "1 item is at or below its low-stock threshold"
                else "${alerts.size} items at or below their low-stock threshold"
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = InkSoft)
                Column(Modifier.fillMaxWidth().groupedRowsCard()) {
                    visible.forEachIndexed { i, item ->
                        if (i > 0) HorizontalDivider(color = Line)
                        AlertRow(item)
                    }
                }
                if (!showAll && alerts.size > PREVIEW_LIMIT) {
                    Text(
                        "Show all ${alerts.size}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Amber,
                        modifier = Modifier.clickable { showAll = true }.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertRow(item: Item) {
    Row(
        modifier = Modifier.fillMaxWidth().background(RowFill).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Photo placeholder: swap for the item photo once Member 2 stores photoUri.
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Placeholder))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            Text(
                "Threshold: ${formatQtyWithUnit(item.threshold, item.unit)}",
                style = MaterialTheme.typography.labelSmall,
                color = InkSoft,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                formatQtyWithUnit(item.quantity, item.unit),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            )
            StatusPill(item.status)
        }
    }
}
