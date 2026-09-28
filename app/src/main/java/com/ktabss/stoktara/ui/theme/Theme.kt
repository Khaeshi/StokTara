package com.ktabss.stoktara.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StokColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Amber,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    onSurfaceVariant = InkSoft,
    error = Danger,
    outline = Line,
)

/** Light-only on purpose: the Figma has a single light palette. */
@Composable
fun StokTaraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = StokColors, typography = StokTypography, content = content)
}
