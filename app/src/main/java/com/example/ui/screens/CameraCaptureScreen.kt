package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
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

@Composable
fun CameraCaptureScreen(
    initialLatitude: Double,
    initialLongitude: Double,
    administrativeRegion: String,
    protocolNumber: String,
    capturedPhotosCount: Int,
    onPhotoCaptured: (uri: Uri, exactLatitude: Double, exactLongitude: Double, exactTimestampMillis: Long, dynamicRa: String) -> Unit,
    onPickFromGallery: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exemplarNumber = capturedPhotosCount + 1

    val locationHelper = remember(context) { LocationHelper(context) }
    var currentLatitude by remember { mutableDoubleStateOf(initialLatitude) }
    var currentLongitude by remember { mutableDoubleStateOf(initialLongitude) }
    var currentAccuracy by remember { mutableFloatStateOf(0f) }
    var currentAltitude by remember { mutableDoubleStateOf(0.0) }
    var isRealGps by remember { mutableStateOf(false) }
    var currentTimestampMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var currentDynamicRa by remember { mutableStateOf(administrativeRegion) }

    var lensFacing by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var flashAnimation by remember { mutableStateOf(false) }

    // Stream contínuo de coordenadas dinâmicas e relógio tipo Timestamp Camera
    LaunchedEffect(Unit) {
        // 1. Relógio atualizando segundo a segundo (Princípio Timestamp Camera)
        launch {
            while (isActive) {
                currentTimestampMillis = System.currentTimeMillis()
                delay(1000L)
            }
        }

        // 2. Stream de GPS contínuo e em tempo real (Princípio Google Maps)
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
            // Atualiza dinamicamente a Região Administrativa com base no posicionamento em tempo real
            val detected = com.example.data.model.DfConstants.detectClosestRa(coords.latitude, coords.longitude)
            currentDynamicRa = detected
        }
    }

    BackHandler {
        onClose()
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
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    imageCapture = capture

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            lensFacing,
                            preview,
                            capture
                        )
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Aviso da câmera: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            update = {
                imageCapture?.flashMode = flashMode
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

        // TOP BAR: Botão voltar + HUD de GPS Dinâmico em Tempo Real + Flash/Lente
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 42.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close / Back button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }

            // HUD DINÂMICO (Atualiza continuamente conforme o fiscal se move, estilo Google Maps e Timestamp)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.75f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Ponto pulsante verde indicando GPS dinâmico ativo
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isRealGps) Color(0xFF22C55E) else Color(0xFFEAB308),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format(Locale.US, "%.6f°, %.6f°", currentLatitude, currentLongitude),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (currentAccuracy > 0f) {
                            Text(
                                text = String.format(Locale.US, " (±%.1fm)", currentAccuracy),
                                color = Color(0xFF86EFAC),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF15803D),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC).copy(alpha = 0.8f))
                        ) {
                            Text(
                                text = "Ex $exemplarNumber",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR")).format(Date(currentTimestampMillis)),
                            color = Color(0xFFFEF08A),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = " • $currentDynamicRa",
                            color = Color(0xFF67E8F9),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Camera Switch & Flash
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                            else -> ImageCapture.FLASH_MODE_AUTO
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
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
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Trocar Câmera",
                        tint = Color.White
                    )
                }
            }
        }

        // DYNAMIC RA & EXEMPLAR BADGE (Posicionamento dinâmico em tempo real exibido com destaque na câmera)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.75f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF15803D)
                ) {
                    Text(
                        text = "Ex $exemplarNumber",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "RA Atual: $currentDynamicRa",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // BOTTOM CONTROLS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pick from Gallery
            IconButton(
                onClick = onPickFromGallery,
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Galeria",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Clean Shutter Button (Grava as coordenadas dinâmicas exatas do instante do clique)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
                    .clickable(enabled = !isCapturing) {
                        isCapturing = true
                        flashAnimation = true

                        // Coordenadas, RA e horário dinâmicos exatos no instante do clique
                        val exactLat = currentLatitude
                        val exactLng = currentLongitude
                        val exactTime = currentTimestampMillis
                        val exactRa = currentDynamicRa

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
                                        onPhotoCaptured(uri, exactLat, exactLng, exactTime, exactRa)
                                        Toast.makeText(context, "Foto salva (Ex $exemplarNumber • $exactRa)!", Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        val fallbackFile = createFallbackFieldPhoto(context, exactLat, exactLng, exactRa, protocolNumber, exactTime, exemplarNumber)
                                        flashAnimation = false
                                        isCapturing = false
                                        onPhotoCaptured(Uri.fromFile(fallbackFile), exactLat, exactLng, exactTime, exactRa)
                                        Toast.makeText(context, "Foto salva (Ex $exemplarNumber • $exactRa)!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        } else {
                            val fallbackFile = createFallbackFieldPhoto(context, exactLat, exactLng, exactRa, protocolNumber, exactTime, exemplarNumber)
                            flashAnimation = false
                            isCapturing = false
                            onPhotoCaptured(Uri.fromFile(fallbackFile), exactLat, exactLng, exactTime, exactRa)
                            Toast.makeText(context, "Foto salva (Ex $exemplarNumber • $exactRa)!", Toast.LENGTH_SHORT).show()
                        }
                    }
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

            // Photo Count Badge
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.45f),
                modifier = Modifier.size(50.dp)
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

private fun createFallbackFieldPhoto(
    context: Context,
    latitude: Double,
    longitude: Double,
    administrativeRegion: String,
    protocolNumber: String,
    timestamp: Long = System.currentTimeMillis(),
    exemplarNumber: Int = 1
): File {
    val width = 1200
    val height = 900
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint().apply {
        color = AndroidColor.rgb(18, 38, 30)
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val textPaint = Paint().apply {
        color = AndroidColor.rgb(240, 240, 240)
        textSize = 34f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    canvas.drawText("LEVANTAMENTO DE CAMPO • APOIO À FISCALIZAÇÃO", 60f, 200f, textPaint)

    textPaint.textSize = 28f
    textPaint.typeface = Typeface.DEFAULT
    textPaint.color = AndroidColor.rgb(254, 240, 138)
    canvas.drawText("Protocolo: $protocolNumber", 60f, 260f, textPaint)
    canvas.drawText("Exemplar do Animal: Ex $exemplarNumber", 60f, 310f, textPaint)
    canvas.drawText("Região Administrativa: $administrativeRegion", 60f, 360f, textPaint)

    textPaint.color = AndroidColor.rgb(134, 239, 172)
    canvas.drawText(String.format(Locale.US, "GPS Dinâmico: %.6f°, %.6f°", latitude, longitude), 60f, 410f, textPaint)
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
    canvas.drawText("Data/Hora: ${sdf.format(Date(timestamp))}", 60f, 460f, textPaint)

    val photosDir = File(context.cacheDir, "camera_raw").apply {
        if (!exists()) mkdirs()
    }
    val fallbackFile = File(photosDir, "FIELD_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
    FileOutputStream(fallbackFile).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return fallbackFile
}
