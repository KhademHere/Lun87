package com.example.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Modular 4dp/8dp spacing tokens enforcing Law of Proximity, generous card padding,
 * macro section separation, and accessible touch ergonomics across all viewports.
 */
object StudioSpacing {
    val micro = 4.dp          // Tight label-to-input or icon-to-badge coupling
    val tight = 8.dp          // Paired controls & compact chip gaps
    val controlGap = 12.dp    // Safe inter-control gap between adjacent buttons
    val cardPadding = 18.dp   // Generous internal card & sheet padding
    val screenEdge = 20.dp    // Clean responsive horizontal screen gutter
    val sectionGap = 24.dp    // Macro whitespace between distinct page sections
    val macroGap = 32.dp      // Large breathing room between major regions
    val touchTarget = 48.dp   // Standard accessible minimum hit area (Fitts's Law)
    val primaryButtonHeight = 54.dp
    val maxReadingWidth = 560.dp
    val maxCompactContentWidth = 640.dp
}
