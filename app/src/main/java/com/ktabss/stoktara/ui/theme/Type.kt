package com.ktabss.stoktara.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ktabss.stoktara.R

/** Assumed from the Figma: JetBrains Mono for UI text. Swap the files in res/font if Figma says otherwise. */
val MonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
)

private fun mono(size: Int, weight: FontWeight = FontWeight.Normal, spacing: Double = 0.0) = TextStyle(
    fontFamily = MonoFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * 1.4).sp,
    letterSpacing = spacing.sp,
)

val StokTypography = Typography(
    titleLarge = mono(18, FontWeight.Medium),
    titleMedium = mono(16, FontWeight.Bold),
    titleSmall = mono(14, FontWeight.Medium),
    bodyLarge = mono(14, FontWeight.Medium),
    bodyMedium = mono(13),
    bodySmall = mono(12),
    labelLarge = mono(14, FontWeight.Medium),
    labelMedium = mono(12, spacing = 1.0),
    labelSmall = mono(11),
)
