package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// WCAG AA / AAA light editorial scheme mapped strictly to the five-color brand palette
private val BrandLightColorScheme = lightColorScheme(
    primary = DeepPlum,
    onPrimary = PaleLavender,
    primaryContainer = DustyRoseMuted,
    onPrimaryContainer = MidnightIndigo,
    secondary = OrchidMauve,
    onSecondary = PaleLavender,
    secondaryContainer = DustyRose,
    onSecondaryContainer = MidnightIndigo,
    tertiary = MidnightIndigo,
    onTertiary = PaleLavender,
    tertiaryContainer = DustyRoseMuted,
    onTertiaryContainer = MidnightIndigo,
    background = PaleLavender,
    onBackground = MidnightIndigo,
    surface = PaleLavender,
    onSurface = MidnightIndigo,
    surfaceVariant = DustyRose,
    onSurfaceVariant = DeepPlum,
    surfaceContainerLowest = PaleLavenderElevated,
    surfaceContainerLow = PaleLavenderSoft,
    surfaceContainer = DustyRoseMuted,
    surfaceContainerHigh = DustyRose,
    surfaceContainerHighest = DustyRoseBorder,
    outline = OrchidMauve,
    outlineVariant = DustyRoseBorder,
    error = DeepPlumDestructive,
    onError = PaleLavender,
    errorContainer = DustyRoseMuted,
    onErrorContainer = MidnightIndigo
)

private val LumenShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun LumenStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrandLightColorScheme,
        typography = LumenTypography,
        shapes = LumenShapes,
        content = content
    )
}
