package com.ktabss.stoktara.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktabss.stoktara.ui.components.ActivityRow
import com.ktabss.stoktara.ui.components.EmptyNote
import com.ktabss.stoktara.ui.components.IconSquareButton
import com.ktabss.stoktara.ui.components.SectionLabel
import com.ktabss.stoktara.ui.components.groupedRowsCard
import com.ktabss.stoktara.ui.components.softCard
import com.ktabss.stoktara.ui.theme.Amber
import com.ktabss.stoktara.ui.theme.BannerFill
import com.ktabss.stoktara.ui.theme.Ink
import com.ktabss.stoktara.ui.theme.InkSoft
import com.ktabss.stoktara.ui.theme.Line
import com.ktabss.stoktara.ui.theme.Primary
import java.util.Calendar

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenScan: () -> Unit,
    onOpenAlerts: () -> Unit,
    onAddItem: () -> Unit,
    onSeeMoreActivity: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val s by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(greeting(), style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                Text(s.businessName, style = MaterialTheme.typography.titleLarge)
            }
            IconSquareButton(Icons.Outlined.Settings, "Settings", onOpenSettings)
        }

        // Restock banner (hidden when nothing needs restocking)
        if (s.restockCount > 0) {
            val text = if (s.restockCount == 1) "1 item needs restocking" else "${s.restockCount} items need restocking"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BannerFill)
                    .clickable(onClick = onOpenAlerts)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.NotificationsNone, null, tint = Amber, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text("$text - Tap to review", style = MaterialTheme.typography.bodyMedium, color = Amber)
            }
        }

        // Stat cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Total items tracked", s.totalItems.toString(), Ink)
            StatCard("Low stock", s.lowCount.toString(), Amber)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Out stock", s.outCount.toString(), Amber)
            StatCard("Logged stock moves", s.loggedMoves.toString(), Ink)
        }

        // Quick actions
        Spacer(Modifier.size(4.dp))
        SectionLabel("Quick actions")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Primary)
                .clickable(onClick = onOpenScan)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Scan Barcode", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text("Log stock in / out", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
            }
            Icon(Icons.Outlined.QrCode2, "Scan", tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile("Add Item", Icons.Outlined.Add, onAddItem)
            ActionTile("View Alerts", Icons.Outlined.NotificationsNone, onOpenAlerts)
        }

        // Recent activity (last 3)
        Spacer(Modifier.size(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { SectionLabel("Recent activity") }
            Text(
                "See More",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = Amber,
                modifier = Modifier.clickable(onClick = onSeeMoreActivity),
            )
        }
        if (s.recent.isEmpty()) {
            EmptyNote("No activity yet.")
        } else {
            Column(Modifier.fillMaxWidth().groupedRowsCard()) {
                s.recent.forEachIndexed { i, log ->
                    if (i > 0) HorizontalDivider(color = Line)
                    ActivityRow(log)
                }
            }
        }
        Spacer(Modifier.size(8.dp))
    }
}

@Composable
private fun RowScope.StatCard(label: String, value: String, valueColor: Color) {
    Column(
        modifier = Modifier.weight(1f).heightIn(min = 92.dp).softCard().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp), color = valueColor)
        Text(label, style = MaterialTheme.typography.bodySmall, color = InkSoft)
    }
}

@Composable
private fun RowScope.ActionTile(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier.weight(1f).softCard().clickable(onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp)) }
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}
