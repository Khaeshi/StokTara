package com.ktabss.stoktara.ui.theme

import androidx.compose.ui.graphics.Color

// Tokens read from the Figma Inspect panel.
val Paper = Color(0xFFF3F1E7)       // screen background
val Line = Color(0xFFDCD8C6)        // hairlines / card borders
val Ink = Color(0xFF000000)
val InkSoft = Color(0xFF656463)
val InkFaint = Color(0xFFAEACAC)
val Primary = Color(0xFF2F5B3C)     // green
val Amber = Color(0xFFB5651D)       // low stock / accents
val Danger = Color(0xFF9C3B3B)      // out of stock / destructive
val Placeholder = Color(0xFFD9D9D9) // image placeholders
val RowFill = Placeholder.copy(alpha = 0.5f)

// NOT in the palette panel: matched by eye from a screenshot of the restock banner.
// Replace with the exact hex from Inspect.
val BannerFill = Color(0xFFE9BE94)
