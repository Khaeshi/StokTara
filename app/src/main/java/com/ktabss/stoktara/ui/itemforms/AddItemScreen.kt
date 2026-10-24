package com.ktabss.stoktara.ui.itemforms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktabss.stoktara.data.AppGraph
import com.ktabss.stoktara.data.InventoryRepository
import com.ktabss.stoktara.ui.alerts.AlertsViewModel
import com.ktabss.stoktara.ui.components.ScreenHeader
import com.ktabss.stoktara.ui.inventory.InventoryViewModel
import com.ktabss.stoktara.ui.theme.Paper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ktabss.stoktara.data.AppGraph.repository
import com.ktabss.stoktara.data.CategoryTag
import com.ktabss.stoktara.data.ItemFormErrors
import com.ktabss.stoktara.data.NewItem
import com.ktabss.stoktara.data.Profile
import com.ktabss.stoktara.ui.inventory.StokButton
import com.ktabss.stoktara.ui.theme.Danger
import com.ktabss.stoktara.ui.theme.InkSoft
import com.ktabss.stoktara.ui.theme.Line
import com.ktabss.stoktara.ui.theme.Primary
import kotlinx.coroutines.launch

class AddItemViewModel(private val repo: InventoryRepository = AppGraph.repository) : ViewModel() {
    private val _newItemName = MutableStateFlow("")
    val newItemName: StateFlow<String> = _newItemName.asStateFlow()

    fun setNewItemName(value: String) {
        _newItemName.value = value
    }

    val categories: StateFlow<List<CategoryTag>> = repo.categories

    private val _categoryId = MutableStateFlow<Long?>(null)
    val categoryId: StateFlow<Long?> = _categoryId.asStateFlow()

    init {
        viewModelScope.launch {
            categories.collect { list ->
                if (_categoryId.value == null && list.isNotEmpty()) {
                    _categoryId.value = list.first().id
                }
            }
        }
    }

    fun setCategory(id: Long?) {
        _categoryId.value = id
    }

    private val _unit = MutableStateFlow("")
    val unit: StateFlow<String> = _unit.asStateFlow()
    fun setUnit(value: String) { _unit.value = value }

    private val _quantity = MutableStateFlow("")
    val quantity: StateFlow<String> = _quantity.asStateFlow()
    fun setQuantity(value: String) { _quantity.value = value }

    private val _lowStockThreshold = MutableStateFlow("")
    val lowStockThreshold: StateFlow<String> = _lowStockThreshold.asStateFlow()
    fun setLowStockThreshold(value: String) { _lowStockThreshold.value = value }


    private val _photoUri = MutableStateFlow<String?>(null)
    val photoUri: StateFlow<String?> = _photoUri.asStateFlow()
    fun setPhotoUri(uri: String?) {
        _photoUri.value = uri
    }

    private val _sku = MutableStateFlow<String?>(null)
    val sku: StateFlow<String?> = _sku.asStateFlow()
    fun setSku(uri: String?) {
        _sku.value = uri
    }

    private val _formErrors = MutableStateFlow(ItemFormErrors())
    val formErrors: StateFlow<ItemFormErrors> = _formErrors.asStateFlow()

    fun addItem(actor: Profile, onSuccess: () -> Unit) {
        val name = _newItemName.value.trim()
        val unit = _unit.value.trim()
        val quantityStr = _quantity.value.trim()
        val thresholdStr = _lowStockThreshold.value.trim()
        val skuVal = _sku.value?.trim()

        val skuExists = !skuVal.isNullOrBlank() && repo.findItemBySku(skuVal) != null

        val errors = ItemFormErrors(
            name = if (name.isBlank()) "This field is required" else null,
            unit = if (unit.isBlank()) "This field is required" else null,
            quantity = if (quantityStr.toDoubleOrNull() == null) "Enter a valid number" else null,
            threshold = if (thresholdStr.toDoubleOrNull() == null) "Enter a valid number" else null,
            sku = if (skuExists) "SKU already in use" else null
        )

        _formErrors.value = errors

        if (errors.name != null || errors.unit != null || errors.quantity != null || errors.threshold != null) {
            return
        }

        viewModelScope.launch {
            val draft = NewItem(
                name = name,
                categoryTagId = _categoryId.value,
                unit = unit,
                quantity = quantityStr.toDouble(),
                threshold = thresholdStr.toDouble(),
                sku = _sku.value?.takeIf { it.isNotBlank() },
                photoUri = _photoUri.value?.takeIf { it.isNotBlank() }
            )

            val actor = repository.currentProfile.value
            repository.addItem(draft, actor)
            onSuccess()
        }
    }

    fun setScannedSku(sku: String) {
        _sku.value = sku
    }
    fun setCapturedPhoto(photoUri: String) {
        _photoUri.value = photoUri
    }

}

@Composable
fun AddItemScreen(
    onBack: () -> Unit,
    onScanClick: () -> Unit,
    onPhotoClick: () -> Unit,
    viewModel: AddItemViewModel = viewModel(),
) {
    val itemName by viewModel.newItemName.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val categoryId by viewModel.categoryId.collectAsStateWithLifecycle()
    val unit by viewModel.unit.collectAsStateWithLifecycle()
    val quantity by viewModel.quantity.collectAsStateWithLifecycle()
    val lowStockThreshold by viewModel.lowStockThreshold.collectAsStateWithLifecycle()
    val photoUri by viewModel.photoUri.collectAsStateWithLifecycle()
    val sku by viewModel.sku.collectAsStateWithLifecycle()
    val formErrors by viewModel.formErrors.collectAsStateWithLifecycle()

    val skuTrim = sku?.trim() ?: ""
    val skuAlreadyExists = skuTrim.isNotBlank() && repository.findItemBySku(skuTrim) != null

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Add Item", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Paper)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Item Name",
                color = InkSoft
            )
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

            Text(
                text = "Category",
                color = InkSoft
            )
            Spacer(modifier = Modifier.height(8.dp))
            CategoryDropdownField(
                categories = categories,
                selectedCategoryId = categoryId,
                onCategorySelected = { viewModel.setCategory(it) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Unit of Measure",
                color = InkSoft
            )
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

            Text(
                text = "Current Quantity",
                color = InkSoft
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = quantity,
                onValueChange = { viewModel.setQuantity(it) },
                placeholder = { Text("e.g. 10") },
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

            Text(
                text = "Low-Stock Threshold",
                color = InkSoft
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = lowStockThreshold,
                onValueChange = { viewModel.setLowStockThreshold(it) },
                placeholder = { Text("e.g. 5") },
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

            Text(
                text = "Item Photo",
                color = InkSoft
            )
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

            StokButton(
                label = "Add Item",
                onClick = {
                    viewModel.addItem(repository.currentProfile.value) {
                        onBack()
                    }
                }
            )

            StokButton(
                label = "Cancel",
                onClick = { onBack() },
                containerColor = Paper,
                textColor = Color.Black
            )
        }
    }
}