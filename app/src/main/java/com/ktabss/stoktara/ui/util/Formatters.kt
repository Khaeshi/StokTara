package com.ktabss.stoktara.ui.util

import com.ktabss.stoktara.data.AuditLog
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/** 45.0 -> "45", 0.5 -> "0.5" */
fun formatQty(value: Double): String =
    BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()

/** Figma style: no space between number and unit ("45kg", "5pack"). */
fun formatQtyWithUnit(value: Double, unit: String): String = formatQty(value) + unit

/** "+45kg" / "-100mL", or null for entries without a quantity (created-without-qty, deleted, details updated). */
fun AuditLog.deltaText(): String? = delta?.let { (if (it > 0) "+" else "-") + formatQtyWithUnit(abs(it), unit) }

/** "Sep 2, 10:30 AM" */
fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(millis))
