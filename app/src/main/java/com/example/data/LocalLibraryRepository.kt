package com.example.data

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.Flow

class LocalLibraryRepository(
    private val dao: HeadshotDao,
    private val storage: ImageStorageHelper
) {
    val headshots: Flow<List<HeadshotEntity>> = dao.observeAllHeadshots()

    suspend fun decodePickerUri(uri: Uri): Bitmap = storage.decodeUriToBitmap(uri)

    suspend fun decodeCameraFile(path: String, mirrorFrontCamera: Boolean): Bitmap {
        val decoded = storage.decodeFileToBitmap(path, maxDimension = 1536)
            ?: throw IllegalStateException("Could not load captured selfie.")
        return if (mirrorFrontCamera) storage.mirrorHorizontally(decoded) else decoded
    }

    suspend fun loadSavedBitmap(path: String): Bitmap? = storage.decodeFileToBitmap(path)

    suspend fun persistSourceSelfie(bitmap: Bitmap): String =
        storage.saveInternalBitmap(bitmap, prefix = "selfie")

    suspend fun saveGeneratedHeadshot(
        stylePresetId: String,
        styleName: String,
        modelTierId: String,
        modelName: String,
        aspectRatioLabel: String,
        resolutionLabel: String,
        retouchPrompt: String?,
        sourceImagePath: String,
        generatedBitmap: Bitmap
    ): HeadshotEntity {
        val generatedPath = storage.saveInternalBitmap(generatedBitmap, prefix = "headshot")
        val entity = HeadshotEntity(
            stylePresetId = stylePresetId,
            styleName = styleName,
            modelTierId = modelTierId,
            modelName = modelName,
            aspectRatioLabel = aspectRatioLabel,
            resolutionLabel = resolutionLabel,
            retouchPrompt = retouchPrompt,
            sourceImagePath = sourceImagePath,
            generatedImagePath = generatedPath
        )
        val id = dao.insertHeadshot(entity)
        return entity.copy(id = id)
    }

    suspend fun toggleFavorite(entity: HeadshotEntity) {
        dao.updateFavorite(entity.id, !entity.isFavorite)
    }

    suspend fun deleteHeadshot(entity: HeadshotEntity) {
        dao.deleteById(entity.id)
        storage.deleteInternalFile(entity.generatedImagePath)
        val remainingSourceRefs = dao.countReferencesToSource(entity.sourceImagePath)
        if (remainingSourceRefs == 0) {
            storage.deleteInternalFile(entity.sourceImagePath)
        }
    }

    suspend fun exportToPhotos(bitmap: Bitmap, stylePresetId: String): Uri =
        storage.exportToPhotosGallery(bitmap, stylePresetId)
}
