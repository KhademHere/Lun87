package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.FilmTrayCharcoal
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.ObsidianDarkroom
import com.example.ui.theme.StudioSlate
import com.example.ui.theme.TungstenAmber
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

    Column(modifier = modifier.fillMaxWidth()) {
        // Main Portrait Frame Stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(
                    ratio = state.selectedAspectRatio.ratioValue.coerceIn(0.68f, 1.65f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            TungstenAmber.copy(alpha = 0.08f),
                            FilmTrayCharcoal,
                            ObsidianDarkroom
                        ),
                        radius = 750f
                    )
                )
                .border(1.dp, HairlineSteel, RoundedCornerShape(20.dp)),
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
                    // Subtle top HUD badge showing selfie loaded
                    Surface(
                        color = ObsidianDarkroom.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HairlineSteel),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "SOURCE SELFIE · READY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                else -> {
                    ZeroStateViewfinder(
                        onPickSelfie = onPickSelfie,
                        onTakeSelfie = onTakeSelfie
                    )
                }
            }

            // Viewfinder Precision Corner Reticle (shown when empty or source preview only)
            if (state.developedHeadshot == null) {
                ViewfinderCornerMarks(
                    color = TungstenAmber.copy(alpha = if (state.sourceSelfie == null) 0.45f else 0.75f),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                )
            }

            // Favorite Floating Icon on Developed Portrait
            if (state.developedHeadshot != null && state.activeRecord != null && !isBusy) {
                val isFav = state.activeRecord.isFavorite
                Surface(
                    shape = CircleShape,
                    color = ObsidianDarkroom.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, HairlineSteel),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("favorite_active_button")
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFav) "Remove from favorites" else "Mark as favorite",
                            tint = if (isFav) TungstenAmber else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Darkroom Developing Shimmer Overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = isBusy,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                DarkroomDevelopingOverlay(
                    isRetouch = state.retouchState is AsyncOpState.Loading,
                    specLine = "${state.selectedModelTier.hudBadge} · ${state.selectedAspectRatio.label} · ${state.selectedResolution.label}",
                    onCancel = onCancelGeneration
                )
            }

            // Transparent Error Overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = !isBusy && activeError != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (activeError != null) {
                    DarkroomErrorOverlay(
                        message = activeError,
                        onRetry = onRetryGeneration
                    )
                }
            }
        }

        // Action Bar below Viewport when a selfie is loaded
        if (state.sourceSelfie != null) {
            Spacer(modifier = Modifier.height(12.dp))
            if (state.developedHeadshot != null) {
                // Result Action Strip: Compare · Retouch · Save to Photos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onToggleCompare,
                        enabled = !isBusy,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (state.isComparing) MaterialTheme.colorScheme.primaryContainer else StudioSlate,
                            contentColor = if (state.isComparing) TungstenAmber else MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
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
                            text = stringResource(R.string.action_compare),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    FilledTonalButton(
                        onClick = onOpenRetouch,
                        enabled = !isBusy,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = StudioSlate,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
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

                    FilledTonalButton(
                        onClick = onSaveToPhotos,
                        enabled = !isBusy && !state.isExporting,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = StudioSlate,
                            contentColor = TungstenAmber
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_to_photos_button")
                    ) {
                        if (state.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = TungstenAmber
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
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Source Utility Strip: Analyze Selfie + Replace Photo / Camera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAnalyzeSelfie,
                    enabled = !isBusy,
                    border = BorderStroke(1.dp, TungstenAmber.copy(alpha = 0.55f)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("analyze_selfie_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = TungstenAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.action_analyze_selfie),
                        color = TungstenAmber,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StudioSlate,
                    border = BorderStroke(1.dp, HairlineSteel)
                ) {
                    Row {
                        IconButton(
                            onClick = onPickSelfie,
                            enabled = !isBusy,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("replace_gallery_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoLibrary,
                                contentDescription = "Choose another selfie from Photos",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(
                            onClick = onTakeSelfie,
                            enabled = !isBusy,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("replace_camera_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = "Capture a new selfie with camera",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZeroStateViewfinder(
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
                .size(68.dp)
                .clip(CircleShape)
                .background(TungstenAmber.copy(alpha = 0.12f))
                .border(1.dp, TungstenAmber.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = null,
                tint = TungstenAmber,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Step into the darkroom",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Upload a casual selfie or snap one in-app to develop studio-lit professional headshots up to 4K.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
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
                Text(stringResource(R.string.action_choose_selfie))
            }

            OutlinedButton(
                onClick = onTakeSelfie,
                border = BorderStroke(1.dp, TungstenAmber.copy(alpha = 0.6f)),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                modifier = Modifier.testTag("take_selfie_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = TungstenAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_take_selfie),
                    color = TungstenAmber
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
        val stroke = 1.6.dp.toPx()
        val w = size.width
        val h = size.height

        // Top-left
        drawLine(color, Offset(0f, 0f), Offset(len, 0f), stroke)
        drawLine(color, Offset(0f, 0f), Offset(0f, len), stroke)
        // Top-right
        drawLine(color, Offset(w, 0f), Offset(w - len, 0f), stroke)
        drawLine(color, Offset(w, 0f), Offset(w, len), stroke)
        // Bottom-left
        drawLine(color, Offset(0f, h), Offset(len, h), stroke)
        drawLine(color, Offset(0f, h), Offset(0f, h - len), stroke)
        // Bottom-right
        drawLine(color, Offset(w, h), Offset(w - len, h), stroke)
        drawLine(color, Offset(w, h), Offset(w, h - len), stroke)
    }
}

@Composable
private fun DarkroomDevelopingOverlay(
    isRetouch: Boolean,
    specLine: String,
    onCancel: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "darkroom_sweep")
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
            .background(ObsidianDarkroom.copy(alpha = 0.84f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = size.height * sweep
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        TungstenAmber.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    startY = y - 140f,
                    endY = y + 140f
                )
            )
        }

        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = TungstenAmber,
                trackColor = HairlineSteel,
                strokeWidth = 3.dp,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(if (isRetouch) R.string.label_retouching else R.string.label_developing),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = specLine,
                style = MaterialTheme.typography.labelMedium,
                color = TungstenAmber
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.label_developing_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag("cancel_generation_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun DarkroomErrorOverlay(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDarkroom.copy(alpha = 0.88f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(38.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Studio Exposure Interrupted",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.testTag("retry_generation_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Retry Exposure")
            }
        }
    }
}
