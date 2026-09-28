package com.ktabss.stoktara.ui.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ktabss.stoktara.ui.components.ScreenHeader
import com.ktabss.stoktara.ui.components.softCard
import com.ktabss.stoktara.ui.theme.InkSoft

/** Stand-in for a screen someone else owns. Replace the whole composable body with the Figma UI. */
@Composable
fun PlaceholderScreen(title: String, owner: String, hint: String, onBack: (() -> Unit)? = null) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title, onBack)
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.softCard().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("$owner\u2019s screen", style = MaterialTheme.typography.titleMedium)
                Text(hint, style = MaterialTheme.typography.bodySmall, color = InkSoft, textAlign = TextAlign.Center)
            }
        }
    }
}
