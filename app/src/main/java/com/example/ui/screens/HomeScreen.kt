package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.model.HorseAnatomyData
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import com.example.ui.components.AppOutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.components.HorseAnatomyAtlasView
import com.example.ui.components.FiveDomainsView
import com.example.data.model.FiveDomainsData
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.heightIn
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.AnexoFData
import com.example.data.model.AnexoFGuideCategory
import com.example.data.model.AnexoFGuideItem
import com.example.data.model.DfConstants
import com.example.data.model.HorseInspection
import com.example.ui.components.AnimalSpecies
import com.example.ui.components.SpeciesSelectorTabs
import com.example.ui.viewmodel.InspectionViewModel
import java.io.File

@Composable
fun HomeScreen(
    viewModel: InspectionViewModel,
    onNavigateToNewInspection: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
) {
    val inspections by viewModel.filteredInspections.collectAsStateWithLifecycle()
    val allInspections by viewModel.allInspections.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showInfoDialog by remember { mutableStateOf(false) }
    var showAuditorDialog by remember { mutableStateOf(false) }
    val currentAuditor = viewModel.auditorIdentification

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Barra de Identificação do Auditor Fiscal Ativo
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { showAuditorDialog = true }
                .testTag("home_auditor_bar"),
            color = if (currentAuditor.isNotBlank())
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (currentAuditor.isNotBlank())
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (currentAuditor.isNotBlank())
                                "Auditor Fiscal: $currentAuditor"
                            else
                                "Identificação do Auditor: Não informada",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Toque para alterar nome / matrícula para telas e relatórios",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (currentAuditor.isNotBlank()) "ATIVO" else "DEFINIR",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Species Selector Tabs (Cachorro, Cavalo, Gato, Galináceos e Outros)
        SpeciesSelectorTabs(
            selectedSpecies = viewModel.selectedSpecies,
            onSpeciesSelected = { species ->
                viewModel.selectSpecies(species)
            },
            onCustomSpeciesNamed = { name ->
                viewModel.setCustomSpecies(name)
            }
        )

        // Search Bar with Anexo F guide button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppOutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_field"),
                placeholder = { Text("Buscar por RA, tutor, levantamento ou notas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { showInfoDialog = true },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .testTag("button_open_anexo_f")
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_owl),
                    contentDescription = if (viewModel.selectedSpecies == AnimalSpecies.HORSE)
                        "Menu Conhecimento - Atlas Anatômico do Cavalo, 5 Domínios e Guia Técnico"
                    else
                        "Menu Conhecimento - Os 5 Domínios e Guia Técnico (${viewModel.selectedSpecies.displayName})",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main List
        if (inspections.isEmpty()) {
            EmptyState(
                hasAny = allInspections.isNotEmpty(),
                onStartNew = onNavigateToNewInspection
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(inspections, key = { _, item -> item.id }) { index, inspection ->
                    InspectionItemCard(
                        inspection = inspection,
                        index = index,
                        currentAuditorName = currentAuditor,
                        onClick = { onNavigateToDetail(inspection.id) },
                        onExportPdf = { viewModel.exportSinglePdf(inspection) }
                    )
                }
            }
        }
    }

    if (showInfoDialog) {
        DfSafetyInfoDialog(
            species = viewModel.selectedSpecies,
            onDismiss = { showInfoDialog = false }
        )
    }

    if (showAuditorDialog) {
        var tempName by remember { mutableStateOf(viewModel.auditorIdentification) }
        AlertDialog(
            onDismissRequest = { showAuditorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Identificação do Auditor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Informe seu nome ou matrícula. Ele será exibido em destaque no aplicativo e incluído nas assinaturas dos relatórios e PDFs gerados:",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AppOutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_auditor_input"),
                        label = { Text("Auditor Fiscal / Agente") },
                        placeholder = { Text("Ex: Auditor Silva • Matr. 123.456-X") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setAuditorId(tempName)
                        showAuditorDialog = false
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuditorDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun StatsSummaryCard(allInspections: List<HorseInspection>) {
    val totalAnimals = allInspections.sumOf { it.horseCount }
    val mistreatedCount = allInspections.sumOf { it.mistreatedHorseCount }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(label = "Vistorias", value = allInspections.size.toString())
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )
            StatItem(label = "Animais", value = totalAnimals.toString())
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )
            StatItem(
                label = "Com Alertas",
                value = mistreatedCount.toString(),
                color = if (mistreatedCount > 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 19.sp,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InspectionItemCard(
    inspection: HorseInspection,
    index: Int = 0,
    currentAuditorName: String = "",
    onClick: () -> Unit,
    onExportPdf: () -> Unit
) {
    val isEven = index % 2 == 0
    val cardBgColor = if (isEven) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag("inspection_card_${inspection.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardBgColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // CABEÇALHO DO CARD: Número de protocolo em destaque, Espécie e Botão de Ação Rápida PDF
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Levantamento: ${inspection.protocolNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${inspection.speciesEmoji} ${inspection.speciesDisplay}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onExportPdf,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("export_pdf_btn_${inspection.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Exportar PDF",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CORPO DO CARD: Foto e Dados Operacionais Lado a Lado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Coluna Esquerda: Thumbnail da foto
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val photoFile = File(inspection.photoPath)
                    if (photoFile.exists()) {
                        AsyncImage(
                            model = photoFile,
                            contentDescription = "Foto da fiscalização",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    if (inspection.allPhotoPaths.size > 1) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(topStart = 6.dp),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Text(
                                text = inspection.allPhotoPaths.size.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Coluna Direita: Informações de RA, Endereço e Animais
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Região Administrativa (RA)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "RA: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = inspection.administrativeRegion,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Endereço Residencial logo abaixo do campo RA
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "End. Residencial: ",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = inspection.residentialAddress,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Quantitativo de Animais e Alertas de Maus-Tratos
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            val animalUnit = if (inspection.isDog) "cão(ães)" else "animal(is)"
                            Text(
                                text = "${inspection.speciesEmoji} ${inspection.horseCount} $animalUnit",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (inspection.mistreatedHorseCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "⚠️ ${inspection.mistreatedHorseCount} com sinais",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB91C1C),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Auditor Fiscal Responsável da Vistoria
                    val cardAuditor = inspection.auditorName.ifBlank { currentAuditorName }
                    if (cardAuditor.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Auditor: $cardAuditor",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Tutor / Responsável
                    if (inspection.tutorName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tutor: ${inspection.tutorName}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // RODAPÉ DO CARD: Data em formato HORIZONTAL, sem corte, bem dividida
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Data da Fiscalização:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = inspection.formattedCaptureDate,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(hasAny: Boolean, onStartNew: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (hasAny) "Nenhuma fiscalização encontrada com estes filtros" else "Nenhuma fiscalização registrada",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (hasAny) "Tente limpar os termos de busca ou filtros de risco." else "Toque no botão abaixo para capturar fotos georreferenciadas com marca d'água automática e emitir relatórios em PDF.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (!hasAny) {
                Spacer(modifier = Modifier.height(20.dp))
                ExtendedFloatingActionButton(
                    onClick = onStartNew,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Registrar Primeira Fiscalização") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun DfSafetyInfoDialog(
    species: AnimalSpecies = AnimalSpecies.HORSE,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isHorse = species == AnimalSpecies.HORSE
    var mainGuideTab by remember(species) { mutableIntStateOf(0) }
    var selectedCategoryId by remember(species) {
        mutableStateOf<String?>(
            if (species == AnimalSpecies.DOG) "caes" else "all"
        )
    }
    var filterText by remember { mutableStateOf("") }

    val allCategories = remember(species) {
        if (species == AnimalSpecies.HORSE) {
            // Para a espécie equina, mantém os parâmetros de saúde e o Atlas de Anatomia exclusivo
            AnexoFData.categories.filter { it.id != "caes" }
        } else {
            // Para caninos e outras espécies, o atlas de cavalos é totalmente excluído
            AnexoFData.categories
        }
    }

    val filteredCategories = remember(allCategories, selectedCategoryId, filterText) {
        val base = if (selectedCategoryId == null || selectedCategoryId == "all") {
            allCategories
        } else {
            allCategories.filter { it.id == selectedCategoryId }
        }

        if (filterText.isBlank()) {
            base
        } else {
            val query = filterText.trim().lowercase()
            base.mapNotNull { cat ->
                val matchingItems = cat.items.filter { item ->
                    item.title.lowercase().contains(query) ||
                            item.description.lowercase().contains(query) ||
                            item.tag.lowercase().contains(query)
                }
                if (matchingItems.isNotEmpty()) {
                    cat.copy(items = matchingItems)
                } else null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 720.dp)
            .testTag("dialog_safety_info"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_owl),
                            contentDescription = if (isHorse) "Símbolo da Coruja - Atlas do Cavalo, 5 Domínios e Guia" else "Símbolo da Coruja - 5 Domínios e Guia Técnico (${species.displayName})",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isHorse) {
                            when (mainGuideTab) {
                                0 -> "OS 5 DOMÍNIOS DO BEM-ESTAR"
                                1 -> "ATLAS DE ANATOMIA DO CAVALO"
                                else -> "ANEXO F - GUIA PRÁTICO"
                            }
                        } else {
                            if (mainGuideTab == 0) "OS 5 DOMÍNIOS DO BEM-ESTAR"
                            else if (species == AnimalSpecies.DOG) "GUIA TÉCNICO DE BEM-ESTAR CANINO"
                            else "GUIA TÉCNICO DE BEM-ESTAR (${species.displayName.uppercase()})"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                    Text(
                        text = if (isHorse) {
                            when (mainGuideTab) {
                                0 -> "Garantias Fundamentais ao Bem-Estar Animal"
                                1 -> "Anatomia Externa do Cavalo (Página 2 do Atlas)"
                                else -> "Bem-Estar e Cuidados Básicos (Equinos & Geral)"
                            }
                        } else {
                            if (mainGuideTab == 0) "Garantias Fundamentais ao Bem-Estar Animal"
                            else "Parâmetros de Bem-Estar e Fiscalização (${species.displayName})"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isHorse) {
                            when (mainGuideTab) {
                                0 -> FiveDomainsData.FRAMEWORK_SOURCE
                                1 -> "Biblioteca AGPTEA • Estrutura Original Preservada"
                                else -> AnexoFData.SOURCE_CREDIT
                            }
                        } else {
                            if (mainGuideTab == 0) FiveDomainsData.FRAMEWORK_SOURCE
                            else AnexoFData.SOURCE_CREDIT
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                }

                if (isHorse && mainGuideTab == 1) {
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
                            .testTag("button_header_open_atlas")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Abrir Atlas Original em PDF",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isHorse) {
                    // Abas Principais da espécie Equina: 5 Domínios, Atlas e Guia Prático Anexo F
                    TabRow(
                        selectedTabIndex = mainGuideTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Tab(
                            selected = mainGuideTab == 0,
                            onClick = { mainGuideTab = 0 },
                            text = {
                                Text(
                                    text = "5 Domínios",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (mainGuideTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("tab_guide_five_domains")
                        )
                        Tab(
                            selected = mainGuideTab == 1,
                            onClick = { mainGuideTab = 1 },
                            text = {
                                Text(
                                    text = "Atlas Equino",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (mainGuideTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("tab_guide_horse_anatomy")
                        )
                        Tab(
                            selected = mainGuideTab == 2,
                            onClick = { mainGuideTab = 2 },
                            text = {
                                Text(
                                    text = "Anexo F",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (mainGuideTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("tab_guide_anexo_f")
                        )
                    }
                } else {
                    // Abas das Demais Espécies (ex: Caninos): 5 Domínios e Guia Prático (sem o atlas do cavalo)
                    TabRow(
                        selectedTabIndex = mainGuideTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Tab(
                            selected = mainGuideTab == 0,
                            onClick = { mainGuideTab = 0 },
                            text = {
                                Text(
                                    text = "5 Domínios",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (mainGuideTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("tab_guide_five_domains")
                        )
                        Tab(
                            selected = mainGuideTab == 1,
                            onClick = { mainGuideTab = 1 },
                            text = {
                                Text(
                                    text = if (species == AnimalSpecies.DOG) "Guia Canino" else "Guia Prático",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (mainGuideTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("tab_guide_anexo_f")
                        )
                    }
                }

                when {
                    mainGuideTab == 0 -> {
                        // Os 5 Domínios que devem ser garantidos aos animais
                        FiveDomainsView(modifier = Modifier.fillMaxWidth())
                    }
                    isHorse && mainGuideTab == 1 -> {
                        // Visualizador da Anatomia Externa do Cavalo (mantido exclusivamente para a espécie equina)
                        HorseAnatomyAtlasView(modifier = Modifier.fillMaxWidth())
                    }
                    else -> {
                    // Category Selector Chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryId == "all",
                                onClick = { selectedCategoryId = "all" },
                                label = { Text("Todos") },
                                modifier = Modifier.testTag("anexo_f_tab_all")
                            )
                        }
                        items(allCategories) { cat ->
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick = { selectedCategoryId = cat.id },
                                label = { Text(cat.title) },
                                modifier = Modifier.testTag("anexo_f_tab_${cat.id}")
                            )
                        }
                    }

                    // Filter search input
                    AppOutlinedTextField(
                        value = filterText,
                        onValueChange = { filterText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("anexo_f_search"),
                        placeholder = { Text("Filtrar parâmetros (ex: olhos, água, abrigo...)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            if (filterText.isNotEmpty()) {
                                IconButton(onClick = { filterText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpar", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // List of items
                    if (filteredCategories.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhum parâmetro encontrado para \"$filterText\".",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            filteredCategories.forEach { category ->
                                item(key = "header_${category.id}") {
                                    Column(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = category.title.uppercase(),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        Text(
                                            text = category.subtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                                        )
                                    }
                                }

                                items(category.items, key = { "${category.id}_${it.title}" }) { item ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.title,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = item.tag,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    },
    confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("anexo_f_close_button")
            ) {
                Text("Fechar", fontWeight = FontWeight.Bold)
            }
        }
    )
}
