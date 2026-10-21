package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.components.AppOutlinedTextField
import com.example.util.LocationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private data class PendingPhotoCapture(
    val uri: Uri,
    val exactLat: Double,
    val exactLng: Double,
    val exactTime: Long,
    val exactRa: String,
    val orientationDegrees: Int
)

/**
 * Câmera Técnica de Campo para Fiscalização Ambiental e Bem-Estar Animal.
 * - Orientação das fotos e da marca d'água sincronizada com a rotação do aparelho (Paisagem / Retrato).
 * - HUD georreferenciado limpo e adaptativo que acompanha a rotação sem deslocamento ou cortes.
 * - Zoom suave por gestos de pinça dos dedos e botões de atalho rápido.
 */
@Composable
fun CameraCaptureScreen(
    onPhotoCaptured: (Uri, Double, Double, Long, String, String?, Int) -> Unit,
    onClose: () -> Unit,
    onPickFromGallery: () -> Unit,
    initialLatitude: Double = -15.7942,
    initialLongitude: Double = -47.8821,
    administrativeRegion: String = "Plano Piloto",
    capturedPhotosCount: Int = 0,
    protocolNumber: String = "DF-MAUS-TRATOS-2026",
    photoNumber: Int = capturedPhotosCount + 1
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val locationHelper = remember { LocationHelper(context) }

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }

    // Telemetria em tempo real
    var currentLatitude by remember { mutableDoubleStateOf(initialLatitude) }
    var currentLongitude by remember { mutableDoubleStateOf(initialLongitude) }
    var currentAccuracy by remember { mutableFloatStateOf(0f) }
    var currentAltitude by remember { mutableDoubleStateOf(0.0) }
    var isRealGps by remember { mutableStateOf(false) }
    var currentDynamicRa by remember { mutableStateOf(administrativeRegion.ifBlank { "Plano Piloto" }) }
    var currentTimestampMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Estado do Zoom (1x até 8x)
    var currentZoomRatio by remember { mutableFloatStateOf(1f) }
    var minZoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoomRatio by remember { mutableFloatStateOf(8f) }

    // Detecção física da orientação do sensor
    var currentDeviceOrientation by remember { mutableIntStateOf(0) }
    var targetRotationSurface by remember { mutableIntStateOf(Surface.ROTATION_0) }

    val orientationEventListener = remember(context) {
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val (surfaceRot, deg) = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270 to 270
                    in 135 until 225 -> Surface.ROTATION_180 to 180
                    in 225 until 315 -> Surface.ROTATION_90 to 90
                    else -> Surface.ROTATION_0 to 0
                }
                if (deg != currentDeviceOrientation) {
                    currentDeviceOrientation = deg
                    targetRotationSurface = surfaceRot
                    imageCapture?.targetRotation = surfaceRot
                }
            }
        }
    }

    DisposableEffect(orientationEventListener) {
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }
        onDispose {
            orientationEventListener.disable()
        }
    }

    // Identifica se a janela está em orientação paisagem pelo sistema
    val isScreenLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Ângulo de rotação animada para os elementos de interface acompanharem a mão do fiscal
    val targetUiRotation = when (currentDeviceOrientation) {
        90 -> 270f
        180 -> 180f
        270 -> 90f
        else -> 0f
    }
    val uiRotationDegrees by animateFloatAsState(
        targetValue = if (isScreenLandscape) 0f else targetUiRotation,
        animationSpec = tween(durationMillis = 300),
        label = "cameraUiRotation"
    )

    var isCapturing by remember { mutableStateOf(false) }
    var flashAnimation by remember { mutableStateOf(false) }
    var pendingPhotoCapture by remember { mutableStateOf<PendingPhotoCapture?>(null) }
    var photoTagInput by remember { mutableStateOf("") }

    // Stream contínuo de coordenadas dinâmicas e relógio
    LaunchedEffect(Unit) {
        launch {
            while (isActive) {
                currentTimestampMillis = System.currentTimeMillis()
                delay(1000L)
            }
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        locationHelper.getLocationUpdatesFlow(hasPermission).collect { coords ->
            currentLatitude = coords.latitude
            currentLongitude = coords.longitude
            currentAccuracy = coords.accuracy
            currentAltitude = coords.altitude
            isRealGps = coords.isRealGps
            val detected = com.example.data.model.DfConstants.detectClosestRa(coords.latitude, coords.longitude)
            currentDynamicRa = detected
        }
    }

    BackHandler {
        onClose()
    }

    val triggerCapture: () -> Unit = {
        if (!isCapturing) {
            isCapturing = true
            flashAnimation = true

            val exactLat = currentLatitude
            val exactLng = currentLongitude
            val exactTime = currentTimestampMillis
            val exactRa = currentDynamicRa
            val captureOrientation = currentDeviceOrientation

            val capture = imageCapture
            if (capture != null) {
                val photosDir = File(context.cacheDir, "camera_raw").apply {
                    if (!exists()) mkdirs()
                }
                val photoFile = File(photosDir, "CAM_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                capture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            flashAnimation = false
                            isCapturing = false
                            val uri = Uri.fromFile(photoFile)
                            pendingPhotoCapture = PendingPhotoCapture(uri, exactLat, exactLng, exactTime, exactRa, captureOrientation)
                            photoTagInput = ""
                        }

                        override fun onError(exception: ImageCaptureException) {
                            val isPhotoLandscape = (captureOrientation == 90 || captureOrientation == 270 || isScreenLandscape)
                            val fallbackFile = createFallbackFieldPhoto(context, exactLat, exactLng, exactRa, protocolNumber, exactTime, photoNumber, isPhotoLandscape)
                            flashAnimation = false
                            isCapturing = false
                            pendingPhotoCapture = PendingPhotoCapture(Uri.fromFile(fallbackFile), exactLat, exactLng, exactTime, exactRa, captureOrientation)
                            photoTagInput = ""
                        }
                    }
                )
            } else {
                val isPhotoLandscape = (captureOrientation == 90 || captureOrientation == 270 || isScreenLandscape)
                val fallbackFile = createFallbackFieldPhoto(context, exactLat, exactLng, exactRa, protocolNumber, exactTime, photoNumber, isPhotoLandscape)
                flashAnimation = false
                isCapturing = false
                pendingPhotoCapture = PendingPhotoCapture(Uri.fromFile(fallbackFile), exactLat, exactLng, exactTime, exactRa, captureOrientation)
                photoTagInput = ""
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fullscreen Camera Preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    setBackgroundColor(AndroidColor.BLACK)
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val capture = ImageCapture.Builder()
                        .setFlashMode(flashMode)
                        .setTargetRotation(targetRotationSurface)
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    imageCapture = capture

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            lensFacing,
                            preview,
                            capture
                        )
                        activeCamera = camera
                        camera.cameraInfo.zoomState.observe(lifecycleOwner) { zoomState ->
                            if (zoomState != null) {
                                currentZoomRatio = zoomState.zoomRatio
                                minZoomRatio = zoomState.minZoomRatio
                                maxZoomRatio = zoomState.maxZoomRatio.coerceAtMost(8f)
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Aviso da câmera: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            update = {
                imageCapture?.flashMode = flashMode
                imageCapture?.targetRotation = targetRotationSurface
            }
        )

        // Overlay transparente para detecção de gestos de PINÇA com os dedos (Zoom In / Zoom Out)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(activeCamera) {
                    detectTransformGestures { _, _, zoomFactor, _ ->
                        if (zoomFactor != 1f) {
                            val camera = activeCamera ?: return@detectTransformGestures
                            val current = currentZoomRatio
                            val newZoom = (current * zoomFactor).coerceIn(minZoomRatio, maxZoomRatio)
                            currentZoomRatio = newZoom
                            camera.cameraControl.setZoomRatio(newZoom)
                        }
                    }
                }
        )

        // Shutter Flash Animation overlay
        AnimatedVisibility(
            visible = flashAnimation,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.85f))
            )
        }

        // =========================================================================
        // 1. BARRA SUPERIOR: Voltar (Esquerda) e Flash / Trocar Câmera (Direita)
        // Separados do HUD para nunca haver sobreposição ou truncamento
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(
                    top = if (isScreenLandscape) 14.dp else 42.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botão Voltar
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    .graphicsLayer { rotationZ = uiRotationDegrees }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }

            // Ações de Flash e Alternar Lente (Frontal/Traseira)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                            else -> ImageCapture.FLASH_MODE_AUTO
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                        .graphicsLayer { rotationZ = uiRotationDegrees }
                ) {
                    val flashIcon = when (flashMode) {
                        ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                        ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
                        else -> Icons.Default.FlashAuto
                    }
                    Icon(
                        imageVector = flashIcon,
                        contentDescription = "Flash",
                        tint = if (flashMode == ImageCapture.FLASH_MODE_ON) Color(0xFFFFD54F) else Color.White
                    )
                }

                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                        .graphicsLayer { rotationZ = uiRotationDegrees }
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Trocar Câmera",
                        tint = Color.White
                    )
                }
            }
        }

        // =========================================================================
        // 2. HUD FLUTUANTE GEORREFERENCIADO (GPS, Data/Hora, RA, Foto #)
        // Design unificado em 2 linhas, compacto, nítido e com rotação adaptativa
        // =========================================================================
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.Black.copy(alpha = 0.78f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    top = if (isScreenLandscape) 14.dp else 94.dp,
                    start = 16.dp,
                    end = 16.dp
                )
                .widthIn(max = 380.dp)
                .graphicsLayer { rotationZ = uiRotationDegrees }
                .testTag("camera_gps_hud")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Linha 1: Status GPS + Coordenadas + Precisão + Foto #
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Ponto indicador GPS
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (isRealGps) Color(0xFF22C55E) else Color(0xFFEAB308), CircleShape)
                    )

                    Text(
                        text = String.format(Locale.US, "%.6f°, %.6f°", currentLatitude, currentLongitude),
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    if (currentAccuracy > 0f) {
                        Text(
                            text = String.format(Locale.US, "(±%.1fm)", currentAccuracy),
                            color = Color(0xFF86EFAC),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF15803D),
                        border = BorderStroke(0.5.dp, Color(0xFF86EFAC).copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = "Foto #$photoNumber",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                // Linha 2: Data/Hora com segundos + Região Administrativa
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR")).format(Date(currentTimestampMillis)),
                        color = Color(0xFFFEF08A),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "• $currentDynamicRa",
                        color = Color(0xFF67E8F9),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // =========================================================================
        // 3. BARRA DE ZOOM (Suporte contínuo a pinça com indicador visual e atalhos 1x, 2x, 3x)
        // =========================================================================
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.Black.copy(alpha = 0.75f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isScreenLandscape) 16.dp else 114.dp)
                .graphicsLayer { rotationZ = uiRotationDegrees }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom",
                    tint = Color(0xFF86EFAC),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(start = 2.dp)
                )

                val quickZoomLevels = listOf(1.0f, 2.0f, 3.0f, 5.0f).filter { it in minZoomRatio..maxZoomRatio }
                val activeLevels = if (quickZoomLevels.isEmpty()) listOf(1.0f) else quickZoomLevels

                activeLevels.forEach { level ->
                    val isSelected = kotlin.math.abs(currentZoomRatio - level) < 0.15f
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) Color(0xFF22C55E) else Color.Transparent,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable {
                                currentZoomRatio = level
                                activeCamera?.cameraControl?.setZoomRatio(level)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${level.toInt()}x",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Indicador dinâmico de zoom intermediário obtido pelo gesto de pinça
                val isIntermediate = activeLevels.none { kotlin.math.abs(currentZoomRatio - it) < 0.15f }
                if (isIntermediate) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF15803D),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC).copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1fx", currentZoomRatio),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 4. CONTROLES DE CAPTURA (Obturador, Galeria e Contador de Fotos)
        // Posicionados conforme a orientação: Lateral direita em Paisagem, Inferior em Retrato
        // =========================================================================
        if (isScreenLandscape) {
            // MODO PAISAGEM: Painel lateral à direita (alcance natural do polegar)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Badge contador
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier
                        .size(44.dp)
                        .graphicsLayer { rotationZ = uiRotationDegrees }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$capturedPhotosCount",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Botão de Disparo
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                        .clickable(enabled = !isCapturing) { triggerCapture() }
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }

                // Botão Galeria
                IconButton(
                    onClick = onPickFromGallery,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                        .graphicsLayer { rotationZ = uiRotationDegrees }
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Galeria",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        } else {
            // MODO RETRATO: Barra inferior centralizada
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão Galeria
                IconButton(
                    onClick = onPickFromGallery,
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .graphicsLayer { rotationZ = uiRotationDegrees }
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Galeria",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Botão de Disparo do Obturador
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                        .clickable(enabled = !isCapturing) { triggerCapture() }
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }

                // Contador de Fotos Capturadas
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.45f),
                    modifier = Modifier
                        .size(50.dp)
                        .graphicsLayer { rotationZ = uiRotationDegrees }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$capturedPhotosCount",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Diálogo opcional para inserção de Tag na foto (máximo 20 caracteres)
    pendingPhotoCapture?.let { pending ->
        AlertDialog(
            onDismissRequest = {
                onPhotoCaptured(
                    pending.uri,
                    pending.exactLat,
                    pending.exactLng,
                    pending.exactTime,
                    pending.exactRa,
                    null,
                    pending.orientationDegrees
                )
                Toast.makeText(context, "Foto salva no relatório e na Galeria!", Toast.LENGTH_SHORT).show()
                pendingPhotoCapture = null
                photoTagInput = ""
            },
            shape = RoundedCornerShape(20.dp),
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Sell,
                            contentDescription = "Tag",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Deseja colocar uma tag na foto?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Identifique o animal ou detalhe da foto para registro (limite de 20 caracteres):",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AppOutlinedTextField(
                        value = photoTagInput,
                        onValueChange = { photoTagInput = it.take(20) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("photo_tag_input_field"),
                        label = { Text("Tag da Foto") },
                        placeholder = { Text("Ex: Baio, Potro, Pata dir.") },
                        singleLine = true,
                        maxLength = 20,
                        supportingText = {
                            Text(
                                text = "${photoTagInput.length}/20 caracteres",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalTag = photoTagInput.trim().ifEmpty { null }
                        onPhotoCaptured(
                            pending.uri,
                            pending.exactLat,
                            pending.exactLng,
                            pending.exactTime,
                            pending.exactRa,
                            finalTag,
                            pending.orientationDegrees
                        )
                        Toast.makeText(
                            context,
                            if (finalTag != null) "Foto com tag salva no relatório e na Galeria!" else "Foto salva no relatório e na Galeria!",
                            Toast.LENGTH_SHORT
                        ).show()
                        pendingPhotoCapture = null
                        photoTagInput = ""
                    },
                    modifier = Modifier.testTag("confirm_photo_tag_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Salvar Tag", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        onPhotoCaptured(
                            pending.uri,
                            pending.exactLat,
                            pending.exactLng,
                            pending.exactTime,
                            pending.exactRa,
                            null,
                            pending.orientationDegrees
                        )
                        Toast.makeText(context, "Foto salva no relatório e na Galeria!", Toast.LENGTH_SHORT).show()
                        pendingPhotoCapture = null
                        photoTagInput = ""
                    },
                    modifier = Modifier.testTag("skip_photo_tag_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sem Tag")
                }
            }
        )
    }
}

private fun createFallbackFieldPhoto(
    context: Context,
    latitude: Double,
    longitude: Double,
    administrativeRegion: String,
    protocolNumber: String,
    timestamp: Long = System.currentTimeMillis(),
    photoNumber: Int = 1,
    isLandscape: Boolean = false
): File {
    val width = if (isLandscape) 1200 else 900
    val height = if (isLandscape) 900 else 1200
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint().apply {
        color = AndroidColor.rgb(18, 38, 30)
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val textPaint = Paint().apply {
        color = AndroidColor.rgb(240, 240, 240)
        textSize = if (isLandscape) 30f else 34f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    canvas.drawText("LEVANTAMENTO DE CAMPO • APOIO À FISCALIZAÇÃO", 60f, if (isLandscape) 160f else 200f, textPaint)

    textPaint.textSize = if (isLandscape) 24f else 28f
    textPaint.typeface = Typeface.DEFAULT
    textPaint.color = AndroidColor.rgb(254, 240, 138)
    canvas.drawText("Levantamento: $protocolNumber", 60f, if (isLandscape) 210f else 260f, textPaint)
    canvas.drawText("Registro Fotográfico nº $photoNumber", 60f, if (isLandscape) 260f else 310f, textPaint)
    canvas.drawText("Região Administrativa: $administrativeRegion", 60f, if (isLandscape) 310f else 360f, textPaint)

    textPaint.color = AndroidColor.rgb(134, 239, 172)
    canvas.drawText(String.format(Locale.US, "GPS Dinâmico: %.6f°, %.6f°", latitude, longitude), 60f, if (isLandscape) 360f else 410f, textPaint)
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
    canvas.drawText("Data/Hora: ${sdf.format(Date(timestamp))}", 60f, if (isLandscape) 410f else 460f, textPaint)

    val photosDir = File(context.cacheDir, "camera_raw").apply {
        if (!exists()) mkdirs()
    }
    val fallbackFile = File(photosDir, "FIELD_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
    FileOutputStream(fallbackFile).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return fallbackFile
}
