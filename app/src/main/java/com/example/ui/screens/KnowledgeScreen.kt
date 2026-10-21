package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.viewmodel.InspectionViewModel

/**
 * Aba Conhecimento: Apresenta os 5 Domínios que devem ser garantidos aos animais,
 * o Atlas Anatômico do Cavalo (exclusivo para equinos) e os Guias Técnicos Operacionais.
 * Refatorada para a paleta verde institucional e cartões brancos limpos.
 */
@Composable
fun KnowledgeScreen(
    viewModel: InspectionViewModel,
    modifier: Modifier = Modifier,
    onSwitchSpecies: () -> Unit = {}
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
            .background(Color(0xFFF4FAF6))
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("screen_knowledge")
    ) {
        // 1. Cabeçalho Institucional de Conhecimento (Card Branco Refinado)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF14532D),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_owl),
                            contentDescription = "Base de Conhecimento",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GUIA & CONHECIMENTO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color(0xFF14532D),
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFD1FAE5),
                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = "Garantias Fundamentais & PPBEA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF14532D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "Parâmetros técnicos para avaliação de bem-estar",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Botão Trocar Espécie
                    Surface(
                        onClick = onSwitchSpecies,
                        shape = CircleShape,
                        color = Color(0xFF14532D),
                        shadowElevation = 2.dp,
                        modifier = Modifier.testTag("btn_knowledge_switch_species")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Trocar Espécie",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Trocar Espécie",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Botão para abrir o Manual Oficial do CRMV-MS (ANEXO D - PPBEA)
                    Surface(
                        onClick = {
                            try {
                                val manualUrl = "https://crmvms.org.br/wp-content/uploads/2022/08/MANUAL-DE-VERIFICACAO-INICIAL-DE-MAUS-TRATOS-A-CAES-E-GATOS.pdf"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(manualUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Não foi possível abrir o link do Manual.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = CircleShape,
                        color = Color(0xFFD1FAE5),
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("knowledge_open_pdf_manual")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Abrir Manual PPBEA (PDF)",
                                tint = Color(0xFF14532D),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (isHorse && selectedTab == 1) {
                        Surface(
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
                            shape = CircleShape,
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("knowledge_open_pdf_atlas")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "Abrir Atlas em PDF",
                                    tint = Color(0xFF14532D),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Seletor de Abas Estilo Pílula Verde Institucional
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Aba 1: 5 Domínios
                val isTab0 = selectedTab == 0
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_knowledge_five_domains")
                        .clickable { selectedTab = 0 },
                    shape = RoundedCornerShape(9.dp),
                    color = if (isTab0) Color(0xFFA7F3D0).copy(alpha = 0.85f) else Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "5 Domínios",
                            fontSize = 12.sp,
                            fontWeight = if (isTab0) FontWeight.Bold else FontWeight.Medium,
                            color = if (isTab0) Color(0xFF022C22) else Color(0xFF64748B)
                        )
                    }
                }

                if (isHorse) {
                    // Aba 2: Atlas Equino
                    val isTab1 = selectedTab == 1
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_knowledge_horse_atlas")
                            .clickable { selectedTab = 1 },
                        shape = RoundedCornerShape(9.dp),
                        color = if (isTab1) Color(0xFFA7F3D0).copy(alpha = 0.85f) else Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Atlas Equino",
                                fontSize = 12.sp,
                                fontWeight = if (isTab1) FontWeight.Bold else FontWeight.Medium,
                                color = if (isTab1) Color(0xFF022C22) else Color(0xFF64748B)
                            )
                        }
                    }

                    // Aba 3: Guia Prático
                    val isTab2 = selectedTab == 2
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_knowledge_anexo_f")
                            .clickable { selectedTab = 2 },
                        shape = RoundedCornerShape(9.dp),
                        color = if (isTab2) Color(0xFFA7F3D0).copy(alpha = 0.85f) else Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Guia Prático",
                                fontSize = 12.sp,
                                fontWeight = if (isTab2) FontWeight.Bold else FontWeight.Medium,
                                color = if (isTab2) Color(0xFF022C22) else Color(0xFF64748B)
                            )
                        }
                    }
                } else {
                    // Aba 2: Guia Canino
                    val isTab1 = selectedTab == 1
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_knowledge_anexo_f")
                            .clickable { selectedTab = 1 },
                        shape = RoundedCornerShape(9.dp),
                        color = if (isTab1) Color(0xFFA7F3D0).copy(alpha = 0.85f) else Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Guia Canino",
                                fontSize = 12.sp,
                                fontWeight = if (isTab1) FontWeight.Bold else FontWeight.Medium,
                                color = if (isTab1) Color(0xFF022C22) else Color(0xFF64748B)
                            )
                        }
                    }
                }
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(allCategories, key = { it.id }) { cat ->
                                val isSelected = cat.id == selectedCategoryId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategoryId = cat.id },
                                    label = { Text(cat.title, fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                                        selectedLabelColor = Color(0xFF022C22),
                                        containerColor = Color.White,
                                        labelColor = Color(0xFF475569)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isSelected) Color(0xFF14532D).copy(alpha = 0.3f) else Color(0xFFE2E8F0),
                                        enabled = true,
                                        selected = isSelected
                                    )
                                )
                            }
                        }
                    }

                    val activeCategory = allCategories.firstOrNull { it.id == selectedCategoryId } ?: allCategories.firstOrNull()
                    if (activeCategory != null) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 88.dp)
                        ) {
                            items(activeCategory.items) { item ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    shadowElevation = 1.dp
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = Color(0xFF14532D)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.description,
                                            fontSize = 12.sp,
                                            color = Color(0xFF334155),
                                            lineHeight = 16.5.sp
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
