package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnimalSpecies
import com.example.ui.viewmodel.InspectionViewModel

/**
 * Screen displaying species options using the official Material 3
 * Multi-Browse Carousel (HorizontalMultiBrowseCarousel) per:
 * https://m3.material.io/components/carousel/overview
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesSelectionScreen(
    viewModel: InspectionViewModel,
    onSpeciesSelectedAndNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var customSpeciesInput by remember { mutableStateOf("") }
    var customSpeciesError by remember { mutableStateOf(false) }

    val speciesList = remember {
        listOf(
            AnimalSpecies.GATO,
            AnimalSpecies.CACHORRO,
            AnimalSpecies.CAVALO,
            AnimalSpecies.GALINACEOS,
            AnimalSpecies.OUTROS
        )
    }

    var highlightedSpecies by remember { mutableStateOf(speciesList[0]) }
    val carouselState = rememberCarouselState { speciesList.size }

    fun handleSpeciesSelection(species: AnimalSpecies) {
        highlightedSpecies = species
        if (species == AnimalSpecies.OUTROS) {
            customSpeciesInput = ""
            customSpeciesError = false
            showCustomDialog = true
        } else {
            viewModel.selectSpecies(species)
            onSpeciesSelectedAndNavigateToProfile()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
    ) {
        // App Header Bar
        Surface(
            color = Color(0xFF00684A),
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 3.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF00684A), Color(0xFF004D36))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Brasão Fiscalização DF",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FISCALIZAÇÃO DF",
                                color = Color(0xFFFFB703),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Bem-Estar Animal",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selecione a espécie para iniciar a vistoria técnica",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section Title: Material 3 Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Espécies de Animais",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Deslize o carrossel e toque na espécie desejada",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "5 opções",
                    color = Color(0xFF00684A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Official Material 3 Multi-Browse Carousel (https://m3.material.io/components/carousel/overview)
        HorizontalMultiBrowseCarousel(
            state = carouselState,
            preferredItemWidth = 200.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .testTag("m3_multi_browse_carousel")
        ) { itemIndex ->
            val species = speciesList[itemIndex]
            M3CarouselCard(
                species = species,
                onClick = { handleSpeciesSelection(species) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Overview Card of the current active species
        val activeSpecies = highlightedSpecies

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("active_species_summary_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = when (activeSpecies) {
                                AnimalSpecies.GATO -> Color(0xFFFFF7ED)
                                AnimalSpecies.CACHORRO -> Color(0xFFEFF6FF)
                                AnimalSpecies.CAVALO -> Color(0xFFECFDF5)
                                AnimalSpecies.GALINACEOS -> Color(0xFFFEF2F2)
                                AnimalSpecies.OUTROS -> Color(0xFFF5F3FF)
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = activeSpecies.emoji, fontSize = 24.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Espécie Selecionada:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = activeSpecies.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    // Direct Action Button
                    Button(
                        onClick = { handleSpeciesSelection(activeSpecies) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00684A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_select_active_species")
                    ) {
                        Text(
                            text = if (activeSpecies == AnimalSpecies.OUTROS) "Digitar" else "Avançar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Legal basis guideline snippet
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF00684A),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when (activeSpecies) {
                                AnimalSpecies.GATO -> "Protocolo felino: Escore corporal, espaço vertical, caixa de areia, vacinação e esterilização."
                                AnimalSpecies.CACHORRO -> "Protocolo canino: Escore corporal, contenção segura, acesso à água, abrigo contra intempéries."
                                AnimalSpecies.CAVALO -> "Protocolo equino: Ferrageamento, arreamento, carga de tração, pasto e hidratação contínua."
                                AnimalSpecies.GALINACEOS -> "Protocolo avícola: Densidade no galinheiro, ventilação, poleiros, sanidade e nutrição."
                                AnimalSpecies.OUTROS -> "Protocolo customizado: Digite a espécie para registrar a ação fiscal de acordo com a CFMV 1.236/18."
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Compliance Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Resolução CFMV nº 1.236/2018 • Fiscalização do Distrito Federal",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Floating Modal Dialog for "Outros"
    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🐾 ",
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Especificar Outra Espécie",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Digite o nome da espécie animal que está sendo vistoriada nesta ação fiscal:",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = customSpeciesInput,
                        onValueChange = {
                            customSpeciesInput = it
                            if (it.isNotBlank()) customSpeciesError = false
                        },
                        label = { Text("Nome da Espécie") },
                        placeholder = { Text("Ex: Suínos, Bovinos, Ovinos, Caprinos...") },
                        isError = customSpeciesError,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_species_input")
                    )
                    if (customSpeciesError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Por favor, informe o nome da espécie para prosseguir.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = customSpeciesInput.trim()
                        if (trimmed.isBlank()) {
                            customSpeciesError = true
                        } else {
                            showCustomDialog = false
                            viewModel.selectSpecies(AnimalSpecies.OUTROS, customName = trimmed)
                            onSpeciesSelectedAndNavigateToProfile()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00684A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_species_button")
                ) {
                    Text("Confirmar e Abrir Perfil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCustomDialog = false },
                    modifier = Modifier.testTag("cancel_species_button")
                ) {
                    Text("Cancelar", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }
}

/**
 * An individual carousel item styled according to the Material 3 Carousel specifications:
 * Uses maskClip(MaterialTheme.shapes.extraLarge) inside CarouselItemScope so it smoothly
 * morphs between large, medium, and small states as the user scrolls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun androidx.compose.material3.carousel.CarouselItemScope.M3CarouselCard(
    species: AnimalSpecies,
    onClick: () -> Unit
) {
    val gradientColors = when (species) {
        AnimalSpecies.GATO -> listOf(Color(0xFFB45309), Color(0xFF78350F))
        AnimalSpecies.CACHORRO -> listOf(Color(0xFF1D4ED8), Color(0xFF1E3A8A))
        AnimalSpecies.CAVALO -> listOf(Color(0xFF047857), Color(0xFF064E3B))
        AnimalSpecies.GALINACEOS -> listOf(Color(0xFFB91C1C), Color(0xFF7F1D1D))
        AnimalSpecies.OUTROS -> listOf(Color(0xFF6D28D9), Color(0xFF4C1D95))
    }

    val subtitle = when (species) {
        AnimalSpecies.GATO -> "Felinos domésticos"
        AnimalSpecies.CACHORRO -> "Caninos domésticos"
        AnimalSpecies.CAVALO -> "Equinos de tração/pasto"
        AnimalSpecies.GALINACEOS -> "Aves e criações"
        AnimalSpecies.OUTROS -> "Personalizado"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .maskClip(MaterialTheme.shapes.extraLarge)
            .clickable { onClick() }
            .testTag("carousel_item_${species.name.lowercase()}")
    ) {
        // Vibrant background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
        )

        // Center prominent animal motif
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.size(86.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = species.emoji,
                        fontSize = 48.sp
                    )
                }
            }
        }

        // Top category pill
        Surface(
            color = Color.Black.copy(alpha = 0.35f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = species.name,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Dark gradient scrim at the bottom for text legibility (M3 style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Bottom text overlay (Title, Subtitle, Arrow Icon)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = species.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (species == AnimalSpecies.OUTROS) Icons.Default.Edit else Icons.Default.ChevronRight,
                            contentDescription = "Selecionar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
