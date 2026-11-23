package com.example.ui.screens

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.HeadshotEntity
import com.example.model.StudioPresets
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.DestructiveActionButton
import com.example.ui.components.PrimaryCtaButton
import com.example.ui.components.SecondaryActionButton
import com.example.ui.components.TertiaryActionButton
import com.example.ui.theme.DeepPlum
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LibraryScreen(
    headshots: List<HeadshotEntity>,
    activeFilter: String,
    isExporting: Boolean,
    onFilterChange: (String) -> Unit,
    onOpenInStudio: (HeadshotEntity, Boolean) -> Unit,
    onToggleFavorite: (HeadshotEntity) -> Unit,
    onExportToPhotos: (HeadshotEntity) -> Unit,
    onDelete: (HeadshotEntity) -> Unit,
    onLoadBitmap: suspend (String) -> Bitmap?,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inspectedEntity by remember { mutableStateOf<HeadshotEntity?>(null) }
    var deleteCandidate by remember { mutableStateOf<HeadshotEntity?>(null) }

    val liveInspected = inspectedEntity?.let { current ->
        headshots.firstOrNull { it.id == current.id }
    }

    val filteredList = remember(headshots, activeFilter) {
        when (activeFilter) {
            "ALL" -> headshots
            "FAVORITES" -> headshots.filter { it.isFavorite }
            else -> headshots.filter { it.stylePresetId == activeFilter }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = StudioSpacing.screenEdge, vertical = 16.dp)
    ) {
        // Page Header with Tabular Exposure Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Studio Library",
                    style = MaterialTheme.typography.displaySmall,
                    color = MidnightIndigo
                )
                Text(
                    text = "Local contact sheet & saved portraits",
                    style = MaterialTheme.typography.bodySmall,
                    color = DeepPlum
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DustyRoseMuted,
                border = BorderStroke(1.dp, DustyRoseBorder)
            ) {
                Text(
                    text = "${headshots.size} saved",
                    style = MaterialTheme.typography.labelMedium.tabular(),
                    color = MidnightIndigo,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Accessible 48dp Filter Chips Row
        val filterOptions = remember {
            buildList {
                add("ALL" to "All")
                add("FAVORITES" to "Favorites")
                StudioPresets.all.forEach { add(it.id to it.shortTitle) }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(StudioSpacing.tight),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(filterOptions, key = { it.first }) { (key, label) ->
                val selected = activeFilter == key
                FilterChip(
                    selected = selected,
                    onClick = { onFilterChange(key) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = PaleLavenderElevated,
                        selectedContainerColor = DeepPlum,
                        selectedLabelColor = PaleLavender,
                        labelColor = MidnightIndigo
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = DustyRoseBorder,
                        selectedBorderColor = DeepPlum,
                        borderWidth = 1.25.dp,
                        selectedBorderWidth = 1.5.dp
                    ),
                    modifier = Modifier
                        .defaultMinSize(minHeight = StudioSpacing.touchTarget)
                        .testTag("library_filter_$key")
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = PaleLavenderElevated,
                    border = BorderStroke(1.25.dp, DustyRoseBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(StudioSpacing.controlGap)
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
                                imageVector = Icons.Outlined.PhotoLibrary,
                                contentDescription = null,
                                tint = DeepPlum,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = if (headshots.isEmpty()) {
                                stringResource(R.string.empty_library_title)
                            } else {
                                "No headshots match this filter"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            color = MidnightIndigo,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.empty_library_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepPlum,
                            textAlign = TextAlign.Center
                        )
                        if (headshots.isEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            PrimaryCtaButton(
                                onClick = onNavigateToStudio,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("empty_library_go_studio_button")
                            ) {
                                Text("Open Studio", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 164.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 28.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { item ->
                    ContactSheetCard(
                        entity = item,
                        onClick = { inspectedEntity = item },
                        onToggleFavorite = { onToggleFavorite(item) }
                    )
                }
            }
        }
    }

    if (liveInspected != null) {
        HeadshotInspectorDialog(
            entity = liveInspected,
            isExporting = isExporting,
            onLoadBitmap = onLoadBitmap,
            onToggleFavorite = { onToggleFavorite(liveInspected) },
            onSaveToPhotos = { onExportToPhotos(liveInspected) },
            onOpenInStudio = { openRetouch ->
                inspectedEntity = null
                onOpenInStudio(liveInspected, openRetouch)
            },
            onRequestDelete = { deleteCandidate = liveInspected },
            onDismiss = { inspectedEntity = null }
        )
    }

    if (deleteCandidate != null) {
        val candidate = deleteCandidate!!
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            containerColor = PaleLavenderSoft,
            titleContentColor = MidnightIndigo,
            textContentColor = MidnightIndigo,
            title = {
                Text(
                    text = "Delete Headshot?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MidnightIndigo
                )
            },
            text = {
                Text(
                    text = "Remove this ${candidate.styleName} exposure from your local Studio Library? Photos already saved to your device gallery will remain untouched.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepPlum
                )
            },
            dismissButton = {
                TertiaryActionButton(onClick = { deleteCandidate = null }) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
            },
            confirmButton = {
                DestructiveActionButton(
                    onClick = {
                        deleteCandidate = null
                        if (inspectedEntity?.id == candidate.id) inspectedEntity = null
                        onDelete(candidate)
                    },
                    filled = true,
                    modifier = Modifier.testTag("confirm_delete_headshot_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Delete", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}

@Composable
private fun ContactSheetCard(
    entity: HeadshotEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.76f)
            .clip(RoundedCornerShape(18.dp))
            .background(MidnightIndigo)
            .border(1.25.dp, DustyRoseBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("library_card_${entity.id}")
    ) {
        AsyncImage(
            model = File(entity.generatedImagePath),
            contentDescription = entity.styleName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // High-contrast Midnight Indigo gradient scrim for AAA Pale Lavender metadata readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MidnightIndigo.copy(alpha = 0.28f),
                            MidnightIndigo.copy(alpha = 0.04f),
                            MidnightIndigo.copy(alpha = 0.90f)
                        )
                    )
                )
        )

        // Top-left tabular spec badge
        Surface(
            color = MidnightIndigo.copy(alpha = 0.86f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, OrchidMauve.copy(alpha = 0.65f)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            Text(
                text = "${entity.aspectRatioLabel} · ${entity.resolutionLabel}",
                style = MaterialTheme.typography.labelSmall.tabular(),
                color = PaleLavender,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Top-right accessible 48dp favorite icon button
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(StudioSpacing.touchTarget)
                .testTag("library_fav_${entity.id}")
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (entity.isFavorite) DeepPlum else MidnightIndigo.copy(alpha = 0.72f))
                    .border(1.dp, PaleLavender.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (entity.isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (entity.isFavorite) "Remove from favorites" else "Save to favorites",
                    tint = PaleLavender,
                    modifier = Modifier.size(18.dp)
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
                text = entity.styleName,
                style = MaterialTheme.typography.titleSmall,
                color = PaleLavender,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val dateStr = remember(entity.createdAt) {
                SimpleDateFormat("MMM d · HH:mm", Locale.getDefault()).format(Date(entity.createdAt))
            }
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelSmall.tabular(),
                color = DustyRose
            )
        }
    }
}

@Composable
private fun HeadshotInspectorDialog(
    entity: HeadshotEntity,
    isExporting: Boolean,
    onLoadBitmap: suspend (String) -> Bitmap?,
    onToggleFavorite: () -> Unit,
    onSaveToPhotos: () -> Unit,
    onOpenInStudio: (Boolean) -> Unit,
    onRequestDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var generatedBitmap by remember(entity.generatedImagePath) { mutableStateOf<Bitmap?>(null) }
    var sourceBitmap by remember(entity.sourceImagePath) { mutableStateOf<Bitmap?>(null) }
    var isComparing by remember { mutableStateOf(false) }
    var splitFraction by remember { mutableFloatStateOf(0.5f) }

    LaunchedEffect(entity.id) {
        generatedBitmap = onLoadBitmap(entity.generatedImagePath)
        sourceBitmap = onLoadBitmap(entity.sourceImagePath)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            color = PaleLavenderSoft,
            border = BorderStroke(1.5.dp, DustyRoseBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(StudioSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                            text = entity.styleName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MidnightIndigo,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${entity.modelTierId.uppercase()} · ${entity.aspectRatioLabel} · ${entity.resolutionLabel}",
                            style = MaterialTheme.typography.labelMedium.tabular(),
                            color = DeepPlum
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(StudioSpacing.touchTarget)
                        ) {
                            Icon(
                                imageVector = if (entity.isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (entity.isFavorite) "Remove favorite" else "Mark favorite",
                                tint = DeepPlum
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(StudioSpacing.touchTarget)
                                .testTag("close_inspector_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close inspector",
                                tint = MidnightIndigo
                            )
                        }
                    }
                }

                // Midnight Indigo Photo Mat Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MidnightIndigo)
                        .border(1.dp, OrchidMauve.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val after = generatedBitmap
                    val before = sourceBitmap
                    when {
                        after == null -> CircularProgressIndicator(color = PaleLavender)
                        isComparing && before != null -> {
                            BeforeAfterSlider(
                                beforeBitmap = before,
                                afterBitmap = after,
                                splitFraction = splitFraction,
                                onSplitFractionChange = { splitFraction = it },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            val img = remember(after) { after.asImageBitmap() }
                            Image(
                                bitmap = img,
                                contentDescription = entity.styleName,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                if (!entity.retouchPrompt.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PaleLavenderElevated,
                        border = BorderStroke(1.dp, DustyRoseBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Direction: ${entity.retouchPrompt}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MidnightIndigo,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }

                // Secondary Actions Row: Compare & Retouch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(StudioSpacing.controlGap)
                ) {
                    if (sourceBitmap != null) {
                        SecondaryActionButton(
                            onClick = { isComparing = !isComparing },
                            selected = isComparing,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("inspector_compare_button")
                        ) {
                            Icon(Icons.Outlined.Compare, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isComparing) "Show Final" else "Compare", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    SecondaryActionButton(
                        onClick = { onOpenInStudio(true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inspector_retouch_button")
                    ) {
                        Icon(Icons.Outlined.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Retouch", style = MaterialTheme.typography.labelLarge)
                    }
                }

                HorizontalDivider(color = DustyRose)

                // Safe Separation: Destructive Delete (left, distinct) vs Primary Save to Photos (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(StudioSpacing.controlGap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DestructiveActionButton(
                        onClick = onRequestDelete,
                        filled = false,
                        modifier = Modifier.testTag("inspector_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete headshot",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Delete", style = MaterialTheme.typography.labelLarge)
                    }

                    PrimaryCtaButton(
                        onClick = onSaveToPhotos,
                        enabled = !isExporting,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inspector_save_photos_button")
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                color = PaleLavender,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.action_save_to_photos),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
