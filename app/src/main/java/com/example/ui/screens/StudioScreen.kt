package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AsyncOpState
import com.example.model.HeadshotAspectRatio
import com.example.model.HeadshotResolution
import com.example.model.HeadshotStylePreset
import com.example.model.StudioModelTier
import com.example.ui.components.CameraHudControlsPanel
import com.example.ui.components.DarkroomViewport
import com.example.ui.components.PrimaryCtaButton
import com.example.ui.components.StylePresetCarousel
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.DustyRoseBorder
import com.example.ui.theme.DustyRoseMuted
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.tabular
import com.example.ui.viewmodel.StudioUiState

@Composable
fun StudioScreen(
    state: StudioUiState,
    onPickSelfie: () -> Unit,
    onTakeSelfie: () -> Unit,
    onAnalyzeSelfie: () -> Unit,
    onSelectPreset: (HeadshotStylePreset) -> Unit,
    onSelectModelTier: (StudioModelTier) -> Unit,
    onSelectAspectRatio: (HeadshotAspectRatio) -> Unit,
    onSelectResolution: (HeadshotResolution) -> Unit,
    onCustomNotesChange: (String) -> Unit,
    onDevelopHeadshot: () -> Unit,
    onToggleCompare: () -> Unit,
    onSplitChange: (Float) -> Unit,
    onOpenRetouch: () -> Unit,
    onSaveToPhotos: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCancelGeneration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBusy = state.generationState is AsyncOpState.Loading || state.retouchState is AsyncOpState.Loading

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideLayout = maxWidth >= 820.dp

        if (isWideLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = StudioSpacing.sectionGap, vertical = StudioSpacing.cardPadding),
                horizontalArrangement = Arrangement.spacedBy(StudioSpacing.macroGap)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(StudioSpacing.sectionGap)
                ) {
                    StudioHeader(state = state)
                    DarkroomViewport(
                        state = state,
                        onPickSelfie = onPickSelfie,
                        onTakeSelfie = onTakeSelfie,
                        onAnalyzeSelfie = onAnalyzeSelfie,
                        onToggleCompare = onToggleCompare,
                        onSplitChange = onSplitChange,
                        onOpenRetouch = onOpenRetouch,
                        onSaveToPhotos = onSaveToPhotos,
                        onToggleFavorite = onToggleFavorite,
                        onRetryGeneration = onDevelopHeadshot,
                        onCancelGeneration = onCancelGeneration
                    )
                    Spacer(modifier = Modifier.height(StudioSpacing.cardPadding))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(StudioSpacing.sectionGap)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    StylePresetCarousel(
                        selectedPreset = state.selectedPreset,
                        onSelectPreset = onSelectPreset
                    )
                    CameraHudControlsPanel(
                        selectedTier = state.selectedModelTier,
                        selectedAspectRatio = state.selectedAspectRatio,
                        selectedResolution = state.selectedResolution,
                        customNotes = state.customStyleNotes,
                        onSelectTier = onSelectModelTier,
                        onSelectAspectRatio = onSelectAspectRatio,
                        onSelectResolution = onSelectResolution,
                        onCustomNotesChange = onCustomNotesChange
                    )
                    DevelopSectionCta(
                        state = state,
                        enabled = state.sourceSelfie != null && !isBusy,
                        onClick = onDevelopHeadshot
                    )
                    Spacer(modifier = Modifier.height(StudioSpacing.cardPadding))
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = StudioSpacing.maxCompactContentWidth)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = StudioSpacing.screenEdge, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(StudioSpacing.sectionGap)
                ) {
                    StudioHeader(state = state)

                    DarkroomViewport(
                        state = state,
                        onPickSelfie = onPickSelfie,
                        onTakeSelfie = onTakeSelfie,
                        onAnalyzeSelfie = onAnalyzeSelfie,
                        onToggleCompare = onToggleCompare,
                        onSplitChange = onSplitChange,
                        onOpenRetouch = onOpenRetouch,
                        onSaveToPhotos = onSaveToPhotos,
                        onToggleFavorite = onToggleFavorite,
                        onRetryGeneration = onDevelopHeadshot,
                        onCancelGeneration = onCancelGeneration
                    )

                    StylePresetCarousel(
                        selectedPreset = state.selectedPreset,
                        onSelectPreset = onSelectPreset
                    )

                    CameraHudControlsPanel(
                        selectedTier = state.selectedModelTier,
                        selectedAspectRatio = state.selectedAspectRatio,
                        selectedResolution = state.selectedResolution,
                        customNotes = state.customStyleNotes,
                        onSelectTier = onSelectModelTier,
                        onSelectAspectRatio = onSelectAspectRatio,
                        onSelectResolution = onSelectResolution,
                        onCustomNotesChange = onCustomNotesChange
                    )

                    DevelopSectionCta(
                        state = state,
                        enabled = state.sourceSelfie != null && !isBusy,
                        onClick = onDevelopHeadshot
                    )

                    Spacer(modifier = Modifier.height(StudioSpacing.controlGap))
                }
            }
        }
    }
}

@Composable
private fun StudioHeader(state: StudioUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MidnightIndigo
            )
            Text(
                text = "AI Headshot Photographer",
                style = MaterialTheme.typography.bodySmall,
                color = DeepPlum
            )
        }

        Spacer(modifier = Modifier.width(StudioSpacing.controlGap))

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = DustyRoseMuted,
            border = BorderStroke(1.dp, DustyRoseBorder)
        ) {
            Text(
                text = "${state.selectedModelTier.hudBadge} · ${state.selectedAspectRatio.label} · ${state.selectedResolution.label}",
                style = MaterialTheme.typography.labelMedium.tabular(),
                color = MidnightIndigo,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun DevelopSectionCta(
    state: StudioUiState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(StudioSpacing.tight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PrimaryCtaButton(
            onClick = onClick,
            enabled = enabled,
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("develop_headshot_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.Camera,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (state.developedHeadshot == null) {
                    stringResource(R.string.action_develop_headshot)
                } else {
                    "Develop New Exposure"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "${state.selectedResolution.label} · ${state.selectedAspectRatio.label}",
                style = MaterialTheme.typography.labelMedium.tabular()
            )
        }

        if (state.sourceSelfie == null) {
            Text(
                text = "Choose or capture a selfie above to develop your headshot.",
                style = MaterialTheme.typography.bodySmall,
                color = DeepPlum,
                textAlign = TextAlign.Center
            )
        }
    }
}
