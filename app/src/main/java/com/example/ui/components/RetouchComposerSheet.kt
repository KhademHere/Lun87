package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.RetouchPresets
import com.example.ui.theme.FilmTrayCharcoal
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.StudioSlate
import com.example.ui.theme.TungstenAmber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RetouchComposerSheet(
    promptDraft: String,
    onPromptChange: (String) -> Unit,
    onApplyRetouch: (String?) -> Unit,
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
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoFixHigh,
                    contentDescription = null,
                    tint = TungstenAmber,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "Darkroom Retouch",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "PROMPT-BASED RETOUCH & BACKGROUND EDITS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ONE-TAP DARKROOM ADJUSTMENTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RetouchPresets.quickChips.forEach { chip ->
                        SuggestionChip(
                            onClick = { onPromptChange(chip.instruction) },
                            label = {
                                Text(
                                    text = chip.label,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = StudioSlate,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, HairlineSteel),
                            modifier = Modifier.testTag("retouch_chip_${chip.id}")
                        )
                    }
                }
            }

            OutlinedTextField(
                value = promptDraft,
                onValueChange = onPromptChange,
                label = { Text("Describe your retouch or background edit") },
                placeholder = {
                    Text("e.g., Swap backdrop to warm modern library, change shirt to dark navy blazer")
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TungstenAmber,
                    unfocusedBorderColor = HairlineSteel,
                    focusedContainerColor = StudioSlate,
                    unfocusedContainerColor = StudioSlate
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("retouch_prompt_input")
            )

            Button(
                onClick = { onApplyRetouch(null) },
                enabled = promptDraft.isNotBlank(),
                contentPadding = PaddingValues(vertical = 15.dp),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
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

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
