package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkroomColorScheme = darkColorScheme(
    primary = TungstenAmber,
    onPrimary = DeepInk,
    primaryContainer = TungstenContainer,
    onPrimaryContainer = OnTungstenContainer,
    secondary = SilverHalide,
    onSecondary = SilverInk,
    secondaryContainer = SilverContainer,
    onSecondaryContainer = WarmPaperWhite,
    tertiary = WarmCopper,
    onTertiary = CopperInk,
    tertiaryContainer = CopperContainer,
    onTertiaryContainer = WarmPaperWhite,
    background = ObsidianDarkroom,
    onBackground = WarmPaperWhite,
    surface = ObsidianDarkroom,
    onSurface = WarmPaperWhite,
    surfaceVariant = StudioSlate,
    onSurfaceVariant = MutedGraphite,
    surfaceContainerLowest = ObsidianDarkroom,
    surfaceContainerLow = FilmTrayCharcoal,
    surfaceContainer = StudioSlate,
    surfaceContainerHigh = LoupePanel,
    surfaceContainerHighest = ElevatedGraphite,
    outline = SubtleOutline,
    outlineVariant = HairlineSteel,
    error = SafelightCrimson,
    onError = CrimsonInk,
    errorContainer = CrimsonContainer,
    onErrorContainer = WarmPaperWhite
)

private val LumenShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun LumenStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkroomColorScheme,
        typography = LumenTypography,
        shapes = LumenShapes,
        content = content
    )
}
