package com.example.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ImageStorageHelper(private val context: Context) {

    private val headshotsDir: File by lazy {
        File(context.filesDir, "headshots").apply { mkdirs() }
    }

    /**
     * Decodes a gallery Uri into an upright, memory-safe Bitmap well under the 20MB Gemini inline limit.
     */
    suspend fun decodeUriToBitmap(uri: Uri, maxDimension: Int = 1536): Bitmap = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        } ?: throw IOException("Unable to read selected image.")

        val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val rawBitmap = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOpts)
        } ?: throw IOException("Failed to decode selected image.")

        val orientation = resolver.openInputStream(uri)?.use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        val oriented = applyExifOrientation(rawBitmap, orientation)
        scaleDownIfNeeded(oriented, maxDimension)
    }

    suspend fun decodeFileToBitmap(path: String, maxDimension: Int = 2048): Bitmap? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) return@withContext null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)

        val opts = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val raw = BitmapFactory.decodeFile(path, opts) ?: return@withContext null
        val orientation = try {
            ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
        scaleDownIfNeeded(applyExifOrientation(raw, orientation), maxDimension)
    }

    suspend fun saveInternalBitmap(bitmap: Bitmap, prefix: String): String = withContext(Dispatchers.IO) {
        val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png"
        val targetFile = File(headshotsDir, fileName)
        FileOutputStream(targetFile).use { out ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                throw IOException("Failed to write headshot image to internal storage.")
            }
        }
        targetFile.absolutePath
    }

    suspend fun deleteInternalFile(path: String) = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists() && file.parentFile?.canonicalPath == headshotsDir.canonicalPath) {
            file.delete()
        }
    }

    /**
     * Exports a high-resolution PNG to the device Photos gallery (Pictures/Lumen Studio)
     * via scoped MediaStore without requiring broad storage permissions.
     */
    suspend fun exportToPhotosGallery(bitmap: Bitmap, styleSlug: String): Uri = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val displayName = "LumenStudio_${styleSlug}_$timestamp.png"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Lumen Studio")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Could not create MediaStore entry for Photos export.")

        try {
            resolver.openOutputStream(uri)?.use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw IOException("Failed to encode PNG during Photos export.")
                }
            } ?: throw IOException("Unable to open MediaStore output stream.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    }

    fun mirrorHorizontally(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply { postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var inSampleSize = 1
        if (width > maxDim || height > maxDim) {
            val halfW = width / 2
            val halfH = height / 2
            while ((halfW / inSampleSize) >= maxDim || (halfH / inSampleSize) >= maxDim) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val largest = maxOf(bitmap.width, bitmap.height)
        if (largest <= maxDim) return bitmap
        val ratio = maxDim.toFloat() / largest.toFloat()
        val newW = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val newH = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        if (scaled != bitmap) bitmap.recycle()
        return scaled
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }
        val transformed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (transformed != bitmap) bitmap.recycle()
        return transformed
    }
}
