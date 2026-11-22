package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.HeadshotAspectRatio
import com.example.model.HeadshotResolution
import com.example.model.HeadshotStylePreset
import com.example.model.StudioModelTier
import com.example.model.StudioPresets
import com.example.ui.theme.FilmTrayCharcoal
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.ObsidianDarkroom
import com.example.ui.theme.StudioSlate
import com.example.ui.theme.TungstenAmber

@Composable
fun StylePresetCarousel(
    selectedPreset: HeadshotStylePreset,
    onSelectPreset: (HeadshotStylePreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BACKDROP & LIGHTING",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = selectedPreset.lensSpec,
                style = MaterialTheme.typography.labelSmall,
                color = TungstenAmber
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(StudioPresets.all, key = { it.id }) { preset ->
                StylePresetCard(
                    preset = preset,
                    selected = preset.id == selectedPreset.id,
                    onClick = { onSelectPreset(preset) }
                )
            }
        }
    }
}

@Composable
private fun StylePresetCard(
    preset: HeadshotStylePreset,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) TungstenAmber else HairlineSteel,
        animationSpec = spring(),
        label = "preset_border_color"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.dp else 1.dp,
        animationSpec = spring(),
        label = "preset_border_width"
    )

    Box(
        modifier = Modifier
            .width(146.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("style_preset_${preset.id}")
    ) {
        if (preset.thumbnailRes != null) {
            Image(
                painter = painterResource(id = preset.thumbnailRes),
                contentDescription = preset.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(preset.fallbackGradientHexStart),
                                Color(preset.fallbackGradientHexEnd)
                            )
                        )
                    )
            )
        }

        // Scrim for legible studio caption
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.12f),
                            ObsidianDarkroom.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(TungstenAmber),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = ObsidianDarkroom,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = preset.lightingSpec,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) TungstenAmber else MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = preset.shortTitle,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CameraHudControlsPanel(
    selectedTier: StudioModelTier,
    selectedAspectRatio: HeadshotAspectRatio,
    selectedResolution: HeadshotResolution,
    customNotes: String,
    onSelectTier: (StudioModelTier) -> Unit,
    onSelectAspectRatio: (HeadshotAspectRatio) -> Unit,
    onSelectResolution: (HeadshotResolution) -> Unit,
    onCustomNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = FilmTrayCharcoal,
        border = BorderStroke(1.dp, HairlineSteel)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Model Quality Tier (Fast Studio vs Pro Studio)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SENSOR ENGINE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedTier.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioModelTier.entries.forEach { tier ->
                        val active = tier == selectedTier
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) MaterialTheme.colorScheme.primaryContainer else StudioSlate,
                            border = BorderStroke(
                                width = if (active) 1.5.dp else 1.dp,
                                color = if (active) TungstenAmber else HairlineSteel
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectTier(tier) }
                                .testTag("model_tier_${tier.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (tier == StudioModelTier.PRO_STUDIO) Icons.Rounded.AutoAwesome else Icons.Rounded.FlashOn,
                                    contentDescription = null,
                                    tint = if (active) TungstenAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = tier.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (active) TungstenAmber else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 2. Output Resolution (1K / 2K / 4K)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.HighQuality,
                            contentDescription = null,
                            tint = TungstenAmber,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "RESOLUTION",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = selectedResolution.detail,
                        style = MaterialTheme.typography.labelSmall,
                        color = TungstenAmber
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeadshotResolution.entries.forEach { res ->
                        val active = res == selectedResolution
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) MaterialTheme.colorScheme.primaryContainer else StudioSlate,
                            border = BorderStroke(
                                width = if (active) 1.5.dp else 1.dp,
                                color = if (active) TungstenAmber else HairlineSteel
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectResolution(res) }
                                .testTag("resolution_${res.label}")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = res.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (active) TungstenAmber else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 3. Aspect Ratio Selector (All 8 Ratios)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FRAME RATIO",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedAspectRatio.hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HeadshotAspectRatio.entries, key = { it.label }) { ratio ->
                        val active = ratio == selectedAspectRatio
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) MaterialTheme.colorScheme.primaryContainer else StudioSlate,
                            border = BorderStroke(
                                width = if (active) 1.5.dp else 1.dp,
                                color = if (active) TungstenAmber else HairlineSteel
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectAspectRatio(ratio) }
                                .testTag("aspect_ratio_${ratio.label.replace(":", "_")}")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ratio.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (active) TungstenAmber else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 4. Optional Wardrobe / Mood Notes
            OutlinedTextField(
                value = customNotes,
                onValueChange = onCustomNotesChange,
                label = { Text("Wardrobe or art direction notes (optional)") },
                placeholder = { Text("e.g., Navy blazer, warm rim light, subtle confident smile") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TungstenAmber,
                    unfocusedBorderColor = HairlineSteel,
                    focusedContainerColor = StudioSlate,
                    unfocusedContainerColor = StudioSlate
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_style_notes_input")
            )
        }
    }
}
