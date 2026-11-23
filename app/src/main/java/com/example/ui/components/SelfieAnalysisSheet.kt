package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AsyncOpState
import com.example.model.SelfieAnalysisResult
import com.example.model.StudioPresets
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.DeepPlumDestructive
import com.example.ui.theme.DustyRose
import com.example.ui.theme.DustyRoseBorder
import com.example.ui.theme.DustyRoseMuted
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.OrchidMauve
import com.example.ui.theme.PaleLavender
import com.example.ui.theme.PaleLavenderElevated
import com.example.ui.theme.PaleLavenderSoft
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.tabular

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfieAnalysisSheet(
    analysisState: AsyncOpState<SelfieAnalysisResult>,
    artDirectionState: AsyncOpState<String>,
    extractedPrompts: List<String>,
    targetRoleInput: String,
    onTargetRoleChange: (String) -> Unit,
    onRetryAnalysis: () -> Unit,
    onApplyRecommendedPreset: (String) -> Unit,
    onGenerateArtDirection: () -> Unit,
    onUseArtDirectionPrompt: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var artDirectionExpanded by rememberSaveable {
        mutableStateOf(artDirectionState !is AsyncOpState.Idle)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PaleLavenderSoft,
        contentColor = MidnightIndigo
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = StudioSpacing.maxCompactContentWidth)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = StudioSpacing.screenEdge)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Sheet Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DustyRoseMuted),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = DeepPlum,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Selfie Quality & Art Director",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MidnightIndigo
                    )
                    Text(
                        text = "Lighting, framing critique & personalized style direction",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepPlum
                    )
                }
            }

            // Section 1: Multimodal Selfie Quality Analysis (gemini-4-neon)
            when (analysisState) {
                is AsyncOpState.Idle,
                is AsyncOpState.Loading,
                is AsyncOpState.Streaming -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PaleLavenderElevated,
                        border = BorderStroke(1.25.dp, DustyRoseBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(StudioSpacing.cardPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(
                                color = DeepPlum,
                                trackColor = DustyRose,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = stringResource(R.string.label_analyzing),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MidnightIndigo
                            )
                        }
                    }
                }

                is AsyncOpState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DustyRoseMuted,
                        border = BorderStroke(1.5.dp, DeepPlumDestructive),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(StudioSpacing.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = DeepPlumDestructive,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = analysisState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MidnightIndigo
                                )
                            }
                            SecondaryActionButton(
                                onClick = onRetryAnalysis,
                                modifier = Modifier.testTag("retry_analysis_button")
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Retry Analysis", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                is AsyncOpState.Success -> {
                    val data = analysisState.data
                    val recommendedPreset = StudioPresets.byId(data.recommendedPresetId)
                    val readinessLabel = when {
                        data.overallScore >= 80 -> "Studio Ready"
                        data.overallScore >= 60 -> "Good Base"
                        else -> "Tips Suggested"
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = PaleLavenderElevated,
                        border = BorderStroke(1.25.dp, DustyRoseBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(StudioSpacing.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Score Header with Icon + Text Status Badge (never color alone)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .widthIn(max = StudioSpacing.maxReadingWidth),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (data.overallScore >= 80) Icons.Rounded.CheckCircle else Icons.Rounded.Info,
                                            contentDescription = null,
                                            tint = DeepPlum,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = readinessLabel,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = DeepPlum
                                        )
                                    }
                                    Text(
                                        text = data.summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MidnightIndigo
                                    )
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(CircleShape)
                                            .background(DeepPlum),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${data.overallScore}",
                                            style = MaterialTheme.typography.headlineMedium.tabular(),
                                            color = PaleLavender
                                        )
                                    }
                                    Text(
                                        text = "out of 100",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DeepPlum
                                    )
                                }
                            }

                            // 4 Optical Metric Bars with tabular numerals
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                MetricBar("Lighting", data.lightingScore)
                                MetricBar("Framing", data.framingScore)
                                MetricBar("Sharpness", data.sharpnessScore)
                                MetricBar("Expression", data.expressionScore)
                            }

                            // Photographer Tips
                            if (data.tips.isNotEmpty()) {
                                HorizontalDivider(color = DustyRose)
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.widthIn(max = StudioSpacing.maxReadingWidth)
                                ) {
                                    Text(
                                        text = "Photographer Tips",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MidnightIndigo
                                    )
                                    data.tips.forEach { tip ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircleOutline,
                                                contentDescription = null,
                                                tint = DeepPlum,
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .size(18.dp)
                                            )
                                            Text(
                                                text = tip,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MidnightIndigo
                                            )
                                        }
                                    }
                                }
                            }

                            // Recommended Backdrop Card (Single Primary CTA inside Sheet)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = DustyRoseMuted,
                                border = BorderStroke(1.25.dp, OrchidMauve),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Recommended Style: ${recommendedPreset.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MidnightIndigo
                                    )
                                    Text(
                                        text = data.recommendedReason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DeepPlum
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    PrimaryCtaButton(
                                        onClick = { onApplyRecommendedPreset(recommendedPreset.id) },
                                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("apply_recommended_style_button")
                                    ) {
                                        Text(
                                            text = "Apply ${recommendedPreset.shortTitle}",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Progressive Disclosure for Deep Art Direction (gemini-4-carbon, ThinkingLevel.HIGH)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = PaleLavenderElevated,
                border = BorderStroke(1.25.dp, DustyRoseBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(StudioSpacing.cardPadding)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = StudioSpacing.touchTarget)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { artDirectionExpanded = !artDirectionExpanded }
                            .padding(vertical = 4.dp)
                            .testTag("toggle_art_direction_panel"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Psychology,
                                contentDescription = null,
                                tint = DeepPlum,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stringResource(R.string.action_deep_art_direction),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MidnightIndigo
                                )
                                Text(
                                    text = "In-depth wardrobe, lighting & brand consultation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeepPlum
                                )
                            }
                        }
                        Icon(
                            imageVector = if (artDirectionExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = if (artDirectionExpanded) "Collapse art director panel" else "Expand art director panel",
                            tint = MidnightIndigo
                        )
                    }

                    AnimatedVisibility(
                        visible = artDirectionExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            HorizontalDivider(color = DustyRose)

                            LabeledStudioTextField(
                                label = "Target Role or Industry (Optional)",
                                value = targetRoleInput,
                                onValueChange = onTargetRoleChange,
                                placeholder = "e.g., Product Designer, Startup Founder, Medical Director",
                                helperText = "Tailors lighting contrast, backdrop tone, and wardrobe palette.",
                                singleLine = true,
                                fieldModifier = Modifier.testTag("target_role_input")
                            )

                            SecondaryActionButton(
                                onClick = onGenerateArtDirection,
                                enabled = artDirectionState !is AsyncOpState.Loading && artDirectionState !is AsyncOpState.Streaming,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_art_direction_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Psychology,
                                    contentDescription = null,
                                    tint = DeepPlum,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Consult Senior Art Director",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            when (artDirectionState) {
                                is AsyncOpState.Idle -> Unit
                                is AsyncOpState.Loading -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(DustyRoseMuted)
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = DeepPlum,
                                            trackColor = DustyRose,
                                            strokeWidth = 2.5.dp,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.label_thinking),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MidnightIndigo
                                        )
                                    }
                                }

                                is AsyncOpState.Streaming -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = DustyRoseMuted,
                                        border = BorderStroke(1.dp, OrchidMauve),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .widthIn(max = StudioSpacing.maxReadingWidth),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Writing Art Director Brief…",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = DeepPlum
                                            )
                                            Text(
                                                text = artDirectionState.partialText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MidnightIndigo
                                            )
                                        }
                                    }
                                }

                                is AsyncOpState.Success -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = DustyRoseMuted,
                                        border = BorderStroke(1.dp, DustyRoseBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .widthIn(max = StudioSpacing.maxReadingWidth),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = artDirectionState.data,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MidnightIndigo
                                            )

                                            if (extractedPrompts.isNotEmpty()) {
                                                HorizontalDivider(color = DustyRoseBorder)
                                                Text(
                                                    text = "Tap a suggested edit to apply",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = DeepPlum
                                                )
                                                extractedPrompts.forEachIndexed { index, prompt ->
                                                    SecondaryActionButton(
                                                        onClick = { onUseArtDirectionPrompt(prompt) },
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .testTag("art_direction_prompt_$index")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.AutoFixHigh,
                                                            contentDescription = null,
                                                            tint = DeepPlum,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(
                                                            text = prompt,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MidnightIndigo,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                is AsyncOpState.Error -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = DustyRoseMuted,
                                        border = BorderStroke(1.5.dp, DeepPlumDestructive),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ErrorOutline,
                                                contentDescription = null,
                                                tint = DeepPlumDestructive,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = artDirectionState.message,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MidnightIndigo
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBar(
    label: String,
    score: Int
) {
    val statusText = when {
        score >= 80 -> "Strong"
        score >= 60 -> "Fair"
        else -> "Improve"
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MidnightIndigo
            )
            Text(
                text = "$score / 100 · $statusText",
                style = MaterialTheme.typography.labelMedium.tabular(),
                color = DeepPlum
            )
        }
        LinearProgressIndicator(
            progress = { (score / 100f).coerceIn(0f, 1f) },
            color = if (score >= 70) DeepPlum else OrchidMauve,
            trackColor = DustyRose,
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}
