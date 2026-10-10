package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AnatomicalPoint
import com.example.model.AnatomyRegionCrop
import com.example.model.HorseAnatomyData

@Composable
fun HorseAnatomyAtlasView(
    modifier: Modifier = Modifier
) {
    var selectedRegion by remember { mutableStateOf("Todas as Regiões") }
    var searchQuery by remember { mutableStateOf("") }
    var showFullScreenDialog by remember { mutableStateOf(false) }

    val currentCrop = remember(selectedRegion) {
        HorseAnatomyData.getCropForRegion(selectedRegion)
    }

    val filteredPoints = remember(selectedRegion, searchQuery) {
        val base = HorseAnatomyData.getPointsForRegion(selectedRegion)

        if (searchQuery.isBlank()) {
            base
        } else {
            val q = searchQuery.trim().lowercase()
            base.filter { point ->
                point.name.lowercase().contains(q) ||
                        point.description.lowercase().contains(q) ||
                        point.clinicalImportance.lowercase().contains(q) ||
                        point.region.lowercase().contains(q)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Seletor de Filtros Anatômicos (Recortes do Atlas)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cortes Anatômicos do Atlas:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${filteredPoints.size} ponto(s) listado(s)",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(HorseAnatomyData.regions) { region ->
                    val isSelected = selectedRegion == region
                    val pointCount = remember(region) {
                        HorseAnatomyData.getPointsForRegion(region).size
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedRegion = region },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = region,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = pointCount.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("chip_anatomy_${region.replace(" ", "_")}")
                    )
                }
            }
        }

        // Card do Recorte Anatômico Selecionado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("card_horse_anatomy_chart"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Cabeçalho do Card com Título Dinâmico do Corte
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CropFree,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentCrop.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = currentCrop.subtitle,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Botão para ampliar em tela cheia com zoom interativo
                    OutlinedButton(
                        onClick = { showFullScreenDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("button_fullscreen_anatomy")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Expandir visualizador",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ampliar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quadro do Recorte da Imagem Anatômica do Atlas Original
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .clickable { showFullScreenDialog = true }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = currentCrop,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "anatomy_crop_transition"
                    ) { crop ->
                        Image(
                            painter = painterResource(id = crop.drawableResId),
                            contentDescription = "Recorte anatômico do cavalo: ${crop.title}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(crop.aspectRatio)
                        )
                    }

                    // Selo de toque para zoom
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Toque para Zoom HD",
                                fontSize = 9.5.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Indicador de Região Ativa no topo
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = currentCrop.name,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentCrop.description,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }

        // Campo de Pesquisa Rápida de Pontos Anatômicos
        AppOutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("input_search_anatomy_point"),
            placeholder = { Text("Buscar ponto anatômico (ex: chanfro, casco, quartela, jarrete...)", fontSize = 11.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpar busca", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        // Lista de Pontos Anatômicos do Corte Selecionado
        if (filteredPoints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum ponto anatômico encontrado para \"$searchQuery\".",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPoints, key = { it.id }) { point ->
                    AnatomicalPointCard(point = point)
                }
            }
        }
    }

    // Modal de Tela Cheia com Zoom Interativo para leitura nítida das letras e linhas
    if (showFullScreenDialog) {
        Dialog(
            onDismissRequest = { showFullScreenDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true
            )
        ) {
            FullScreenAnatomyViewer(
                initialRegion = selectedRegion,
                onRegionChanged = { selectedRegion = it },
                onDismiss = { showFullScreenDialog = false }
            )
        }
    }
}

@Composable
fun AnatomicalPointCard(point: AnatomicalPoint) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_point_${point.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = point.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = point.region,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = point.description,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Importância na Fiscalização: ${point.clinicalImportance}",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Visualizador em Tela Cheia com Suporte a Zoom, Pan e Troca Rápida de Cortes Anatômicos.
 */
@Composable
fun FullScreenAnatomyViewer(
    initialRegion: String,
    onRegionChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var activeRegion by remember { mutableStateOf(initialRegion) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val crop = remember(activeRegion) {
        HorseAnatomyData.getCropForRegion(activeRegion)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1013))
            .testTag("dialog_fullscreen_anatomy")
    ) {
        // Barra Superior de Controles e Identificação
        Surface(
            color = Color.Black.copy(alpha = 0.85f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = crop.title,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Atlas Oficial de Anatomia do Cavalo • Pág. 2 (AGPTEA)",
                            color = Color.LightGray,
                            fontSize = 10.5.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Botão Zoom Out
                        IconButton(
                            onClick = {
                                scale = (scale - 0.5f).coerceAtLeast(1f)
                                if (scale == 1f) {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Diminuir zoom", tint = Color.White)
                        }

                        Text(
                            text = "${(scale * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Botão Zoom In
                        IconButton(
                            onClick = {
                                scale = (scale + 0.5f).coerceAtMost(5f)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar zoom", tint = Color.White)
                        }

                        // Botão Reset
                        IconButton(
                            onClick = {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Redefinir visualização", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Botão Fechar
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar tela cheia", tint = Color.White)
                        }
                    }
                }

                // Seletor de Cortes Anatômicos dentro da Tela Cheia
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(HorseAnatomyData.regions) { region ->
                        val isSelected = activeRegion == region
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    activeRegion = region
                                    onRegionChanged(region)
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                        ) {
                            Text(
                                text = region,
                                fontSize = 10.5.sp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Área da Imagem do Corte Anatômico com detecção de gestos (Pinch-to-zoom e Pan)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, bottom = 44.dp)
                .pointerInput(activeRegion) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        if (scale > 1f) {
                            val maxOffsetX = 900f * (scale - 1f)
                            val maxOffsetY = 900f * (scale - 1f)
                            offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                            offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = crop.drawableResId),
                contentDescription = "Corte Anatômico: ${crop.title}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            )
        }

        // Barra Inferior de Informações e Legenda
        Surface(
            color = Color.Black.copy(alpha = 0.85f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 Arraste com dois dedos para navegar ou use (+) e (-) para ampliar letras e linhas de chamada.",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Corte: ${crop.name}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                )
            }
        }
    }
}
