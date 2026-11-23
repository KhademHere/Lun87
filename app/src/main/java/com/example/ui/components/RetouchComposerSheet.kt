package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.RetouchPresets
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.DustyRose
import com.example.ui.theme.DustyRoseBorder
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.PaleLavenderElevated
import com.example.ui.theme.PaleLavenderSoft
import com.example.ui.theme.StudioSpacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RetouchComposerSheet(
    promptDraft: String,
    onPromptChange: (String) -> Unit,
    onApplyRetouch: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showEmptyError by remember { mutableStateOf(false) }

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
            // Sheet Title & Context
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoFixHigh,
                    contentDescription = null,
                    tint = DeepPlum,
                    modifier = Modifier.size(24.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Retouch Headshot",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MidnightIndigo
                    )
                    Text(
                        text = "Refine lighting, wardrobe, or backdrop while preserving identity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepPlum
                    )
                }
            }

            // One-Tap Quick Adjustments
            Column(verticalArrangement = Arrangement.spacedBy(StudioSpacing.tight)) {
                Text(
                    text = "Quick Adjustments",
                    style = MaterialTheme.typography.titleSmall,
                    color = MidnightIndigo
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(StudioSpacing.tight),
                    verticalArrangement = Arrangement.spacedBy(StudioSpacing.tight)
                ) {
                    RetouchPresets.quickChips.forEach { chip ->
                        SuggestionChip(
                            onClick = {
                                showEmptyError = false
                                onPromptChange(chip.instruction)
                            },
                            label = {
                                Text(
                                    text = chip.label,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = PaleLavenderElevated,
                                labelColor = MidnightIndigo
                            ),
                            border = BorderStroke(1.25.dp, DustyRoseBorder),
                            modifier = Modifier
                                .defaultMinSize(minHeight = StudioSpacing.touchTarget)
                                .testTag("retouch_chip_${chip.id}")
                        )
                    }
                }
            }

            HorizontalDivider(color = DustyRose)

            // Persistent-Label Input with Inline Validation
            LabeledStudioTextField(
                label = "Retouch Instruction",
                value = promptDraft,
                onValueChange = {
                    if (showEmptyError && it.isNotBlank()) showEmptyError = false
                    onPromptChange(it)
                },
                placeholder = "e.g., Swap backdrop to warm modern library and tailor blazer to charcoal wool",
                helperText = "Tap a quick adjustment above or write your own custom darkroom edit.",
                errorText = if (showEmptyError) "Enter a retouch instruction or select a quick adjustment first." else null,
                minLines = 3,
                maxLines = 5,
                fieldModifier = Modifier.testTag("retouch_prompt_input")
            )

            // Predictable Confirmation Alignment: Cancel (Tertiary Left) · Apply Retouch (Primary CTA Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(StudioSpacing.controlGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TertiaryActionButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_retouch_sheet_button")
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }

                PrimaryCtaButton(
                    onClick = {
                        if (promptDraft.isBlank()) {
                            showEmptyError = true
                        } else {
                            onApplyRetouch(null)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("apply_retouch_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.action_apply_retouch),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
