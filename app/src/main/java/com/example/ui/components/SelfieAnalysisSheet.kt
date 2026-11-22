package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.EmeraldReady
import com.example.ui.theme.FilmTrayCharcoal
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.LoupePanel
import com.example.ui.theme.StudioSlate
import com.example.ui.theme.TungstenAmber

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FilmTrayCharcoal,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Sheet Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = TungstenAmber,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "Selfie Loupe & Art Director",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "VISION CRITIQUE · HIGH-THINKING STRATEGY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = StudioSlate,
                        border = BorderStroke(1.dp, HairlineSteel),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(
                                color = TungstenAmber,
                                trackColor = HairlineSteel,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = stringResource(R.string.label_analyzing),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                is AsyncOpState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = analysisState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            OutlinedButton(
                                onClick = onRetryAnalysis,
                                modifier = Modifier.testTag("retry_analysis_button")
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Retry Analysis")
                            }
                        }
                    }
                }

                is AsyncOpState.Success -> {
                    val data = analysisState.data
                    val recommendedPreset = StudioPresets.byId(data.recommendedPresetId)

                    // Readiness Score Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = StudioSlate,
                        border = BorderStroke(1.dp, HairlineSteel),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "STUDIO READINESS",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = data.summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(TungstenAmber.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${data.overallScore}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = if (data.overallScore >= 80) EmeraldReady else TungstenAmber
                                    )
                                }
                            }

                            // 4 Optical Metric Bars
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                MetricBar("LIGHTING", data.lightingScore)
                                MetricBar("FRAMING", data.framingScore)
                                MetricBar("SHARPNESS", data.sharpnessScore)
                                MetricBar("EXPRESSION", data.expressionScore)
                            }

                            // Actionable Photographer Tips
                            if (data.tips.isNotEmpty()) {
                                HorizontalDivider(color = HairlineSteel)
                                Text(
                                    text = "PHOTOGRAPHER TIPS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    data.tips.forEach { tip ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircleOutline,
                                                contentDescription = null,
                                                tint = TungstenAmber,
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .size(16.dp)
                                            )
                                            Text(
                                                text = tip,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // Recommended Style Preset Banner
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LoupePanel,
                                border = BorderStroke(1.dp, TungstenAmber.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "RECOMMENDED STYLE · ${recommendedPreset.title.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TungstenAmber
                                    )
                                    Text(
                                        text = data.recommendedReason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Button(
                                        onClick = { onApplyRecommendedPreset(recommendedPreset.id) },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                        modifier = Modifier.testTag("apply_recommended_style_button")
                                    ) {
                                        Text("Apply ${recommendedPreset.shortTitle}")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = HairlineSteel)

            // Section 2: Deep Art Direction (gemini-4-carbon with ThinkingLevel.HIGH)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Psychology,
                        contentDescription = null,
                        tint = TungstenAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.action_deep_art_direction),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "HIGH-THINKING WARDROBE, LIGHTING & BRAND BRIEF",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = targetRoleInput,
                    onValueChange = onTargetRoleChange,
                    label = { Text("Target role or industry (optional)") },
                    placeholder = { Text("e.g., Staff Product Designer, Startup Founder, Surgeon") },
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
                        .testTag("target_role_input")
                )

                FilledTonalButton(
                    onClick = onGenerateArtDirection,
                    enabled = artDirectionState !is AsyncOpState.Loading && artDirectionState !is AsyncOpState.Streaming,
                    contentPadding = PaddingValues(vertical = 13.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_art_direction_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Consult Senior Art Director")
                }

                when (artDirectionState) {
                    is AsyncOpState.Idle -> Unit
                    is AsyncOpState.Loading -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = StudioSlate,
                            border = BorderStroke(1.dp, HairlineSteel),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = TungstenAmber,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = stringResource(R.string.label_thinking),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    is AsyncOpState.Streaming -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = StudioSlate,
                            border = BorderStroke(1.dp, TungstenAmber.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "ART DIRECTOR BRIEF · LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TungstenAmber
                                )
                                Text(
                                    text = artDirectionState.partialText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    is AsyncOpState.Success -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = StudioSlate,
                            border = BorderStroke(1.dp, HairlineSteel),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = artDirectionState.data,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (extractedPrompts.isNotEmpty()) {
                                    HorizontalDivider(color = HairlineSteel)
                                    Text(
                                        text = "TAP TO APPLY DIRECTION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TungstenAmber
                                    )
                                    extractedPrompts.forEachIndexed { index, prompt ->
                                        OutlinedButton(
                                            onClick = { onUseArtDirectionPrompt(prompt) },
                                            border = BorderStroke(1.dp, TungstenAmber.copy(alpha = 0.4f)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("art_direction_prompt_$index")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AutoFixHigh,
                                                contentDescription = null,
                                                tint = TungstenAmber,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = prompt,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
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
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = artDirectionState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(14.dp)
                            )
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "$score / 100",
                style = MaterialTheme.typography.labelSmall,
                color = if (score >= 80) EmeraldReady else TungstenAmber
            )
        }
        LinearProgressIndicator(
            progress = { (score / 100f).coerceIn(0f, 1f) },
            color = if (score >= 80) EmeraldReady else TungstenAmber,
            trackColor = HairlineSteel,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}
