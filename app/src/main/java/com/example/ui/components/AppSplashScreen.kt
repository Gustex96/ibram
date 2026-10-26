package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Tela Inicial conforme mockup LEVO.png:
 * Exibe a arte nativa do Cerrado ao fundo e o Carrossel Multinavegação Material 3
 * posicionado na parte inferior da tela, com fotos reais das espécies
 * (Cachorro, Cavalo, Gato, Galináceos e Outros).
 */
@Composable
fun AppSplashScreen(
    initialSpecies: AnimalSpecies = AnimalSpecies.DOG,
    onStart: (AnimalSpecies, String?) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var showOtherSpeciesDialog by remember { mutableStateOf(false) }

    val carouselListState = rememberLazyListState()

    // Ordem das espécies conforme o exemplo de LEVO.png (Cachorro primeiro, Cavalo, Gato, Galináceos, Outros)
    val speciesList = remember {
        listOf(
            AnimalSpecies.DOG,
            AnimalSpecies.HORSE,
            AnimalSpecies.CAT,
            AnimalSpecies.POULTRY,
            AnimalSpecies.OUTROS
        )
    }

    LaunchedEffect(Unit) {
        isVisible = true
        val targetIndex = speciesList.indexOf(initialSpecies).coerceAtLeast(0)
        carouselListState.scrollToItem(targetIndex)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_splash_screen")
    ) {
        // 1. Arte Vetorial do Cerrado ao Fundo (Conforme LEVO.png)
        Image(
            painter = painterResource(id = R.drawable.bg_splash_vector),
            contentDescription = "Paisagem do Cerrado Brasileiro",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Nome do Aplicativo com o Símbolo e Carrossel Multinavegação Material 3
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600)) + slideInVertically(
                animationSpec = tween(600),
                initialOffsetY = { it / 2 }
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            val snapBehavior = rememberSnapFlingBehavior(lazyListState = carouselListState)

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Nome do aplicativo com o símbolo posicionado em cima do menu carrossel
                Surface(
                    color = Color(0xFF141E15).copy(alpha = 0.88f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF69F0AE).copy(alpha = 0.65f)),
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth()
                        .testTag("app_brand_header_above_carousel")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1B4D36),
                            border = BorderStroke(1.8.dp, Color(0xFF69F0AE)),
                            shadowElevation = 6.dp,
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_protection_icon),
                                    contentDescription = "Emblema do Aplicativo LEVO",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "LEVO",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.5.sp
                            )

                            Surface(
                                color = Color(0xFF2E7D32),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "AUDITORIA • DF",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Selecione a espécie para iniciar a fiscalização:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFC8E6C9),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Menu Carrossel
                LazyRow(
                    state = carouselListState,
                    flingBehavior = snapBehavior,
                    contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("species_multi_browse_carousel")
                ) {
                    items(speciesList, key = { it.id }) { species ->
                        M3CarouselCard(
                            species = species,
                            onClick = {
                                if (species == AnimalSpecies.OUTROS) {
                                    showOtherSpeciesDialog = true
                                } else {
                                    onStart(species, null)
                                }
                            }
                        )
                    }
                }
            }
        }

        // 3. Campo de Digitação Flutuante quando "Outros" é escolhido
        if (showOtherSpeciesDialog) {
            FloatingOtherSpeciesDialog(
                onDismiss = { showOtherSpeciesDialog = false },
                onConfirm = { typedSpecies ->
                    showOtherSpeciesDialog = false
                    onStart(AnimalSpecies.OUTROS, typedSpecies)
                }
            )
        }
    }
}

/**
 * Card do Carrossel Multinavegação Material 3 conforme LEVO.png:
 * Card amplo com cantos arredondados (32dp), foto real com ContentScale.Crop,
 * gradiente de sombra escuro inferior e textos alinhados à esquerda (Título e Supporting Text).
 */
@Composable
private fun M3CarouselCard(
    species: AnimalSpecies,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(290.dp)
            .height(270.dp)
            .clip(RoundedCornerShape(32.dp))
            .clickable(onClick = onClick)
            .testTag("carousel_card_${species.id}"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2619)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagem Real da Espécie
            Image(
                painter = painterResource(id = species.realImageRes),
                contentDescription = species.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradiente Scrim na parte inferior para contraste dos textos
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x10000000),
                                Color(0x66000000),
                                Color(0xCC000000)
                            ),
                            startY = 180f
                        )
                    )
            )

            // Textos no Canto Inferior Esquerdo (Conforme imagem LEVO.png)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            ) {
                Text(
                    text = species.displayName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = when (species) {
                        AnimalSpecies.DOG -> "Cães e caninos"
                        AnimalSpecies.HORSE -> "Equinos e carroças"
                        AnimalSpecies.CAT -> "Felinos e felídeos"
                        AnimalSpecies.POULTRY -> "Aves e galináceos"
                        AnimalSpecies.OUTROS -> "Personalizar espécie"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }
    }
}
