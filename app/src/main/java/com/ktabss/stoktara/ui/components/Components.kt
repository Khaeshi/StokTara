package com.ktabss.stoktara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ktabss.stoktara.data.AuditLog
import com.ktabss.stoktara.data.StockStatus
import com.ktabss.stoktara.ui.theme.Amber
import com.ktabss.stoktara.ui.theme.Danger
import com.ktabss.stoktara.ui.theme.InkSoft
import com.ktabss.stoktara.ui.theme.Line
import com.ktabss.stoktara.ui.theme.Primary
import com.ktabss.stoktara.ui.theme.RowFill
import com.ktabss.stoktara.ui.util.deltaText
import com.ktabss.stoktara.ui.util.formatTimestamp

/** Rounded bordered card used for stat cards, action tiles, etc. */
fun Modifier.softCard(
    borderColor: Color = Line,
    fill: Color = Color.White.copy(alpha = 0.25f),
): Modifier {
    val shape = RoundedCornerShape(16.dp)
    return this.background(fill, shape).border(1.dp, borderColor, shape).clip(shape)
}

/** The amber-outlined container that wraps grouped list rows (Recent Activity, Alerts). */
fun Modifier.groupedRowsCard(): Modifier =
    softCard(borderColor = Amber.copy(alpha = 0.2f), fill = Color.Transparent)
        .padding(8.dp)
        .clip(RoundedCornerShape(10.dp))

/** Page title on the left, optional back button on the RIGHT (that's how the Figma has it). */
@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)? = null) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            if (onBack != null) IconSquareButton(Icons.Outlined.ChevronLeft, "Back", onBack)
        }
        HorizontalDivider(color = Line)
    }
}

@Composable
fun IconSquareButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(40.dp)
            .background(Color.White.copy(alpha = 0.35f), shape)
            .border(1.dp, Line, shape)
            .clip(shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = InkSoft, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = InkSoft)
}

@Composable
fun StatusPill(status: StockStatus) {
    val (label, color) = when (status) {
        StockStatus.OK -> "OK" to Primary
        StockStatus.LOW -> "Low" to Amber
        StockStatus.OUT -> "Out" to Danger
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

@Composable
fun EmptyNote(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = InkSoft,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
    )
}

/** One audit-trail line: "Stock In | All-Purpose Flour", actor + time underneath, delta on the right. */
@Composable
fun ActivityRow(log: AuditLog, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().background(RowFill).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(Primary))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "${log.action.label} | ${log.itemName}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${log.actorLabel} - ${formatTimestamp(log.timestamp)}",
                style = MaterialTheme.typography.labelSmall,
                color = InkSoft,
            )
        }
        log.deltaText()?.let {
            Spacer(Modifier.width(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}
