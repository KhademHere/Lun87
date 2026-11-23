package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AsyncOpState
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
import com.example.ui.viewmodel.StudioUiState

@Composable
fun DarkroomViewport(
    state: StudioUiState,
    onPickSelfie: () -> Unit,
    onTakeSelfie: () -> Unit,
    onAnalyzeSelfie: () -> Unit,
    onToggleCompare: () -> Unit,
    onSplitChange: (Float) -> Unit,
    onOpenRetouch: () -> Unit,
    onSaveToPhotos: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetryGeneration: () -> Unit,
    onCancelGeneration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBusy = state.generationState is AsyncOpState.Loading || state.retouchState is AsyncOpState.Loading
    val activeError = (state.generationState as? AsyncOpState.Error)?.message
        ?: (state.retouchState as? AsyncOpState.Error)?.message
    val hasPhoto = state.sourceSelfie != null || state.developedHeadshot != null

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(StudioSpacing.controlGap)
    ) {
        // Portrait Stage Frame — Pale Lavender Elevated when welcoming, Midnight Indigo gallery mat when framing a photo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(
                    ratio = state.selectedAspectRatio.ratioValue.coerceIn(0.72f, 1.55f)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(
                    if (hasPhoto) MidnightIndigo else PaleLavenderElevated
                )
                .border(
                    width = 1.5.dp,
                    color = if (hasPhoto) OrchidMauve.copy(alpha = 0.55f) else DustyRoseBorder,
                    shape = RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.developedHeadshot != null && state.sourceSelfie != null && state.isComparing -> {
                    BeforeAfterSlider(
                        beforeBitmap = state.sourceSelfie,
                        afterBitmap = state.developedHeadshot,
                        splitFraction = state.compareFraction,
                        onSplitFractionChange = onSplitChange,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                state.developedHeadshot != null -> {
                    val image = remember(state.developedHeadshot) { state.developedHeadshot.asImageBitmap() }
                    Image(
                        bitmap = image,
                        contentDescription = "Developed professional headshot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                state.sourceSelfie != null -> {
                    val image = remember(state.sourceSelfie) { state.sourceSelfie.asImageBitmap() }
                    Image(
                        bitmap = image,
                        contentDescription = "Uploaded casual selfie",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = MidnightIndigo.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, OrchidMauve),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "SOURCE SELFIE · READY",
                            style = MaterialTheme.typography.labelSmall,
                            color = PaleLavender,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                else -> {
                    ZeroStateWelcomeStage(
                        onPickSelfie = onPickSelfie,
                        onTakeSelfie = onTakeSelfie
                    )
                }
            }

            // Precision viewfinder corner marks on empty or source state
            if (state.developedHeadshot == null) {
                ViewfinderCornerMarks(
                    color = if (hasPhoto) PaleLavender.copy(alpha = 0.72f) else OrchidMauve.copy(alpha = 0.65f),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            // Accessible Favorite Toggle on Developed Portrait
            if (state.developedHeadshot != null && state.activeRecord != null && !isBusy) {
                val isFav = state.activeRecord.isFavorite
                Surface(
                    shape = CircleShape,
                    color = if (isFav) DeepPlum else MidnightIndigo.copy(alpha = 0.85f),
                    border = BorderStroke(1.5.dp, PaleLavender.copy(alpha = 0.75f)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(StudioSpacing.touchTarget)
                            .testTag("favorite_active_button")
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFav) "Remove headshot from favorites" else "Save headshot to favorites",
                            tint = PaleLavender
                        )
                    }
                }
            }

            // Developing Progress Veil (high-contrast Midnight Indigo + Pale Lavender)
            androidx.compose.animation.AnimatedVisibility(
                visible = isBusy,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                StudioDevelopingOverlay(
                    isRetouch = state.retouchState is AsyncOpState.Loading,
                    specLine = "${state.selectedModelTier.label} · ${state.selectedAspectRatio.label} · ${state.selectedResolution.label}",
                    onCancel = onCancelGeneration
                )
            }

            // Transparent Error State with Icon + Retry
            androidx.compose.animation.AnimatedVisibility(
                visible = !isBusy && activeError != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (activeError != null) {
                    StudioErrorOverlay(
                        message = activeError,
                        onRetry = onRetryGeneration
                    )
                }
            }
        }

        // Ergonomic Action Toolbars beneath viewport once a selfie is loaded
        if (state.sourceSelfie != null) {
            if (state.developedHeadshot != null) {
                // Result Controls: Compare · Retouch · Save to Photos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(StudioSpacing.tight)
                ) {
                    SecondaryActionButton(
                        onClick = onToggleCompare,
                        enabled = !isBusy,
                        selected = state.isComparing,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("compare_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Compare,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (state.isComparing) "Comparing" else stringResource(R.string.action_compare),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    SecondaryActionButton(
                        onClick = onOpenRetouch,
                        enabled = !isBusy,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("retouch_open_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.action_retouch),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    SecondaryActionButton(
                        onClick = onSaveToPhotos,
                        enabled = !isBusy && !state.isExporting,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_to_photos_button")
                    ) {
                        if (state.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = DeepPlum
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Source Utility Bar: Analyze Selfie + Replace Photo / Retake Camera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(StudioSpacing.controlGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecondaryActionButton(
                    onClick = onAnalyzeSelfie,
                    enabled = !isBusy,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("analyze_selfie_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = DeepPlum,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.action_analyze_selfie),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PaleLavenderElevated,
                    border = BorderStroke(1.25.dp, DustyRoseBorder)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onPickSelfie,
                            enabled = !isBusy,
                            modifier = Modifier
                                .size(StudioSpacing.touchTarget)
                                .testTag("replace_gallery_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoLibrary,
                                contentDescription = "Choose a different selfie from Photos",
                                tint = MidnightIndigo
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(DustyRoseBorder)
                        )
                        IconButton(
                            onClick = onTakeSelfie,
                            enabled = !isBusy,
                            modifier = Modifier
                                .size(StudioSpacing.touchTarget)
                                .testTag("replace_camera_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = "Take a new selfie with camera",
                                tint = MidnightIndigo
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZeroStateWelcomeStage(
    onPickSelfie: () -> Unit,
    onTakeSelfie: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(DustyRoseMuted)
                .border(1.5.dp, OrchidMauve, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = null,
                tint = DeepPlum,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Start with a casual selfie",
            style = MaterialTheme.typography.headlineMedium,
            color = MidnightIndigo,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pick a photo from your library or capture one now to develop studio-lit headshots up to 4K.",
            style = MaterialTheme.typography.bodyMedium,
            color = DeepPlum,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 360.dp)
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Clear Button Hierarchy: One Primary CTA (#69306D) + One Secondary Action
        Row(
            horizontalArrangement = Arrangement.spacedBy(StudioSpacing.controlGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrimaryCtaButton(
                onClick = onPickSelfie,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                modifier = Modifier.testTag("choose_selfie_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_choose_selfie),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            SecondaryActionButton(
                onClick = onTakeSelfie,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                modifier = Modifier.testTag("take_selfie_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = DeepPlum,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_take_selfie),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ViewfinderCornerMarks(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val len = 18.dp.toPx()
        val stroke = 2.dp.toPx()
        val w = size.width
        val h = size.height

        drawLine(color, Offset(0f, 0f), Offset(len, 0f), stroke)
        drawLine(color, Offset(0f, 0f), Offset(0f, len), stroke)

        drawLine(color, Offset(w, 0f), Offset(w - len, 0f), stroke)
        drawLine(color, Offset(w, 0f), Offset(w, len), stroke)

        drawLine(color, Offset(0f, h), Offset(len, h), stroke)
        drawLine(color, Offset(0f, h), Offset(0f, h - len), stroke)

        drawLine(color, Offset(w, h), Offset(w - len, h), stroke)
        drawLine(color, Offset(w, h), Offset(w, h - len), stroke)
    }
}

@Composable
private fun StudioDevelopingOverlay(
    isRetouch: Boolean,
    specLine: String,
    onCancel: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "studio_sweep")
    val sweep by infinite.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_pos"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightIndigo.copy(alpha = 0.90f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = size.height * sweep
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        OrchidMauve.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    startY = y - 140f,
                    endY = y + 140f
                )
            )
        }

        Column(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = PaleLavender,
                trackColor = MidnightIndigoElevated,
                strokeWidth = 3.dp,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = stringResource(if (isRetouch) R.string.label_retouching else R.string.label_developing),
                style = MaterialTheme.typography.titleLarge,
                color = PaleLavender,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DeepPlum
            ) {
                Text(
                    text = specLine,
                    style = MaterialTheme.typography.labelMedium,
                    color = PaleLavender,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.label_developing_hint),
                style = MaterialTheme.typography.bodySmall,
                color = DustyRose,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag("cancel_generation_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = PaleLavender,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    color = PaleLavender
                )
            }
        }
    }
}

@Composable
private fun StudioErrorOverlay(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightIndigo.copy(alpha = 0.92f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(DeepPlum),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = PaleLavender,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Studio Exposure Interrupted",
                style = MaterialTheme.typography.titleLarge,
                color = PaleLavender,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = DustyRose,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryCtaButton(
                onClick = onRetry,
                modifier = Modifier.testTag("retry_generation_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Retry Exposure", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
