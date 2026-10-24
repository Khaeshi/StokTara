package com.ktabss.stoktara.ui.itemforms

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktabss.stoktara.data.AppGraph
import com.ktabss.stoktara.data.InventoryRepository
import com.ktabss.stoktara.data.MockBarcodeScanner
import com.ktabss.stoktara.ui.inventory.StokButton
import com.ktabss.stoktara.ui.theme.InkSoft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ScanBg = Color(0xFF0F1512)
private val ScanField = Color(0xFF1B211E)
private val ScanLine = Color(0xFF4A4F4B)

class ItemScanViewModel(private val repo: InventoryRepository = AppGraph.repository) : ViewModel() {

    fun scanForSku(onSkuScanned: (String) -> Unit) {
        var foundSku: String? = null

        for (i in 0 until 8) {
            val code = MockBarcodeScanner.nextCode()
            val candidateSku = MockBarcodeScanner.skuFor(code) ?: code

            if (repo.findItemBySku(candidateSku) == null) {
                foundSku = candidateSku
                break
            }
        }

        if (foundSku == null) {
            var nextNum = 9
            while (repo.findItemBySku(String.format("BKY-%04d", nextNum)) != null) {
                nextNum++
            }
            foundSku = String.format("BKY-%04d", nextNum)
        }

        onSkuScanned(foundSku)
    }
}

// For Barcode Capture
@Composable
fun ItemScanScreen(
    onBack: () -> Unit,
    onSkuScanned: (String) -> Unit,
    viewModel: ItemScanViewModel = viewModel(),
) {
    SystemBarIcons(lightIcons = true)

    Column(
        Modifier
            .fillMaxSize()
            .background(ScanBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Scan Barcode",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.ChevronLeft,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(2.dp, Color.White, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Point at barcode",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(48.dp))

        StokButton(
            label = "Capture Barcode",
            onClick = {
                viewModel.scanForSku { sku ->
                    onSkuScanned(sku)
                    onBack()
                }
            },
            modifier = Modifier.width(220.dp),
        )
    }
}


// For item photo capture
@Composable
fun ItemPhotoScreen(
    onBack: () -> Unit,
    onPhotoCaptured: (String) -> Unit
) {
    SystemBarIcons(lightIcons = true)

    Column(Modifier.fillMaxSize().background(ScanBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Take Item Photo", style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.weight(1f))
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
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).border(2.dp, Color.White, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("Point at your item", style = MaterialTheme.typography.bodySmall, color = InkSoft)
            }

            Spacer(Modifier.height(48.dp))

            StokButton(
                label = "Capture Photo",
                onClick = {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val generatedUri = "item_photo_$timeStamp.jpg"
                    onPhotoCaptured(generatedUri)
                    onBack()
                },
                modifier = Modifier.width(220.dp),
            )
        }
    }
}

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