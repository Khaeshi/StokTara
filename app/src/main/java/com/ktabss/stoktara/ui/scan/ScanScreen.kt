package com.ktabss.stoktara.ui.scan

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktabss.stoktara.data.AppGraph
import com.ktabss.stoktara.data.InventoryRepository
import com.ktabss.stoktara.data.Item
import com.ktabss.stoktara.data.MockBarcodeScanner
import com.ktabss.stoktara.ui.components.ScreenHeader
import com.ktabss.stoktara.ui.components.softCard
import com.ktabss.stoktara.ui.inventory.QuantityEditor
import com.ktabss.stoktara.ui.inventory.StockEditViewModel
import com.ktabss.stoktara.ui.inventory.StokButton
import com.ktabss.stoktara.ui.theme.InkSoft
import com.ktabss.stoktara.ui.theme.Primary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Background of the viewfinder screen. StokTaraApp also uses it so the status-bar strip matches. */
val ScanBg = Color(0xFF0F1512)
private val ScanField = Color(0xFF1B211E)
private val ScanLine = Color(0xFF4A4F4B)

class ScanViewModel(private val repo: InventoryRepository = AppGraph.repository) : ViewModel() {
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() { _error.value = null }

    /** Mock scan: next barcode from the HashMap -> SKU -> item. */
    fun scan(onMatched: (Long) -> Unit) {
        val code = MockBarcodeScanner.nextCode()
        val item = MockBarcodeScanner.skuFor(code)?.let(repo::findItemBySku)
        if (item != null) { _error.value = null; onMatched(item.id) } else _error.value = "Barcode not recognized."
    }

    /** Manual SKU fallback. */
    fun lookupSku(sku: String, onMatched: (Long) -> Unit) {
        val item = repo.findItemBySku(sku)
        if (item != null) { _error.value = null; onMatched(item.id) } else _error.value = "No item found for \"${sku.trim()}\"."
    }

    fun find(id: Long): Item? = repo.findItem(id)
    fun observe(id: Long): Flow<Item?> = repo.items.map { list -> list.firstOrNull { it.id == id } }
    fun categoryLabel(id: Long?): String? = repo.categories.value.firstOrNull { it.id == id }?.label
}

/** Scan tab: viewfinder + manual SKU lookup. A match navigates to the Barcode Matched screen. */
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onMatched: (Long) -> Unit,
    viewModel: ScanViewModel = viewModel(),
) {
    val error by viewModel.error.collectAsStateWithLifecycle()
    var sku by rememberSaveable { mutableStateOf("") }

    SystemBarIcons(lightIcons = true) // dark screen needs white status/nav icons

    Column(Modifier.fillMaxSize().background(ScanBg).imePadding().verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Scan Barcode", style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.ChevronLeft, "Back", tint = Color.White, modifier = Modifier.size(20.dp)) }
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            // Viewfinder placeholder: replace the Box content with a CameraX preview later.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.08f)
                    .border(2.dp, Color.White, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("Point at a barcode", style = MaterialTheme.typography.bodySmall, color = InkSoft)
            }
            Spacer(Modifier.height(32.dp))

            StokButton(
                label = "Scan",
                onClick = { viewModel.scan(onMatched) },
                modifier = Modifier.width(210.dp),
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it.uppercase(); viewModel.clearError() },
                placeholder = { Text("or type SKU e.g. BKY-0002", style = MaterialTheme.typography.bodyMedium, color = InkSoft, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = ScanField,
                    unfocusedContainerColor = ScanField,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = ScanLine,
                    cursorColor = Color.White,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ScanLine, RoundedCornerShape(12.dp))
                    .clickable(enabled = sku.isNotBlank()) { viewModel.lookupSku(sku) { sku = ""; onMatched(it) } }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Look up SKU",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = if (sku.isNotBlank()) 1f else 0.5f),
                )
            }
            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = Color(0xFFE59A9A), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Barcode Matched: shows the matched item and lets you set the quantity.
 * Saves through the same StockEditViewModel -> recordStockChange() path as the Inventory Quick Stock overlay.
 * After saving, it pops back to the viewfinder so you can keep scanning.
 */
@Composable
fun BarcodeMatchedScreen(
    itemId: Long,
    onBack: () -> Unit,
    onEditDetails: (Long) -> Unit,
    viewModel: ScanViewModel = viewModel(),
    edit: StockEditViewModel = viewModel(),
) {
    SystemBarIcons(lightIcons = false)

    val item by remember(itemId) { viewModel.observe(itemId) }
        .collectAsStateWithLifecycle(initialValue = viewModel.find(itemId))

    // Item was deleted (e.g. via Edit Item Details) -> nothing left to show.
    LaunchedEffect(item == null) { if (item == null) onBack() }

    val current = item ?: return

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Barcode Matched", onBack = onBack)
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().softCard(fill = Color.White).padding(vertical = 36.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(current.name, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Text(
                    listOfNotNull(viewModel.categoryLabel(current.categoryTagId), current.sku).joinToString(" | "),
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSoft,
                )
            }
            Text(
                "Current Quantity",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            QuantityEditor(
                item = current,
                onEditDetails = { onEditDetails(current.id) },
                onSave = { q -> edit.saveQuantity(current.id, q, onBack) },
            )
        }
    }
}

/** Status/nav bar icon color for this screen; restores dark icons when leaving. */
@Composable
private fun SystemBarIcons(lightIcons: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, lightIcons) {
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = !lightIcons
        controller?.isAppearanceLightNavigationBars = !lightIcons
        onDispose {
            controller?.isAppearanceLightStatusBars = true
            controller?.isAppearanceLightNavigationBars = true
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
