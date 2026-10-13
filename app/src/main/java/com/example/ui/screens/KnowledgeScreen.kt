package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AnexoFData
import com.example.data.model.FiveDomainsData
import com.example.model.HorseAnatomyData
import com.example.ui.components.AnimalSpecies
import com.example.ui.components.FiveDomainsView
import com.example.ui.components.HorseAnatomyAtlasView
import com.example.ui.components.SpeciesSelectorTabs
import com.example.ui.viewmodel.InspectionViewModel

/**
 * Aba Conhecimento: Apresenta os 5 Domínios que devem ser garantidos aos animais,
 * o Atlas Anatômico do Cavalo (exclusivo para equinos) e os Guias Técnicos Operacionais.
 */
@Composable
fun KnowledgeScreen(
    viewModel: InspectionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val species = viewModel.selectedSpecies
    val isHorse = species == AnimalSpecies.HORSE

    // 0: 5 Domínios (Garantias aos animais), 1: Atlas (se equino) ou Guia Canino, 2: Guia Prático Anexo F (se equino)
    var selectedTab by remember(species) { mutableIntStateOf(0) }

    val allCategories = remember(species) {
        AnexoFData.categories.filter { cat ->
            if (species == AnimalSpecies.HORSE) cat.id != "caes" else cat.id != "equinos"
        }
    }
    var selectedCategoryId by remember(species) {
        mutableStateOf(allCategories.firstOrNull()?.id)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("screen_knowledge")
    ) {
        // Cabeçalho da Aba Conhecimento
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_owl),
                            contentDescription = "Base de Conhecimento",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BASE DE CONHECIMENTO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Garantias Fundamentais & Parâmetros de Fiscalização",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isHorse && selectedTab == 1) {
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(HorseAnatomyData.ATLAS_DOCUMENT_URL)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Não foi possível abrir o link do Atlas.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("knowledge_open_pdf_atlas")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Abrir Atlas em PDF",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Seletor de Espécie
        SpeciesSelectorTabs(
            selectedSpecies = viewModel.selectedSpecies,
            onSpeciesSelected = { viewModel.selectSpecies(it) },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Abas da Base de Conhecimento
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "5 Domínios",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("tab_knowledge_five_domains")
            )

            if (isHorse) {
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Atlas Equino",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_knowledge_horse_atlas")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "Guia Prático",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_knowledge_anexo_f")
                )
            } else {
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Guia Canino",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_knowledge_anexo_f")
                )
            }
        }

        // Conteúdo da Aba
        when {
            selectedTab == 0 -> {
                // OS 5 DOMÍNIOS QUE DEVEM SER GARANTIDOS AOS ANIMAIS
                FiveDomainsView(modifier = Modifier.fillMaxSize())
            }
            isHorse && selectedTab == 1 -> {
                // ATLAS ANATÔMICO DO CAVALO (EXCLUSIVO PARA EQUINOS)
                HorseAnatomyAtlasView(modifier = Modifier.fillMaxSize())
            }
            else -> {
                // GUIA TÉCNICO E PRÁTICO (ANEXO F)
                Column(modifier = Modifier.fillMaxSize()) {
                    if (allCategories.size > 1) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allCategories, key = { it.id }) { cat ->
                                val isSelected = cat.id == selectedCategoryId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategoryId = cat.id },
                                    label = { Text(cat.title, fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    val activeCategory = allCategories.firstOrNull { it.id == selectedCategoryId } ?: allCategories.firstOrNull()
                    if (activeCategory != null) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(activeCategory.items) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.description,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
