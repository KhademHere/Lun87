package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiStudioRepository
import com.example.data.HeadshotEntity
import com.example.data.ImageStorageHelper
import com.example.data.LocalLibraryRepository
import com.example.data.LumenDatabase
import com.example.model.AsyncOpState
import com.example.model.HeadshotAspectRatio
import com.example.model.HeadshotResolution
import com.example.model.HeadshotStylePreset
import com.example.model.SelfieAnalysisResult
import com.example.model.StudioModelTier
import com.example.model.StudioPresets
import com.google.firebase.ai.type.QuotaExceededException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "HeadshotStudioVM"
private const val AI_TIMEOUT_MS = 60_000L

data class StudioUiState(
    val sourceSelfie: Bitmap? = null,
    val sourceSelfiePath: String? = null,
    val developedHeadshot: Bitmap? = null,
    val activeRecord: HeadshotEntity? = null,
    val selectedPreset: HeadshotStylePreset = StudioPresets.all.first(),
    val selectedModelTier: StudioModelTier = StudioModelTier.FAST_STUDIO,
    val selectedAspectRatio: HeadshotAspectRatio = HeadshotAspectRatio.RATIO_3_4,
    val selectedResolution: HeadshotResolution = HeadshotResolution.RES_1K,
    val customStyleNotes: String = "",
    val retouchPromptDraft: String = "",
    val targetRoleInput: String = "",
    val isComparing: Boolean = false,
    val compareFraction: Float = 0.5f,
    val generationState: AsyncOpState<Unit> = AsyncOpState.Idle,
    val retouchState: AsyncOpState<Unit> = AsyncOpState.Idle,
    val analysisState: AsyncOpState<SelfieAnalysisResult> = AsyncOpState.Idle,
    val artDirectionState: AsyncOpState<String> = AsyncOpState.Idle,
    val extractedArtDirectionPrompts: List<String> = emptyList(),
    val showAnalysisSheet: Boolean = false,
    val showRetouchSheet: Boolean = false,
    val showCameraCapture: Boolean = false,
    val isExporting: Boolean = false,
    val libraryFilter: String = "ALL"
)

class HeadshotStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepository = AiStudioRepository()
    private val libraryRepository: LocalLibraryRepository by lazy {
        val db = LumenDatabase.getInstance(application)
        LocalLibraryRepository(
            dao = db.headshotDao(),
            storage = ImageStorageHelper(application)
        )
    }

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val libraryHeadshots: StateFlow<List<HeadshotEntity>> = libraryRepository.headshots
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    private var activeGenerationJob: Job? = null
    private var activeAnalysisJob: Job? = null
    private var activeArtDirectionJob: Job? = null

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            try {
                val bitmap = libraryRepository.decodePickerUri(uri)
                val savedPath = libraryRepository.persistSourceSelfie(bitmap)
                setNewSourceSelfie(bitmap, savedPath)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load picked photo", e)
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Could not load selected selfie.")
            }
        }
    }

    fun onCameraSelfieCaptured(filePath: String, isFrontCamera: Boolean) {
        viewModelScope.launch {
            try {
                val bitmap = libraryRepository.decodeCameraFile(filePath, mirrorFrontCamera = isFrontCamera)
                val savedPath = libraryRepository.persistSourceSelfie(bitmap)
                setNewSourceSelfie(bitmap, savedPath)
                _uiState.update { it.copy(showCameraCapture = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load captured selfie", e)
                _uiState.update { it.copy(showCameraCapture = false) }
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Could not process captured selfie.")
            }
        }
    }

    private fun setNewSourceSelfie(bitmap: Bitmap, savedPath: String) {
        activeAnalysisJob?.cancel()
        activeArtDirectionJob?.cancel()
        _uiState.update {
            it.copy(
                sourceSelfie = bitmap,
                sourceSelfiePath = savedPath,
                developedHeadshot = null,
                activeRecord = null,
                isComparing = false,
                compareFraction = 0.5f,
                generationState = AsyncOpState.Idle,
                retouchState = AsyncOpState.Idle,
                analysisState = AsyncOpState.Idle,
                artDirectionState = AsyncOpState.Idle,
                extractedArtDirectionPrompts = emptyList()
            )
        }
    }

    fun openCameraCapture() = _uiState.update { it.copy(showCameraCapture = true) }
    fun closeCameraCapture() = _uiState.update { it.copy(showCameraCapture = false) }

    fun selectPreset(preset: HeadshotStylePreset) {
        _uiState.update { it.copy(selectedPreset = preset) }
    }

    fun selectModelTier(tier: StudioModelTier) {
        _uiState.update { it.copy(selectedModelTier = tier) }
    }

    fun selectAspectRatio(ratio: HeadshotAspectRatio) {
        _uiState.update { it.copy(selectedAspectRatio = ratio) }
    }

    fun selectResolution(resolution: HeadshotResolution) {
        _uiState.update { it.copy(selectedResolution = resolution) }
    }

    fun updateCustomStyleNotes(notes: String) {
        _uiState.update { it.copy(customStyleNotes = notes) }
    }

    fun updateRetouchPromptDraft(prompt: String) {
        _uiState.update { it.copy(retouchPromptDraft = prompt) }
    }

    fun updateTargetRoleInput(role: String) {
        _uiState.update { it.copy(targetRoleInput = role) }
    }

    fun toggleCompare(enabled: Boolean? = null) {
        _uiState.update { state ->
            val next = enabled ?: !state.isComparing
            state.copy(isComparing = next && state.sourceSelfie != null && state.developedHeadshot != null)
        }
    }

    fun updateCompareFraction(fraction: Float) {
        _uiState.update { it.copy(compareFraction = fraction.coerceIn(0.05f, 0.95f)) }
    }

    fun setLibraryFilter(filter: String) {
        _uiState.update { it.copy(libraryFilter = filter) }
    }

    fun openRetouchSheet(prefillPrompt: String? = null) {
        _uiState.update {
            it.copy(
                showRetouchSheet = true,
                retouchState = AsyncOpState.Idle,
                retouchPromptDraft = prefillPrompt ?: it.retouchPromptDraft
            )
        }
    }

    fun closeRetouchSheet() {
        _uiState.update { it.copy(showRetouchSheet = false) }
    }

    fun openAnalysisSheetAndAnalyze() {
        val selfie = _uiState.value.sourceSelfie ?: return
        _uiState.update { it.copy(showAnalysisSheet = true) }
        if (_uiState.value.analysisState is AsyncOpState.Idle || _uiState.value.analysisState is AsyncOpState.Error) {
            analyzeSelfie(selfie)
        }
    }

    fun closeAnalysisSheet() {
        _uiState.update { it.copy(showAnalysisSheet = false) }
    }

    fun retrySelfieAnalysis() {
        val selfie = _uiState.value.sourceSelfie ?: return
        analyzeSelfie(selfie)
    }

    private fun analyzeSelfie(selfie: Bitmap) {
        activeAnalysisJob?.cancel()
        activeAnalysisJob = viewModelScope.launch {
            _uiState.update { it.copy(analysisState = AsyncOpState.Loading) }
            try {
                val result = withTimeout(AI_TIMEOUT_MS) {
                    aiRepository.analyzeSelfie(selfie)
                }
                _uiState.update { it.copy(analysisState = AsyncOpState.Success(result)) }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Selfie analysis timed out", e)
                _uiState.update {
                    it.copy(analysisState = AsyncOpState.Error("Analysis timed out after 60 seconds. Please retry."))
                }
            } catch (e: QuotaExceededException) {
                Log.e(TAG, "Selfie analysis quota exceeded", e)
                _uiState.update {
                    it.copy(analysisState = AsyncOpState.Error("Rate limit reached. Please wait a moment and try again."))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Selfie analysis failed", e)
                _uiState.update {
                    it.copy(analysisState = AsyncOpState.Error(e.localizedMessage ?: "Failed to analyze selfie."))
                }
            }
        }
    }

    fun applyRecommendedStyle(presetId: String) {
        val preset = StudioPresets.byId(presetId)
        _uiState.update {
            it.copy(
                selectedPreset = preset,
                showAnalysisSheet = false
            )
        }
        _snackbarEvents.tryEmit("Applied style: ${preset.title}")
    }

    fun generateDeepArtDirection() {
        val state = _uiState.value
        val selfie = state.sourceSelfie ?: return
        activeArtDirectionJob?.cancel()
        activeArtDirectionJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    artDirectionState = AsyncOpState.Loading,
                    extractedArtDirectionPrompts = emptyList()
                )
            }
            val accumulated = StringBuilder()
            try {
                withTimeout(AI_TIMEOUT_MS) {
                    aiRepository.streamDeepArtDirection(
                        selfieBitmap = selfie,
                        targetRole = state.targetRoleInput,
                        selectedPreset = state.selectedPreset
                    ).collect { chunk ->
                        accumulated.append(chunk)
                        _uiState.update {
                            it.copy(artDirectionState = AsyncOpState.Streaming(accumulated.toString()))
                        }
                    }
                }
                val fullText = accumulated.toString().trim()
                if (fullText.isBlank()) {
                    _uiState.update {
                        it.copy(artDirectionState = AsyncOpState.Error("Art director returned an empty brief."))
                    }
                } else {
                    val prompts = extractPromptBullets(fullText)
                    _uiState.update {
                        it.copy(
                            artDirectionState = AsyncOpState.Success(fullText),
                            extractedArtDirectionPrompts = prompts
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Deep art direction timed out", e)
                _uiState.update {
                    it.copy(artDirectionState = AsyncOpState.Error("Deep art direction timed out after 60 seconds."))
                }
            } catch (e: QuotaExceededException) {
                Log.e(TAG, "Deep art direction quota exceeded", e)
                _uiState.update {
                    it.copy(artDirectionState = AsyncOpState.Error("Rate limit exceeded. Please try again shortly."))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Deep art direction failed", e)
                _uiState.update {
                    it.copy(artDirectionState = AsyncOpState.Error(e.localizedMessage ?: "Failed to generate art direction."))
                }
            }
        }
    }

    fun useArtDirectionPrompt(prompt: String) {
        val hasHeadshot = _uiState.value.developedHeadshot != null
        if (hasHeadshot) {
            _uiState.update {
                it.copy(
                    showAnalysisSheet = false,
                    showRetouchSheet = true,
                    retouchPromptDraft = prompt
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    showAnalysisSheet = false,
                    customStyleNotes = prompt
                )
            }
            _snackbarEvents.tryEmit("Added direction to Studio notes")
        }
    }

    fun developHeadshot() {
        val state = _uiState.value
        val selfie = state.sourceSelfie ?: run {
            _snackbarEvents.tryEmit("Choose or take a selfie first.")
            return
        }
        val sourcePath = state.sourceSelfiePath ?: return

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    generationState = AsyncOpState.Loading,
                    isComparing = false
                )
            }
            try {
                val bitmap = withTimeout(AI_TIMEOUT_MS) {
                    aiRepository.generateHeadshot(
                        selfieBitmap = selfie,
                        preset = state.selectedPreset,
                        modelTier = state.selectedModelTier,
                        aspectRatio = state.selectedAspectRatio,
                        resolution = state.selectedResolution,
                        customNotes = state.customStyleNotes
                    )
                }

                val savedEntity = libraryRepository.saveGeneratedHeadshot(
                    stylePresetId = state.selectedPreset.id,
                    styleName = state.selectedPreset.title,
                    modelTierId = state.selectedModelTier.id,
                    modelName = state.selectedModelTier.modelName,
                    aspectRatioLabel = state.selectedAspectRatio.label,
                    resolutionLabel = state.selectedResolution.label,
                    retouchPrompt = state.customStyleNotes.trim().takeIf { it.isNotEmpty() },
                    sourceImagePath = sourcePath,
                    generatedBitmap = bitmap
                )

                _uiState.update {
                    it.copy(
                        developedHeadshot = bitmap,
                        activeRecord = savedEntity,
                        generationState = AsyncOpState.Success(Unit),
                        isComparing = true,
                        compareFraction = 0.5f
                    )
                }
                _snackbarEvents.tryEmit("Headshot developed and archived to Library")
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Headshot generation timed out", e)
                _uiState.update {
                    it.copy(
                        generationState = AsyncOpState.Error(
                            "Studio synthesis timed out after 60 seconds. Try 1K or Studio Fast, or retry."
                        )
                    )
                }
            } catch (e: QuotaExceededException) {
                Log.e(TAG, "Headshot generation quota exceeded", e)
                _uiState.update {
                    it.copy(
                        generationState = AsyncOpState.Error(
                            "Image generation rate limit reached. Please wait a moment and try again."
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Headshot generation failed", e)
                _uiState.update {
                    it.copy(
                        generationState = AsyncOpState.Error(
                            e.localizedMessage ?: "Headshot synthesis failed. Please retry."
                        )
                    )
                }
            }
        }
    }

    fun cancelActiveGeneration() {
        activeGenerationJob?.cancel()
        _uiState.update {
            it.copy(
                generationState = AsyncOpState.Idle,
                retouchState = AsyncOpState.Idle
            )
        }
    }

    fun applyRetouch(instructionOverride: String? = null) {
        val state = _uiState.value
        val currentHeadshot = state.developedHeadshot ?: return
        val sourcePath = state.sourceSelfiePath ?: state.activeRecord?.sourceImagePath ?: return
        val instruction = (instructionOverride ?: state.retouchPromptDraft).trim()
        if (instruction.isEmpty()) {
            _snackbarEvents.tryEmit("Enter a retouch instruction first.")
            return
        }

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    retouchState = AsyncOpState.Loading,
                    showRetouchSheet = false
                )
            }
            try {
                val editedBitmap = withTimeout(AI_TIMEOUT_MS) {
                    aiRepository.retouchHeadshot(
                        currentHeadshot = currentHeadshot,
                        originalSelfie = state.sourceSelfie,
                        instruction = instruction,
                        aspectRatio = state.selectedAspectRatio,
                        resolution = state.selectedResolution
                    )
                }

                val savedEntity = libraryRepository.saveGeneratedHeadshot(
                    stylePresetId = state.selectedPreset.id,
                    styleName = "${state.selectedPreset.shortTitle} · Retouch",
                    modelTierId = StudioModelTier.FAST_STUDIO.id,
                    modelName = StudioModelTier.FAST_STUDIO.modelName,
                    aspectRatioLabel = state.selectedAspectRatio.label,
                    resolutionLabel = state.selectedResolution.label,
                    retouchPrompt = instruction,
                    sourceImagePath = sourcePath,
                    generatedBitmap = editedBitmap
                )

                _uiState.update {
                    it.copy(
                        developedHeadshot = editedBitmap,
                        activeRecord = savedEntity,
                        retouchState = AsyncOpState.Success(Unit),
                        retouchPromptDraft = "",
                        isComparing = state.sourceSelfie != null
                    )
                }
                _snackbarEvents.tryEmit("Retouch applied & saved to Library")
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Retouch timed out", e)
                _uiState.update {
                    it.copy(retouchState = AsyncOpState.Error("Retouch timed out after 60 seconds. Please try again."))
                }
            } catch (e: QuotaExceededException) {
                Log.e(TAG, "Retouch quota exceeded", e)
                _uiState.update {
                    it.copy(retouchState = AsyncOpState.Error("Rate limit exceeded. Please wait a moment and retry."))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Retouch failed", e)
                _uiState.update {
                    it.copy(retouchState = AsyncOpState.Error(e.localizedMessage ?: "Retouch failed."))
                }
            }
        }
    }

    fun exportActiveHeadshotToPhotos() {
        val state = _uiState.value
        val bitmap = state.developedHeadshot ?: return
        val slug = state.activeRecord?.stylePresetId ?: state.selectedPreset.id
        exportBitmapToPhotos(bitmap, slug)
    }

    fun exportRecordToPhotos(entity: HeadshotEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val bitmap = libraryRepository.loadSavedBitmap(entity.generatedImagePath)
                    ?: throw IllegalStateException("Headshot file could not be loaded.")
                libraryRepository.exportToPhotos(bitmap, entity.stylePresetId)
                _snackbarEvents.tryEmit("Saved to Photos (Pictures/Lumen Studio)")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Export to Photos failed", e)
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to save to Photos.")
            } finally {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    private fun exportBitmapToPhotos(bitmap: Bitmap, slug: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                libraryRepository.exportToPhotos(bitmap, slug)
                _snackbarEvents.tryEmit("Saved to Photos (Pictures/Lumen Studio)")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Export to Photos failed", e)
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Failed to save to Photos.")
            } finally {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    fun toggleFavorite(entity: HeadshotEntity) {
        viewModelScope.launch {
            try {
                libraryRepository.toggleFavorite(entity)
                if (_uiState.value.activeRecord?.id == entity.id) {
                    _uiState.update { it.copy(activeRecord = entity.copy(isFavorite = !entity.isFavorite)) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Toggle favorite failed", e)
            }
        }
    }

    fun deleteRecord(entity: HeadshotEntity) {
        viewModelScope.launch {
            try {
                libraryRepository.deleteHeadshot(entity)
                if (_uiState.value.activeRecord?.id == entity.id) {
                    _uiState.update { it.copy(activeRecord = null) }
                }
                _snackbarEvents.tryEmit("Removed from Studio Library")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Delete headshot failed", e)
                _snackbarEvents.tryEmit("Could not delete headshot.")
            }
        }
    }

    fun openRecordInStudio(entity: HeadshotEntity, openRetouchImmediately: Boolean = false) {
        viewModelScope.launch {
            try {
                val generated = libraryRepository.loadSavedBitmap(entity.generatedImagePath)
                    ?: throw IllegalStateException("Headshot file no longer exists.")
                val source = libraryRepository.loadSavedBitmap(entity.sourceImagePath)
                val preset = StudioPresets.byId(entity.stylePresetId)
                val ratio = HeadshotAspectRatio.entries.firstOrNull { it.label == entity.aspectRatioLabel }
                    ?: HeadshotAspectRatio.RATIO_3_4
                val res = HeadshotResolution.entries.firstOrNull { it.label == entity.resolutionLabel }
                    ?: HeadshotResolution.RES_1K
                val tier = StudioModelTier.entries.firstOrNull { it.id == entity.modelTierId }
                    ?: StudioModelTier.FAST_STUDIO

                _uiState.update {
                    it.copy(
                        sourceSelfie = source ?: it.sourceSelfie,
                        sourceSelfiePath = entity.sourceImagePath,
                        developedHeadshot = generated,
                        activeRecord = entity,
                        selectedPreset = preset,
                        selectedAspectRatio = ratio,
                        selectedResolution = res,
                        selectedModelTier = tier,
                        isComparing = source != null,
                        compareFraction = 0.5f,
                        generationState = AsyncOpState.Success(Unit),
                        retouchState = AsyncOpState.Idle,
                        showRetouchSheet = openRetouchImmediately
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Open record in studio failed", e)
                _snackbarEvents.tryEmit(e.localizedMessage ?: "Unable to open headshot.")
            }
        }
    }

    suspend fun loadBitmapForInspector(path: String): Bitmap? =
        libraryRepository.loadSavedBitmap(path)

    private fun extractPromptBullets(brief: String): List<String> {
        return brief.lines()
            .map { it.trim() }
            .filter { it.startsWith("- ") || it.startsWith("• ") }
            .map { it.removePrefix("- ").removePrefix("• ").trim().trim('"') }
            .filter { it.length in 14..180 }
            .takeLast(3)
    }
}
