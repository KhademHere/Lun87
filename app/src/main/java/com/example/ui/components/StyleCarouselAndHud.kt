package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.HeadshotAspectRatio
import com.example.model.HeadshotResolution
import com.example.model.HeadshotStylePreset
import com.example.model.StudioModelTier
import com.example.model.StudioPresets
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.DustyRose
import com.example.ui.theme.DustyRoseBorder
import com.example.ui.theme.DustyRoseMuted
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.MidnightIndigoElevated
import com.example.ui.theme.OrchidMauve
import com.example.ui.theme.PaleLavender
import com.example.ui.theme.PaleLavenderElevated
import com.example.ui.theme.StudioSpacing

@Composable
fun StylePresetCarousel(
    selectedPreset: HeadshotStylePreset,
    onSelectPreset: (HeadshotStylePreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(StudioSpacing.controlGap)
    ) {
        SectionHeader(
            title = "1. Choose Backdrop & Style",
            subtitle = selectedPreset.wardrobeCue,
            badgeText = selectedPreset.lensSpec
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(StudioSpacing.controlGap),
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
        targetValue = if (selected) DeepPlum else DustyRoseBorder,
        animationSpec = spring(),
        label = "preset_border_color"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.5.dp else 1.dp,
        animationSpec = spring(),
        label = "preset_border_width"
    )
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .width(156.dp)
            .height(120.dp)
            .clip(cardShape)
            .border(borderWidth, borderColor, cardShape)
            .brandFocusRing(cardShape)
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick)
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
            val isWarm = preset.id == "executive_warm"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = if (isWarm) {
                                listOf(DeepPlum, MidnightIndigo)
                            } else {
                                listOf(OrchidMauve, MidnightIndigoElevated)
                            }
                        )
                    )
            )
        }

        // High-contrast Midnight Indigo caption scrim guaranteeing AAA Pale Lavender legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MidnightIndigo.copy(alpha = 0.14f),
                            MidnightIndigo.copy(alpha = 0.52f),
                            MidnightIndigo.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        // Active selection indicator pill (never color-only: icon + "Active" state)
        if (selected) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DeepPlum)
                    .border(1.dp, PaleLavender, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected style",
                    tint = PaleLavender,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = PaleLavender
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = preset.lightingSpec,
                style = MaterialTheme.typography.labelSmall,
                color = DustyRose,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = preset.shortTitle,
                style = MaterialTheme.typography.titleSmall,
                color = PaleLavender,
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
    var showCustomNotes by rememberSaveable { mutableStateOf(customNotes.isNotBlank()) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(StudioSpacing.controlGap)
    ) {
        SectionHeader(
            title = "2. Quality, Ratio & Framing",
            subtitle = "${selectedTier.subtitle} · ${selectedAspectRatio.hint}",
            badgeText = "${selectedResolution.label} · ${selectedAspectRatio.label}"
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = PaleLavenderElevated,
            border = BorderStroke(1.25.dp, DustyRoseBorder)
        ) {
            Column(
                modifier = Modifier.padding(StudioSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. Sensor Engine (Studio Fast vs Studio Pro)
                Column(verticalArrangement = Arrangement.spacedBy(StudioSpacing.tight)) {
                    Text(
                        text = "Studio Engine",
                        style = MaterialTheme.typography.titleSmall,
                        color = MidnightIndigo
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(StudioSpacing.controlGap)
                    ) {
                        StudioModelTier.entries.forEach { tier ->
                            val active = tier == selectedTier
                            val pillShape = RoundedCornerShape(12.dp)
                            val containerColor by animateColorAsState(
                                targetValue = if (active) DeepPlum else DustyRoseMuted,
                                animationSpec = tween(160),
                                label = "tier_bg"
                            )
                            val contentColor by animateColorAsState(
                                targetValue = if (active) PaleLavender else MidnightIndigo,
                                animationSpec = tween(160),
                                label = "tier_fg"
                            )

                            Surface(
                                shape = pillShape,
                                color = containerColor,
                                border = BorderStroke(
                                    width = if (active) 1.5.dp else 1.dp,
                                    color = if (active) DeepPlum else DustyRoseBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = StudioSpacing.touchTarget)
                                    .clip(pillShape)
                                    .brandFocusRing(pillShape)
                                    .semantics { selected = active }
                                    .clickable(role = Role.RadioButton) { onSelectTier(tier) }
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
                                        tint = contentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = tier.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = DustyRose)

                // 2. Output Resolution (1K / 2K / 4K)
                Column(verticalArrangement = Arrangement.spacedBy(StudioSpacing.tight)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.HighQuality,
                                contentDescription = null,
                                tint = DeepPlum,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Output Resolution",
                                style = MaterialTheme.typography.titleSmall,
                                color = MidnightIndigo
                            )
                        }
                        Text(
                            text = selectedResolution.detail,
                            style = MaterialTheme.typography.labelMedium,
                            color = DeepPlum
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(StudioSpacing.tight)
                    ) {
                        HeadshotResolution.entries.forEach { res ->
                            val active = res == selectedResolution
                            val pillShape = RoundedCornerShape(12.dp)
                            val containerColor by animateColorAsState(
                                targetValue = if (active) DeepPlum else DustyRoseMuted,
                                animationSpec = tween(160),
                                label = "res_bg"
                            )
                            val contentColor by animateColorAsState(
                                targetValue = if (active) PaleLavender else MidnightIndigo,
                                animationSpec = tween(160),
                                label = "res_fg"
                            )

                            Surface(
                                shape = pillShape,
                                color = containerColor,
                                border = BorderStroke(
                                    width = if (active) 1.5.dp else 1.dp,
                                    color = if (active) DeepPlum else DustyRoseBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = StudioSpacing.touchTarget)
                                    .clip(pillShape)
                                    .brandFocusRing(pillShape)
                                    .semantics { selected = active }
                                    .clickable(role = Role.RadioButton) { onSelectResolution(res) }
                                    .testTag("resolution_${res.label}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = res.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = DustyRose)

                // 3. Aspect Ratio Selector (All 8 Ratios with accessible 48dp touch height)
                Column(verticalArrangement = Arrangement.spacedBy(StudioSpacing.tight)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aspect Ratio",
                            style = MaterialTheme.typography.titleSmall,
                            color = MidnightIndigo
                        )
                        Text(
                            text = selectedAspectRatio.hint,
                            style = MaterialTheme.typography.labelMedium,
                            color = DeepPlum
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(StudioSpacing.tight)) {
                        items(HeadshotAspectRatio.entries, key = { it.label }) { ratio ->
                            val active = ratio == selectedAspectRatio
                            val chipShape = RoundedCornerShape(12.dp)
                            val containerColor by animateColorAsState(
                                targetValue = if (active) DeepPlum else DustyRoseMuted,
                                animationSpec = tween(160),
                                label = "ratio_bg"
                            )
                            val contentColor by animateColorAsState(
                                targetValue = if (active) PaleLavender else MidnightIndigo,
                                animationSpec = tween(160),
                                label = "ratio_fg"
                            )

                            Surface(
                                shape = chipShape,
                                color = containerColor,
                                border = BorderStroke(
                                    width = if (active) 1.5.dp else 1.dp,
                                    color = if (active) DeepPlum else DustyRoseBorder
                                ),
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 58.dp, minHeight = StudioSpacing.touchTarget)
                                    .clip(chipShape)
                                    .brandFocusRing(chipShape)
                                    .semantics { selected = active }
                                    .clickable(role = Role.RadioButton) { onSelectAspectRatio(ratio) }
                                    .testTag("aspect_ratio_${ratio.label.replace(":", "_")}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ratio.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = DustyRose)

                // 4. Progressive Disclosure: Optional Wardrobe & Art Direction Notes
                Column(verticalArrangement = Arrangement.spacedBy(StudioSpacing.controlGap)) {
                    val toggleShape = RoundedCornerShape(12.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = StudioSpacing.touchTarget)
                            .clip(toggleShape)
                            .background(if (showCustomNotes || customNotes.isNotBlank()) DustyRoseMuted else Color.Transparent)
                            .clickable { showCustomNotes = !showCustomNotes }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("toggle_custom_notes_button"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                tint = DeepPlum,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Custom Styling Notes",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MidnightIndigo
                                )
                                Text(
                                    text = if (customNotes.isNotBlank()) "Custom direction active" else "Optional wardrobe, smile, or lighting cue",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeepPlum
                                )
                            }
                        }

                        Icon(
                            imageVector = if (showCustomNotes) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = if (showCustomNotes) "Collapse custom styling notes" else "Expand custom styling notes",
                            tint = MidnightIndigo,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = showCustomNotes,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        LabeledStudioTextField(
                            label = "Wardrobe & Art Direction Notes (Optional)",
                            value = customNotes,
                            onValueChange = onCustomNotesChange,
                            placeholder = "e.g., Navy blazer, subtle confident smile, warm rim light",
                            helperText = "Added directly to the photographer's studio lighting brief.",
                            singleLine = false,
                            minLines = 2,
                            maxLines = 3,
                            fieldModifier = Modifier.testTag("custom_style_notes_input")
                        )
                    }
                }
            }
        }
    }
}
