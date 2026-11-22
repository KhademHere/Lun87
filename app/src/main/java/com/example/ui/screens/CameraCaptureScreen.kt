package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.theme.HairlineSteel
import com.example.ui.theme.ObsidianDarkroom
import com.example.ui.theme.TungstenAmber
import java.io.File

private const val CAMERA_TAG = "StudioCamera"

@Composable
fun CameraCaptureScreen(
    onCaptured: (filePath: String, isFrontCamera: Boolean) -> Unit,
    onFallbackToGallery: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var useFrontCamera by remember { mutableStateOf(true) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDarkroom)
    ) {
        if (hasCameraPermission) {
            CameraXPortraitViewfinder(
                useFrontCamera = useFrontCamera,
                isCapturing = isCapturing,
                cameraError = cameraError,
                onFlipCamera = { useFrontCamera = !useFrontCamera },
                onClose = onClose,
                onCaptureFile = { imageCapture ->
                    if (isCapturing) return@CameraXPortraitViewfinder
                    isCapturing = true
                    cameraError = null
                    val outFile = File(context.cacheDir, "selfie_capture_${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(outFile).build()
                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                isCapturing = false
                                onCaptured(outFile.absolutePath, useFrontCamera)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e(CAMERA_TAG, "Photo capture failed", exception)
                                isCapturing = false
                                cameraError = exception.localizedMessage ?: "Failed to capture selfie."
                            }
                        }
                    )
                },
                onBindError = { msg -> cameraError = msg }
            )
        } else {
            // Friendly permission rationale with zero-permission gallery fallback
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = null,
                    tint = TungstenAmber,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Studio Camera Access",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.camera_permission_rationale),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grant_camera_permission_button")
                ) {
                    Text(stringResource(R.string.camera_grant_permission))
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        onClose()
                        onFallbackToGallery()
                    },
                    border = BorderStroke(1.dp, HairlineSteel),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("camera_fallback_gallery_button")
                ) {
                    Text(stringResource(R.string.action_choose_selfie))
                }
            }
        }
    }
}

@Composable
private fun CameraXPortraitViewfinder(
    useFrontCamera: Boolean,
    isCapturing: Boolean,
    cameraError: String?,
    onFlipCamera: () -> Unit,
    onClose: () -> Unit,
    onCaptureFile: (ImageCapture) -> Unit,
    onBindError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    DisposableEffect(useFrontCamera, lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        var boundProvider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                boundProvider = cameraProvider
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val preferredSelector = if (useFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                val selector = when {
                    cameraProvider.hasCamera(preferredSelector) -> preferredSelector
                    cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                    else -> {
                        onBindError("No camera available on this device.")
                        return@addListener
                    }
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e(CAMERA_TAG, "Failed to bind CameraX", e)
                onBindError(e.localizedMessage ?: "Camera initialization failed.")
            }
        }, executor)

        onDispose {
            boundProvider?.unbindAll()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Studio Oval Framing & Eye-Line Horizon Guide
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ovalW = size.width * 0.68f
            val ovalH = ovalW * 1.32f
            val topLeft = Offset((size.width - ovalW) / 2f, (size.height - ovalH) * 0.42f)

            drawOval(
                color = TungstenAmber.copy(alpha = 0.72f),
                topLeft = topLeft,
                size = Size(ovalW, ovalH),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                )
            )

            // Subtle eye-line ticks
            val eyeY = topLeft.y + ovalH * 0.40f
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = Offset(topLeft.x - 18.dp.toPx(), eyeY),
                end = Offset(topLeft.x - 4.dp.toPx(), eyeY),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = Offset(topLeft.x + ovalW + 4.dp.toPx(), eyeY),
                end = Offset(topLeft.x + ovalW + 18.dp.toPx(), eyeY),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // Top HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = ObsidianDarkroom.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, HairlineSteel)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("camera_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.camera_close_desc),
                        tint = Color.White
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ObsidianDarkroom.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, HairlineSteel)
            ) {
                Text(
                    text = "ALIGN FACE IN OVAL · EVEN LIGHT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TungstenAmber,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Surface(
                shape = CircleShape,
                color = ObsidianDarkroom.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, HairlineSteel)
            ) {
                IconButton(
                    onClick = onFlipCamera,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("camera_flip_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Cameraswitch,
                        contentDescription = stringResource(R.string.camera_flip_desc),
                        tint = Color.White
                    )
                }
            }
        }

        // Error Banner if camera fails
        if (cameraError != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Text(
                    text = cameraError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Bottom Shutter Controls
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(ObsidianDarkroom.copy(alpha = 0.72f))
                .navigationBarsPadding()
                .padding(vertical = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(3.dp, TungstenAmber, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(if (isCapturing) TungstenAmber.copy(alpha = 0.45f) else Color.White)
                    .clickable(enabled = !isCapturing) { onCaptureFile(imageCapture) }
                    .testTag("camera_shutter_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        color = ObsidianDarkroom,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
