package com.ktabss.stoktara.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktabss.stoktara.data.AppGraph
import com.ktabss.stoktara.data.InventoryRepository
import com.ktabss.stoktara.data.Item
import com.ktabss.stoktara.ui.theme.Amber
import com.ktabss.stoktara.ui.theme.InkSoft
import com.ktabss.stoktara.ui.theme.Line
import com.ktabss.stoktara.ui.theme.Paper
import com.ktabss.stoktara.ui.theme.Primary
import com.ktabss.stoktara.ui.util.formatQty
import kotlinx.coroutines.launch

/**
 * THE single "set the quantity" path for Member 3's screens. Both the Inventory Quick Stock
 * overlay and the Scan tab's Barcode Matched screen call this, and it only ever talks to
 * repository.recordStockChange() (so the audit log gets a Stock In / Stock Out entry
 * attributed to the current profile).
 */
class StockEditViewModel(private val repo: InventoryRepository = AppGraph.repository) : ViewModel() {
    fun saveQuantity(itemId: Long, newQty: Double, onDone: () -> Unit) {
        viewModelScope.launch {
            val current = repo.findItem(itemId)
            if (current != null) {
                val delta = newQty - current.quantity
                if (delta != 0.0) repo.recordStockChange(itemId, delta, repo.currentProfile.value)
            }
            onDone()
        }
    }
}

/** Filled green button used for Save Item / Scan. */
@Composable
fun StokButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) Primary else Primary.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

private val QtyPattern = Regex("""\d{0,7}(\.\d{0,2})?""")

/**
 * Quantity field + "Edit Item Details" + "Save Item". Shared by the Quick Stock sheet and the
 * Barcode Matched screen. Dumb on purpose: the caller decides what saving does.
 */
@Composable
fun QuantityEditor(item: Item, onEditDetails: () -> Unit, onSave: (Double) -> Unit) {
    var text by rememberSaveable(item.id) { mutableStateOf(formatQty(item.quantity)) }
    val qty = text.toDoubleOrNull()
    val big = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { if (QtyPattern.matches(it)) text = it },
            singleLine = true,
            isError = qty == null,
            textStyle = big.copy(textAlign = TextAlign.Center),
            suffix = { Text(item.unit, style = big, color = InkSoft) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Primary,
                unfocusedBorderColor = Line,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onEditDetails)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Edit, null, tint = Amber, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Edit Item Details", style = MaterialTheme.typography.labelLarge, color = Amber)
        }
        StokButton("Save Item", onClick = { if (qty != null) onSave(qty) }, enabled = qty != null)
    }
}

/** Quick Stock overlay: opens when an item card is tapped in the Inventory list. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickStockSheet(
    item: Item,
    onDismiss: () -> Unit,
    onEditDetails: () -> Unit,
    edit: StockEditViewModel = viewModel(),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Paper,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp).imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Update Item", style = MaterialTheme.typography.titleLarge)
            Row {
                Text("Current Quantity of: ", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                Text(item.name, style = MaterialTheme.typography.bodyMedium)
            }
            QuantityEditor(
                item = item,
                onEditDetails = { onDismiss(); onEditDetails() },
                onSave = { q -> edit.saveQuantity(item.id, q, onDismiss) },
            )
        }
    }
}
