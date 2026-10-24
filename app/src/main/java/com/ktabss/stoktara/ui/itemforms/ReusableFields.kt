package com.ktabss.stoktara.ui.itemforms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ktabss.stoktara.data.CategoryTag
import com.ktabss.stoktara.ui.theme.Amber
import com.ktabss.stoktara.ui.theme.BannerFill
import com.ktabss.stoktara.ui.theme.InkFaint
import com.ktabss.stoktara.ui.theme.InkSoft
import com.ktabss.stoktara.ui.theme.Line
import com.ktabss.stoktara.ui.theme.Primary

class ReusableFields {
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdownField(
    categories: List<CategoryTag>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedCategoryLabel = categories.find { it.id == selectedCategoryId }?.label
    val isEmpty = categories.isEmpty()

    val displayText = when {
        isEmpty -> "No categories, add first"
        selectedCategoryLabel != null -> selectedCategoryLabel
        else -> "Select category"
    }

    ExposedDropdownMenuBox(
        expanded = expanded && !isEmpty,
        onExpandedChange = { if (!isEmpty) expanded = !expanded },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            enabled = !isEmpty,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && !isEmpty)
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = !isEmpty),
        )

        if (!isEmpty) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.label) },
                        onClick = {
                            onCategorySelected(category.id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}


@Composable
fun CameraField(
    value: String?,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {},
    onCameraClick: () -> Unit,
    onRemoveClick: () -> Unit,
    isEditable: Boolean = false,
    onFieldClick: () -> Unit = {},
    placeholder: String = "Tap camera to select photo...",
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isEditable) {
                OutlinedTextField(
                    value = value ?: "",
                    onValueChange = onValueChange,
                    placeholder = { Text(placeholder, color = InkFaint) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Line,
                    ),
                    trailingIcon = {
                        if (!value.isNullOrBlank()) {
                            Text(
                                text = "Remove",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = Amber,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable(onClick = onRemoveClick),
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Line, RoundedCornerShape(12.dp))
                        .clickable(onClick = onFieldClick)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = value?.ifBlank { null } ?: placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (value.isNullOrBlank()) InkFaint else Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )

                        if (!value.isNullOrBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Remove",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = Amber,
                                modifier = Modifier.clickable(onClick = onRemoveClick),
                            )
                        }
                    }
                }
            }

            Surface(
                onClick = onCameraClick,
                shape = RoundedCornerShape(12.dp),
                color = Amber,
                modifier = Modifier.size(56.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = "Action button",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}