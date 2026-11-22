package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.components.RetouchComposerSheet
import com.example.ui.components.SelfieAnalysisSheet
import com.example.ui.screens.CameraCaptureScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.FilmTrayCharcoal
import com.example.ui.theme.ObsidianDarkroom
import com.example.ui.theme.TungstenAmber
import com.example.ui.viewmodel.HeadshotStudioViewModel

const val ROUTE_STUDIO = "studio"
const val ROUTE_LIBRARY = "library"

private data class StudioNavDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val navDestinations = listOf(
    StudioNavDestination(
        route = ROUTE_STUDIO,
        labelRes = R.string.nav_studio,
        selectedIcon = Icons.Filled.Camera,
        unselectedIcon = Icons.Outlined.Camera
    ),
    StudioNavDestination(
        route = ROUTE_LIBRARY,
        labelRes = R.string.nav_library,
        selectedIcon = Icons.Filled.PhotoLibrary,
        unselectedIcon = Icons.Outlined.PhotoLibrary
    )
)

@Composable
fun LumenStudioApp(
    viewModel: HeadshotStudioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val libraryHeadshots by viewModel.libraryHeadshots.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_STUDIO
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Zero-permission Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onPhotoPicked(uri)
        }
    }

    val launchPhotoPicker: () -> Unit = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val navigateTo: (String) -> Unit = { route ->
        if (route != currentRoute) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDarkroom)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 700.dp

            Scaffold(
                containerColor = ObsidianDarkroom,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useNavigationRail) {
                        NavigationBar(
                            containerColor = FilmTrayCharcoal,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            navDestinations.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = stringResource(dest.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(dest.labelRes)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TungstenAmber,
                                        selectedTextColor = TungstenAmber,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_${dest.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = FilmTrayCharcoal,
                            modifier = Modifier
                                .fillMaxHeight()
                                .statusBarsPadding()
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            navDestinations.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = stringResource(dest.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(dest.labelRes)) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = TungstenAmber,
                                        selectedTextColor = TungstenAmber,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_rail_${dest.route}")
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_STUDIO,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        composable(ROUTE_STUDIO) {
                            StudioScreen(
                                state = uiState,
                                onPickSelfie = launchPhotoPicker,
                                onTakeSelfie = viewModel::openCameraCapture,
                                onAnalyzeSelfie = viewModel::openAnalysisSheetAndAnalyze,
                                onSelectPreset = viewModel::selectPreset,
                                onSelectModelTier = viewModel::selectModelTier,
                                onSelectAspectRatio = viewModel::selectAspectRatio,
                                onSelectResolution = viewModel::selectResolution,
                                onCustomNotesChange = viewModel::updateCustomStyleNotes,
                                onDevelopHeadshot = viewModel::developHeadshot,
                                onToggleCompare = { viewModel.toggleCompare() },
                                onSplitChange = viewModel::updateCompareFraction,
                                onOpenRetouch = { viewModel.openRetouchSheet() },
                                onSaveToPhotos = viewModel::exportActiveHeadshotToPhotos,
                                onToggleFavorite = {
                                    uiState.activeRecord?.let(viewModel::toggleFavorite)
                                },
                                onCancelGeneration = viewModel::cancelActiveGeneration
                            )
                        }

                        composable(ROUTE_LIBRARY) {
                            LibraryScreen(
                                headshots = libraryHeadshots,
                                activeFilter = uiState.libraryFilter,
                                isExporting = uiState.isExporting,
                                onFilterChange = viewModel::setLibraryFilter,
                                onOpenInStudio = { entity, openRetouch ->
                                    viewModel.openRecordInStudio(entity, openRetouch)
                                    navigateTo(ROUTE_STUDIO)
                                },
                                onToggleFavorite = viewModel::toggleFavorite,
                                onExportToPhotos = viewModel::exportRecordToPhotos,
                                onDelete = viewModel::deleteRecord,
                                onLoadBitmap = viewModel::loadBitmapForInspector,
                                onNavigateToStudio = { navigateTo(ROUTE_STUDIO) }
                            )
                        }
                    }
                }
            }
        }

        // Modal Bottom Sheets
        if (uiState.showRetouchSheet && uiState.developedHeadshot != null) {
            RetouchComposerSheet(
                promptDraft = uiState.retouchPromptDraft,
                onPromptChange = viewModel::updateRetouchPromptDraft,
                onApplyRetouch = viewModel::applyRetouch,
                onDismiss = viewModel::closeRetouchSheet
            )
        }

        if (uiState.showAnalysisSheet && uiState.sourceSelfie != null) {
            SelfieAnalysisSheet(
                analysisState = uiState.analysisState,
                artDirectionState = uiState.artDirectionState,
                extractedPrompts = uiState.extractedArtDirectionPrompts,
                targetRoleInput = uiState.targetRoleInput,
                onTargetRoleChange = viewModel::updateTargetRoleInput,
                onRetryAnalysis = viewModel::retrySelfieAnalysis,
                onApplyRecommendedPreset = viewModel::applyRecommendedStyle,
                onGenerateArtDirection = viewModel::generateDeepArtDirection,
                onUseArtDirectionPrompt = viewModel::useArtDirectionPrompt,
                onDismiss = viewModel::closeAnalysisSheet
            )
        }

        // Full-Screen In-App CameraX Viewfinder
        if (uiState.showCameraCapture) {
            CameraCaptureScreen(
                onCaptured = viewModel::onCameraSelfieCaptured,
                onFallbackToGallery = launchPhotoPicker,
                onClose = viewModel::closeCameraCapture
            )
        }
    }
}
