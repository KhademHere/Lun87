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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.FilmTrayCharcoal
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.ObsidianDarkroom
import com.example.ui.theme.StudioSlate
import com.example.ui.theme.TungstenAmber
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

    // Keep inspected item synced with live Room updates (e.g., favorite toggle)
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Contact Sheet",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "LOCAL STUDIO ARCHIVE · ${headshots.size} EXPOSURES",
                    style = MaterialTheme.typography.labelSmall,
                    color = TungstenAmber
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Chips Row
        val filterOptions = remember {
            buildList {
                add("ALL" to "All")
                add("FAVORITES" to "Favorites")
                StudioPresets.all.forEach { add(it.id to it.shortTitle) }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(filterOptions, key = { it.first }) { (key, label) ->
                val selected = activeFilter == key
                FilterChip(
                    selected = selected,
                    onClick = { onFilterChange(key) },
                    label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = FilmTrayCharcoal,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = TungstenAmber,
                        labelColor = MaterialTheme.colorScheme.secondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = HairlineSteel,
                        selectedBorderColor = TungstenAmber
                    ),
                    modifier = Modifier.testTag("library_filter_$key")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(StudioSlate)
                            .border(1.dp, HairlineSteel, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = null,
                            tint = TungstenAmber,
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
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.empty_library_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (headshots.isEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onNavigateToStudio,
                            modifier = Modifier.testTag("empty_library_go_studio_button")
                        ) {
                            Text("Open Darkroom Studio")
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 158.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
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
            containerColor = FilmTrayCharcoal,
            title = { Text("Delete Headshot?", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    "Remove this ${candidate.styleName} exposure from your local Studio Library? Photos already exported to your device gallery will remain untouched.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteCandidate = null
                        if (inspectedEntity?.id == candidate.id) inspectedEntity = null
                        onDelete(candidate)
                    },
                    modifier = Modifier.testTag("confirm_delete_headshot_button")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("Cancel")
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
            .clip(RoundedCornerShape(16.dp))
            .background(FilmTrayCharcoal)
            .border(1.dp, HairlineSteel, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("library_card_${entity.id}")
    ) {
        AsyncImage(
            model = File(entity.generatedImagePath),
            contentDescription = entity.styleName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent,
                            ObsidianDarkroom.copy(alpha = 0.90f)
                        )
                    )
                )
        )

        // Top-left ratio & resolution HUD pill
        Surface(
            color = ObsidianDarkroom.copy(alpha = 0.72f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Text(
                text = "${entity.aspectRatioLabel} · ${entity.resolutionLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }

        // Top-right favorite toggle (48dp touch area)
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(48.dp)
                .testTag("library_fav_${entity.id}")
        ) {
            Icon(
                imageVector = if (entity.isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (entity.isFavorite) "Remove favorite" else "Favorite headshot",
                tint = if (entity.isFavorite) TungstenAmber else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = entity.styleName,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val dateStr = remember(entity.createdAt) {
                SimpleDateFormat("MMM d · HH:mm", Locale.getDefault()).format(Date(entity.createdAt))
            }
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            color = FilmTrayCharcoal,
            border = BorderStroke(1.dp, HairlineSteel)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entity.styleName,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${entity.modelTierId.uppercase()} · ${entity.aspectRatioLabel} · ${entity.resolutionLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TungstenAmber
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (entity.isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Toggle favorite",
                                tint = if (entity.isFavorite) TungstenAmber else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_inspector_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close inspector"
                            )
                        }
                    }
                }

                // Image / Compare Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianDarkroom),
                    contentAlignment = Alignment.Center
                ) {
                    val after = generatedBitmap
                    val before = sourceBitmap
                    when {
                        after == null -> CircularProgressIndicator(color = TungstenAmber)
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
                    Text(
                        text = "Direction: ${entity.retouchPrompt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (sourceBitmap != null) {
                        FilledTonalButton(
                            onClick = { isComparing = !isComparing },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("inspector_compare_button")
                        ) {
                            Icon(Icons.Outlined.Compare, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isComparing) "Final" else "Compare")
                        }
                    }

                    FilledTonalButton(
                        onClick = { onOpenInStudio(true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inspector_retouch_button")
                    ) {
                        Icon(Icons.Outlined.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Retouch")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onSaveToPhotos,
                        enabled = !isExporting,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inspector_save_photos_button")
                    ) {
                        Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.action_save_to_photos))
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StudioSlate,
                        border = BorderStroke(1.dp, HairlineSteel)
                    ) {
                        IconButton(
                            onClick = onRequestDelete,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("inspector_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete headshot",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
