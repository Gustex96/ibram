package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import java.io.File
import kotlin.math.max

/**
 * Visualizador Interativo de Fotografias de Campo com Suporte Completo a Pinça dos Dedos (Pinch-to-Zoom).
 * Permite ao auditor fiscal ampliar detalhes (ferimentos, escoriações, estado nutricional, marcas e
 * coordenadas da marca d'água georreferenciada) utilizando gestos com os dedos ou botões rápidos.
 */
@Composable
fun ZoomablePhotoDialog(
    photoPaths: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    if (photoPaths.isEmpty()) {
        onDismiss()
        return
    }

    var currentIndex by remember {
        mutableIntStateOf(initialIndex.coerceIn(0, max(0, photoPaths.lastIndex)))
    }
    val currentPath = photoPaths.getOrNull(currentIndex) ?: ""

    // Estados do Zoom e Panning com os dedos
    var scale by remember(currentIndex) { mutableFloatStateOf(1f) }
    var offsetX by remember(currentIndex) { mutableFloatStateOf(0f) }
    var offsetY by remember(currentIndex) { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("zoomable_photo_dialog")
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val containerWidthPx = with(density) { maxWidth.toPx() }
                val containerHeightPx = with(density) { maxHeight.toPx() }

                val file = File(currentPath)
                if (file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = "Fotografia fiscalizatória georreferenciada",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offsetX
                                translationY = offsetY
                            }
                            .pointerInput(currentIndex) {
                                detectTapGestures(
                                    onDoubleTap = { tapOffset ->
                                        if (scale > 1.2f) {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        } else {
                                            scale = 2.8f
                                            val centerX = containerWidthPx / 2f
                                            val centerY = containerHeightPx / 2f
                                            val targetX = (centerX - tapOffset.x) * (2.8f - 1f)
                                            val targetY = (centerY - tapOffset.y) * (2.8f - 1f)
                                            val maxX = (containerWidthPx * (2.8f - 1f)) / 2f
                                            val maxY = (containerHeightPx * (2.8f - 1f)) / 2f
                                            offsetX = targetX.coerceIn(-maxX, maxX)
                                            offsetY = targetY.coerceIn(-maxY, maxY)
                                        }
                                    }
                                )
                            }
                            .pointerInput(currentIndex) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val newScale = (scale * zoom).coerceIn(1f, 8f)
                                    scale = newScale
                                    if (scale > 1f) {
                                        val maxX = (containerWidthPx * (scale - 1f)) / 2f
                                        val maxY = (containerHeightPx * (scale - 1f)) / 2f
                                        offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                                        offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "Arquivo da fotografia não encontrado",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            // Barra Superior de Informações e Botão Fechar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "📸 Foto ${currentIndex + 1} de ${photoPaths.size}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (scale > 1.05f) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFF1B5E20),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🔍 ${String.format(java.util.Locale.US, "%.1fx", scale)}",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (scale > 1.05f) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Restaurar tamanho original",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar visualização",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Setas de Navegação Entre Fotos (se houver mais de uma)
            if (photoPaths.size > 1) {
                if (currentIndex > 0) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .size(46.dp)
                    ) {
                        IconButton(
                            onClick = {
                                currentIndex--
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Foto anterior",
                                tint = Color.White
                            )
                        }
                    }
                }

                if (currentIndex < photoPaths.lastIndex) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .size(46.dp)
                    ) {
                        IconButton(
                            onClick = {
                                currentIndex++
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Próxima foto",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Barra Inferior de Controles de Zoom e Dica de Uso
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dica informativa para o auditor
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Use o movimento de pinça dos dedos para ampliar ou reduzir detalhes. Arraste para inspecionar.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Botões de Zoom Rápido (+ / - / 1x)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val newScale = (scale * 0.7f).coerceIn(1f, 8f)
                                    scale = newScale
                                    if (newScale == 1f) {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                },
                                enabled = scale > 1f
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Reduzir zoom",
                                    tint = if (scale > 1f) Color.White else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val newScale = (scale * 1.5f).coerceIn(1f, 8f)
                                    scale = newScale
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Ampliar zoom",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZoomablePhotoDialog(
    photoPath: String,
    onDismiss: () -> Unit
) {
    ZoomablePhotoDialog(
        photoPaths = listOf(photoPath),
        initialIndex = 0,
        onDismiss = onDismiss
    )
}
