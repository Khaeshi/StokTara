package com.ktabss.stoktara.ui.itemforms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ktabss.stoktara.data.CategoryTag
import com.ktabss.stoktara.data.InventoryRepository
import com.ktabss.stoktara.data.Item
import com.ktabss.stoktara.data.ItemFormErrors
import com.ktabss.stoktara.ui.components.ScreenHeader
import com.ktabss.stoktara.ui.inventory.StokButton
import com.ktabss.stoktara.ui.theme.Danger
import com.ktabss.stoktara.ui.theme.InkSoft
import com.ktabss.stoktara.ui.theme.Line
import com.ktabss.stoktara.ui.theme.Paper
import com.ktabss.stoktara.ui.theme.Primary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditItemViewModel(
    private val itemId: Long,
    private val repository: InventoryRepository
) : ViewModel() {

    private val _itemName = MutableStateFlow("")
    val itemName: StateFlow<String> = _itemName.asStateFlow()

    private val _categoryId = MutableStateFlow<Long?>(null)
    val categoryId: StateFlow<Long?> = _categoryId.asStateFlow()

    private val _unit = MutableStateFlow("")
    val unit: StateFlow<String> = _unit.asStateFlow()

    private val _quantity = MutableStateFlow("")
    val quantity: StateFlow<String> = _quantity.asStateFlow()

    private val _lowStockThreshold = MutableStateFlow("")
    val lowStockThreshold: StateFlow<String> = _lowStockThreshold.asStateFlow()

    private val _sku = MutableStateFlow<String?>(null)
    val sku: StateFlow<String?> = _sku.asStateFlow()

    private val _photoUri = MutableStateFlow<String?>(null)
    val photoUri: StateFlow<String?> = _photoUri.asStateFlow()

    private val _formErrors = MutableStateFlow(ItemFormErrors())
    val formErrors: StateFlow<ItemFormErrors> = _formErrors.asStateFlow()

    private val _dateAdded = MutableStateFlow<Long?>(null)
    val dateAdded: StateFlow<Long?> = _dateAdded.asStateFlow()

    val categories: StateFlow<List<CategoryTag>> = repository.categories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadItem()
    }

    private fun loadItem() {
        viewModelScope.launch {
            val item = repository.getItem(itemId)
            if (item != null) {
                _dateAdded.value = item.dateAdded
                _itemName.value = item.name
                _categoryId.value = item.categoryTagId
                _unit.value = item.unit
                _quantity.value = item.quantity.toString()
                _lowStockThreshold.value = item.threshold.toString()
                _sku.value = item.sku
                _photoUri.value = item.photoUri
            }
        }
    }

    fun setNewItemName(name: String) { _itemName.value = name }
    fun setCategory(id: Long?) { _categoryId.value = id }
    fun setUnit(u: String) { _unit.value = u }
    fun setQuantity(q: String) { _quantity.value = q }
    fun setLowStockThreshold(t: String) { _lowStockThreshold.value = t }
    fun setSku(s: String?) { _sku.value = s }
    fun setPhotoUri(uri: String?) { _photoUri.value = uri }

    fun setScannedSku(scannedSku: String) {
        _sku.value = scannedSku
    }

    fun setCapturedPhoto(uri: String) {
        _photoUri.value = uri
    }

    fun updateItem(onSuccess: () -> Unit) {
        val name = _itemName.value.trim()
        val unit = _unit.value.trim()
        val quantityStr = _quantity.value.trim()
        val thresholdStr = _lowStockThreshold.value.trim()
        val skuVal = _sku.value?.trim()

        val skuExists = !skuVal.isNullOrBlank() && repository.findItemBySku(skuVal)?.let { it.id != itemId } == true

        val errors = ItemFormErrors(
            name = if (name.isBlank()) "This field is required" else null,
            unit = if (unit.isBlank()) "This field is required" else null,
            quantity = if (quantityStr.toDoubleOrNull() == null) "Enter a valid number" else null,
            threshold = if (thresholdStr.toDoubleOrNull() == null) "Enter a valid number" else null,
            sku = if (skuExists) "SKU already in use" else null
        )

        _formErrors.value = errors
        if (errors.name != null || errors.unit != null || errors.quantity != null || errors.threshold != null || errors.sku != null) return

        viewModelScope.launch {
            val actor = repository.currentProfile.value

            val updatedItem = Item(
                id = itemId,
                name = name,
                categoryTagId = _categoryId.value,
                unit = unit,
                quantity = quantityStr.toDouble(),
                threshold = thresholdStr.toDouble(),
                sku = skuVal?.takeIf { it.isNotBlank() },
                photoUri = _photoUri.value?.takeIf { it.isNotBlank() },
                dateAdded = _dateAdded.value ?: System.currentTimeMillis()
            )

            repository.updateItem(updatedItem, actor)
            onSuccess()
        }
    }

    fun deleteItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val actor = repository.currentProfile.value
            repository.deleteItem(itemId, actor)
            onSuccess()
        }
    }
}

@Composable
fun EditItemScreen(
    viewModel: EditItemViewModel,
    onBack: () -> Unit,
    onScanClick: () -> Unit,
    onPhotoClick: () -> Unit
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val itemName by viewModel.itemName.collectAsStateWithLifecycle()
    val categoryId by viewModel.categoryId.collectAsStateWithLifecycle()
    val unit by viewModel.unit.collectAsStateWithLifecycle()
    val quantity by viewModel.quantity.collectAsStateWithLifecycle()
    val threshold by viewModel.lowStockThreshold.collectAsStateWithLifecycle()
    val sku by viewModel.sku.collectAsStateWithLifecycle()
    val photoUri by viewModel.photoUri.collectAsStateWithLifecycle()
    val dateAdded by viewModel.dateAdded.collectAsStateWithLifecycle()
    val formErrors by viewModel.formErrors.collectAsStateWithLifecycle()

    val formattedDate = remember(dateAdded) {
        if (dateAdded != null) {
            val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
            sdf.format(Date(dateAdded!!))
        } else {
            "No Added Date Recorded."
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Edit Item", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Paper)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Originally added: $formattedDate",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Item Name", color = InkSoft)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = itemName,
                onValueChange = { viewModel.setNewItemName(it) },
                placeholder = { Text("e.g. Eggs") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Line,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (formErrors.name != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = formErrors.name!!, style = MaterialTheme.typography.bodySmall, color = Danger)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Category", color = InkSoft)
            Spacer(modifier = Modifier.height(8.dp))
            CategoryDropdownField(
                categories = categories,
                selectedCategoryId = categoryId,
                onCategorySelected = { viewModel.setCategory(it) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Unit of Measure", color = InkSoft)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = unit,
                onValueChange = { viewModel.setUnit(it) },
                placeholder = { Text("e.g. kg, pcs") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Line,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (formErrors.unit != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = formErrors.unit!!, style = MaterialTheme.typography.bodySmall, color = Danger)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Current Quantity", color = InkSoft)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = quantity,
                onValueChange = { viewModel.setQuantity(it) },
                placeholder = { Text("e.g. 10") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Line,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (formErrors.quantity != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = formErrors.quantity!!, style = MaterialTheme.typography.bodySmall, color = Danger)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Low-Stock Threshold", color = InkSoft)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = threshold,
                onValueChange = { viewModel.setLowStockThreshold(it) },
                placeholder = { Text("e.g. 5") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Line,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (formErrors.threshold != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = formErrors.threshold!!, style = MaterialTheme.typography.bodySmall, color = Danger)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Item Photo", color = InkSoft)
            Spacer(modifier = Modifier.height(8.dp))
            CameraField(
                value = photoUri,
                onCameraClick = { onPhotoClick() },
                onRemoveClick = { viewModel.setPhotoUri(null) },
                placeholder = "Tap camera to select photo..."
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "SKU / Barcode", color = InkSoft)
            Spacer(modifier = Modifier.height(8.dp))
            CameraField(
                value = sku,
                isEditable = true,
                onValueChange = { viewModel.setSku(it) },
                onCameraClick = { onScanClick() },
                onRemoveClick = { viewModel.setSku("") },
                placeholder = "Enter SKU or scan barcode..."
            )
            if (formErrors.sku != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = formErrors.sku!!, style = MaterialTheme.typography.bodySmall, color = Danger)
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StokButton(
                    label = "Delete",
                    containerColor = Danger.copy(alpha = 0.1f),
                    textColor = Danger,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.deleteItem { onBack() } }
                )

                StokButton(
                    label = "Save Changes",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.updateItem { onBack() } }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            StokButton(
                label = "Cancel",
                containerColor = Paper,
                textColor = Color.Black,
                onClick = { onBack() }
            )
        }
    }
}