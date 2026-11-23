package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Mandatory Five-Color Brand Palette
// =============================================================================
val PaleLavender = Color(0xFFF2D7EE)      // Primary light canvas, elevated surfaces, light ink on dark
val DustyRose = Color(0xFFD3BCC0)         // Secondary neutral fills, dividers, inactive tracks, hairlines
val OrchidMauve = Color(0xFFA5668B)       // Secondary accents, focus indicators, badges, active icons
val DeepPlum = Color(0xFF69306D)          // Primary CTA fill, active nav indicators, strong visual anchors
val MidnightIndigo = Color(0xFF0E103D)    // High-contrast ink for headings, body text, strong icons & dark mat

// =============================================================================
// Subtle Tint / Shade Derivatives (Strictly derived for elevation, borders & states)
// =============================================================================
val PaleLavenderElevated = Color(0xFFFAF0F8)   // Lifted card surface on Pale Lavender canvas (16.3:1 with MidnightIndigo)
val PaleLavenderSoft = Color(0xFFF6E4F3)       // Modal sheet & navigation bar fill
val DustyRoseMuted = Color(0xFFE4D2D8)         // Soft container tint for inputs & secondary chips
val DustyRoseBorder = Color(0xFFC2A8AE)        // Crisp hairline border derivative of Dusty Rose
val DeepPlumPressed = Color(0xFF532456)        // Pressed/active derivative of Deep Plum
val DeepPlumDestructive = Color(0xFF4B194E)    // Distinct deep-shade anchor for destructive confirmation actions
val MidnightIndigoElevated = Color(0xFF1A1D52) // Elevated surface inside dark photographic viewfinders
val OrchidMauveTint = Color(0x26A5668B)        // 15% Orchid Mauve tint for subtle active badges/halos
