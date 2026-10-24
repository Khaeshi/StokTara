package com.ktabss.stoktara.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktabss.stoktara.data.AppGraph
import com.ktabss.stoktara.data.CategoryTag
import com.ktabss.stoktara.data.InventoryRepository
import com.ktabss.stoktara.data.Item
import com.ktabss.stoktara.data.StockStatus
import com.ktabss.stoktara.ui.components.EmptyNote
import com.ktabss.stoktara.ui.components.IconSquareButton
import com.ktabss.stoktara.ui.components.StatusPill
import com.ktabss.stoktara.ui.components.groupedRowsCard
import com.ktabss.stoktara.ui.theme.InkFaint
import com.ktabss.stoktara.ui.theme.InkSoft
import com.ktabss.stoktara.ui.theme.Line
import com.ktabss.stoktara.ui.theme.Placeholder
import com.ktabss.stoktara.ui.theme.Primary
import com.ktabss.stoktara.ui.theme.RowFill
import com.ktabss.stoktara.ui.util.formatQtyWithUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class InventoryViewModel(private val repo: InventoryRepository = AppGraph.repository) : ViewModel() {
    private val _query = MutableStateFlow("")
    private val _categoryId = MutableStateFlow<Long?>(null) // null = All
    private val _status = MutableStateFlow<StockStatus?>(null) // null = All

    val query: StateFlow<String> = _query.asStateFlow()
    val categoryId: StateFlow<Long?> = _categoryId.asStateFlow()
    val status: StateFlow<StockStatus?> = _status.asStateFlow()
    val categories: StateFlow<List<CategoryTag>> = repo.categories

    /** Search (name or SKU) AND category AND status, all combined. */
    val items: StateFlow<List<Item>> = combine(repo.items, _query, _categoryId, _status) { all, q, cat, st ->
        val needle = q.trim()
        all.filter { item ->
            (cat == null || item.categoryTagId == cat) &&
                (st == null || item.status == st) &&
                (needle.isEmpty() ||
                    item.name.contains(needle, ignoreCase = true) ||
                    (item.sku?.contains(needle, ignoreCase = true) == true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repo.items.value)

    fun setQuery(value: String) { _query.value = value }
    fun setCategory(id: Long?) { _categoryId.value = id }
    fun setStatus(value: StockStatus?) { _status.value = value }

    fun find(id: Long): Item? = repo.findItem(id)
}

/**
 * Inventory tab. Tapping a card opens the Quick Stock overlay;
 * "+" goes to Add Item and "Edit Item Details" goes to the Edit Item modal (both Member 2's).
 */
@Composable
fun InventoryScreen(
    onAddItem: () -> Unit,
    onEditItem: (Long) -> Unit,
    viewModel: InventoryViewModel = viewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val categoryId by viewModel.categoryId.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    var quickStockId by rememberSaveable { mutableStateOf<Long?>(null) }

    val categoryLabels = categories.associate { it.id to it.label }

    Column(Modifier.fillMaxSize()) {
        // Header: title left, "+" right (same layout as ScreenHeader, but with an add button)
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Inventory", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconSquareButton(Icons.Outlined.Add, "Add item", onAddItem)
            }
            HorizontalDivider(color = Line)
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Search items...", style = MaterialTheme.typography.bodyMedium, color = InkSoft) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = InkSoft) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Line,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterPill("All", categoryId == null) { viewModel.setCategory(null) } }
                items(categories, key = { it.id }) { c ->
                    FilterPill(c.label, categoryId == c.id) { viewModel.setCategory(c.id) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill("All", status == null) { viewModel.setStatus(null) }
                FilterPill("Low", status == StockStatus.LOW) { viewModel.setStatus(StockStatus.LOW) }
                FilterPill("OK", status == StockStatus.OK) { viewModel.setStatus(StockStatus.OK) }
                FilterPill("Out", status == StockStatus.OUT) { viewModel.setStatus(StockStatus.OUT) }
            }
        }

        if (items.isEmpty()) {
            val filtering = query.isNotBlank() || categoryId != null || status != null
            EmptyNote(if (filtering) "No items match your search." else "No items yet. Tap + to add one.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .weight(1f, fill = false)
                    .groupedRowsCard(),
            ) {
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    if (index > 0) HorizontalDivider(color = Line)
                    ItemRow(
                        item = item,
                        categoryLabel = item.categoryTagId?.let { categoryLabels[it] },
                        onClick = { quickStockId = item.id },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    val quickItem = quickStockId?.let { viewModel.find(it) }
    if (quickItem != null) {
        QuickStockSheet(
            item = quickItem,
            onDismiss = { quickStockId = null },
            onEditDetails = { onEditItem(quickItem.id) },
        )
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) Color.White else InkFaint,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Primary else Placeholder.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun ItemRow(item: Item, categoryLabel: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RowFill)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Photo placeholder: swap for the real image once an image loader is added (item.photoUri).
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(Placeholder))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                item.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOfNotNull(categoryLabel, item.sku).joinToString(" | "),
                style = MaterialTheme.typography.labelSmall,
                color = InkSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                formatQtyWithUnit(item.quantity, item.unit),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            )
            StatusPill(item.status)
        }
    }
}
