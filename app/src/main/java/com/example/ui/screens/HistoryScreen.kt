package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WarningAmber
import com.example.util.DatabaseBackupManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.example.ui.components.AppOutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.HorseInspection
import com.example.ui.components.MapPointThumbnail
import com.example.ui.viewmodel.InspectionViewModel
import com.example.util.CsvExportUtils
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: InspectionViewModel,
    onNavigateToNewInspection: () -> Unit,
    onSwitchSpecies: () -> Unit = {}
) {
    val inspections by viewModel.filteredInspections.collectAsStateWithLifecycle()
    val allInspections by viewModel.allInspections.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var inspectionToEdit by remember { mutableStateOf<HorseInspection?>(null) }
    var inspectionToDelete by remember { mutableStateOf<HorseInspection?>(null) }
    var photosForZoom by remember { mutableStateOf<List<String>?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedRaFilter by remember { mutableStateOf<String?>(null) }
    // Multi-Selection state
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showMultiplePdfDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteAllDialog by remember { mutableStateOf(false) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackupToUri(uri) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackupFromUri(uri) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    val displayedInspections = remember(inspections, selectedRaFilter) {
        if (selectedRaFilter == null) inspections
        else inspections.filter { it.administrativeRegion.contains(selectedRaFilter!!, ignoreCase = true) }
    }

    val selectedInspections = remember(displayedInspections, selectedIds) {
        if (selectedIds.isEmpty()) emptyList()
        else displayedInspections.filter { it.id in selectedIds }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4FAF6))
    ) {
        // 1. Barra Unificada de Pesquisa e Botão "Trocar Espécie" (Alinhado com a HomeScreen)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_search_field"),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 13.5.sp,
                        color = Color(0xFF1E293B)
                    ),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Filtrar por RA, levantamento, tutor, notas...",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.searchQuery.value = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpar busca",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

            // Botão Elevado "Trocar Espécie"
            Surface(
                onClick = onSwitchSpecies,
                modifier = Modifier
                    .height(44.dp)
                    .testTag("btn_history_switch_species"),
                shape = CircleShape,
                color = Color(0xFF14532D),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Trocar Espécie",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }

        // 2. Chips de Filtro Rápido por Região Administrativa (RA) com Paleta Verde
        val availableRas = remember(allInspections) {
            val fromData = allInspections.map { it.administrativeRegion }.distinct()
            val defaults = listOf(
                "Ceilândia (RA IX)",
                "Plano Piloto (RA I)",
                "Taguatinga (RA III)",
                "Samambaia (RA XII)",
                "Gama (RA II)",
                "Planaltina (RA VI)",
                "Sobradinho (RA V)"
            )
            (fromData + defaults).distinct().take(12)
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedRaFilter == null,
                    onClick = { selectedRaFilter = null },
                    label = { Text("Todas as RAs", fontSize = 11.sp, fontWeight = if (selectedRaFilter == null) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF14532D),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color(0xFF334155)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedRaFilter == null,
                        borderColor = Color(0xFFCBD5E1),
                        selectedBorderColor = Color(0xFF14532D)
                    )
                )
            }
            items(availableRas) { ra ->
                val isSelected = selectedRaFilter == ra
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedRaFilter = if (isSelected) null else ra
                    },
                    label = { Text(ra, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF14532D),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color(0xFF334155)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color(0xFFCBD5E1),
                        selectedBorderColor = Color(0xFF14532D)
                    )
                )
            }
        }

        // 3. Barra de Seleção Múltipla e Ações em Lote (Card Branco com Borda Suave)
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedIds.isNotEmpty()) {
                            Surface(
                                color = Color(0xFF14532D),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "${selectedIds.size} selecionado(s)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "${displayedInspections.size} registros",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF022C22),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Ações de seleção rápida
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (displayedInspections.isNotEmpty()) {
                            if (selectedIds.size < displayedInspections.size) {
                                TextButton(
                                    onClick = {
                                        selectedIds = displayedInspections.map { it.id }.toSet()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.SelectAll, contentDescription = null, tint = Color(0xFF14532D), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Todos", fontSize = 11.5.sp, color = Color(0xFF14532D), fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                TextButton(
                                    onClick = { selectedIds = emptySet() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Deselect, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Desmarcar", fontSize = 11.5.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botões de Exportação Múltipla (PDF e CSV)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Exportar PDFs Múltiplos
                    Button(
                        onClick = {
                            if (displayedInspections.isEmpty()) {
                                Toast.makeText(context, "Nenhum registro para exportar.", Toast.LENGTH_SHORT).show()
                            } else {
                                showMultiplePdfDialog = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("export_multiple_pdf_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF14532D)
                        )
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (selectedIds.isNotEmpty()) "PDFs (${selectedIds.size})" else "Exportar PDFs",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Exportar CSV
                    OutlinedButton(
                        onClick = {
                            val targets = if (selectedIds.isNotEmpty()) selectedInspections else displayedInspections
                            if (targets.isEmpty()) {
                                Toast.makeText(context, "Nenhum registro para exportar em CSV.", Toast.LENGTH_SHORT).show()
                            } else {
                                val file = CsvExportUtils.exportInspectionsToCsv(context, targets)
                                if (file != null) {
                                    CsvExportUtils.shareCsvFile(context, file)
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF14532D)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("export_csv_button")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF14532D), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedIds.isNotEmpty()) "CSV (${selectedIds.size})" else "Exportar CSV",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14532D)
                        )
                    }

                    // Backup & Restauração Local (Aparelho)
                    OutlinedButton(
                        onClick = { showBackupDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("backup_button")
                    ) {
                        Icon(Icons.Default.SaveAlt, contentDescription = null, tint = Color(0xFF334155), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Backup", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                    }
                }
            }
        }

        // 4. Lista Principal de Registros
        if (displayedInspections.isEmpty()) {
            EmptyState(
                hasAny = allInspections.isNotEmpty(),
                onStartNew = onNavigateToNewInspection
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(displayedInspections, key = { _, item -> item.id }) { index, item ->
                    val isChecked = selectedIds.contains(item.id)
                    HistoryItemCard(
                        inspection = item,
                        index = index,
                        isSelected = isChecked,
                        onToggleSelect = {
                            selectedIds = if (isChecked) {
                                selectedIds - item.id
                            } else {
                                selectedIds + item.id
                            }
                        },
                        onOpenZoom = { photosForZoom = item.allPhotoPaths },
                        onEdit = { inspectionToEdit = item },
                        onExportPdf = { viewModel.exportSinglePdf(item) },
                        onDelete = { inspectionToDelete = item }
                    )
                }
            }
        }
    }

    // Modal de Opções de Exportação Múltipla de Relatórios PDF
    if (showMultiplePdfDialog) {
        val targetList = if (selectedIds.isNotEmpty()) selectedInspections else displayedInspections
        AlertDialog(
            onDismissRequest = { showMultiplePdfDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar Relatórios PDF")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Aviso",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Documento gerado por aplicativo independente para fins de apoio, sem vínculo oficial com o GDF.",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Text(
                        text = "Foram selecionados ${targetList.size} relatório(s) para exportação. Escolha o formato desejado:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Opção 1: Dossiê Consolidado em 1 arquivo PDF único
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMultiplePdfDialog = false
                                viewModel.exportMultipleConsolidatedPdf(targetList)
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Dossiê Consolidado Único",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Reúne todos os ${targetList.size} relatórios em um único documento consolidado com capa e sumário.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Opção 2: Múltiplos Arquivos PDFs Separados
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMultiplePdfDialog = false
                                viewModel.exportMultipleIndividualPdfs(targetList)
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Arquivos Individuais em Lote",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Gera ${targetList.size} arquivos PDF individuais e compartilha todos juntos.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMultiplePdfDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Nuvem: Fazer Backup e Recuperar Registros
    // Modal de Cópia de Segurança Local (Exportar / Importar Backup no Celular)
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SaveAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cópia de Segurança Local")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Gerencie o backup das suas fiscalizações diretamente no aparelho:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Opção 1: Salvar Cópia no Celular (Escolher Pasta)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBackupDialog = false
                                createBackupLauncher.launch(DatabaseBackupManager.generateBackupFilename())
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Salvar Cópia Completa no Celular (.ZIP com Fotos)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Exporta um pacote .zip contendo todas as vistorias e as fotografias originais com marca d'água para recuperação segura.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Opção 2: Restaurar Backup do Celular (Escolher Arquivo)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBackupDialog = false
                                restoreBackupLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream", "application/json", "*/*"))
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Restaurar Backup do Celular (.ZIP ou .JSON)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Selecione o arquivo de backup (.zip com fotos ou .json legado) para restaurar vistorias e fotos.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Opção 3: Deletar Todos os Registros e Arquivos Locais (Zerar Banco Room)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBackupDialog = false
                                showConfirmDeleteAllDialog = true
                            }
                            .testTag("button_open_delete_all_room"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Deletar Arquivos & Zerar Banco (Room)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Exclui permanentemente todas as vistorias do banco Room e todas as fotos salvas localmente no aparelho.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Segurança & Privacidade: Todo o armazenamento é 100% local no próprio aparelho (Room SQLite), funcionando completamente offline.",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Modal de Confirmação para Deletar Tudo (Room e Fotos)
    if (showConfirmDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteAllDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Zerar Banco Room e Deletar Arquivos?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Atenção: Esta ação irá excluir definitivamente todas as fiscalizações do banco de dados local (Room) e todos os arquivos de fotos armazenados no dispositivo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Recomendamos exportar uma cópia de segurança antes de prosseguir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteAllDialog = false
                        viewModel.deleteAllLocalDataAndRoom { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("button_confirm_delete_all_room")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deletar Tudo Permanentemente")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDeleteAllDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Edição
    if (inspectionToEdit != null) {
        EditInspectionDialog(
            inspection = inspectionToEdit!!,
            onDismiss = { inspectionToEdit = null },
            onSave = { notes, ra, quadra, conjunto, numero, horseDescription, horseCount, mistreatedHorseCount, mistreatmentIndicators, adequateIndicators, requiresSeagriApprehension, seagriNotes, requiresPmdfSupport, pmdfNotes, requiresOtherAgencySupport, otherAgencyName, otherAgencyNotes, riskLevel, safetyRiskAssessment, tutorName, tutorCpf, additionalTutors, auditorName, ppbeaAnswersData, updatedPhotos, indivEvaluationsData, newSampledCount ->
                viewModel.updateInspection(
                    original = inspectionToEdit!!,
                    newNotes = notes,
                    newRa = ra,
                    newQuadra = quadra,
                    newConjunto = conjunto,
                    newNumero = numero,
                    newHorseDescription = horseDescription,
                    newHorseCount = horseCount,
                    newSampledAnimalCount = newSampledCount,
                    newMistreatedHorseCount = mistreatedHorseCount,
                    newMistreatmentIndicators = mistreatmentIndicators,
                    newAdequateIndicators = adequateIndicators,
                    newRequiresSeagriApprehension = requiresSeagriApprehension,
                    newSeagriNotes = seagriNotes,
                    newRequiresPmdfSupport = requiresPmdfSupport,
                    newPmdfNotes = pmdfNotes,
                    newRequiresOtherAgencySupport = requiresOtherAgencySupport,
                    newOtherAgencyName = otherAgencyName,
                    newOtherAgencyNotes = otherAgencyNotes,
                    newRiskLevel = riskLevel,
                    newSafetyRiskAssessment = safetyRiskAssessment,
                    newTutorName = tutorName,
                    newTutorCpf = tutorCpf,
                    newAdditionalTutors = additionalTutors,
                    newAuditorName = auditorName,
                    newPpbeaAnswersData = ppbeaAnswersData,
                    newIndividualEvaluationsData = indivEvaluationsData,
                    newPhotoPaths = updatedPhotos,
                    onSuccess = {
                        inspectionToEdit = null
                    }
                )
            }
        )
    }

    // Confirmação de Exclusão
    if (inspectionToDelete != null) {
        AlertDialog(
            onDismissRequest = { inspectionToDelete = null },
            title = { Text("Excluir Registro?") },
            text = {
                Text("Deseja realmente remover o registro ${inspectionToDelete?.protocolNumber}? As fotos anexadas também serão removidas.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val item = inspectionToDelete!!
                        viewModel.deleteInspection(item)
                        selectedIds = selectedIds - item.id
                        inspectionToDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Zoom de Fotos
    if (photosForZoom != null) {
        PhotoZoomDialog(
            photoPaths = photosForZoom!!,
            onDismiss = { photosForZoom = null }
        )
    }
}

/**
 * Card individual do histórico organizado com os parâmetros:
 * 1. Número de protocolo e data
 * 2. RA (Região Administrativa) & Endereço Residencial
 * 3. Thumbnail do ponto no Mapa e Foto
 * 4. Quantos cavalos monitorados e quantos com sinais de maus tratos
 * 5. Providências Operacionais (Apoio da SEAGRI e PMDF)
 */
@Composable
fun HistoryItemCard(
    inspection: HorseInspection,
    index: Int = 0,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onOpenZoom: () -> Unit,
    onEdit: () -> Unit,
    onExportPdf: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${inspection.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF14532D) else Color(0xFFE2E8F0)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {

            // 1. HEADER: Checkbox + NÚMERO DE PROTOCOLO + DATA
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF14532D)
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = inspection.protocolNumber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF022C22),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFD1FAE5),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${inspection.speciesEmoji} ${inspection.speciesDisplay}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14532D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (inspection.auditorName.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "👤 ${inspection.auditorName}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = inspection.formattedCaptureDate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. PARÂMETRO: REGIÃO ADMINISTRATIVA (RA) & ENDEREÇO RESIDENCIAL
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Linha 1: Região Administrativa (RA)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RA: ${inspection.administrativeRegion}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Linha 2: Endereço Residencial
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Endereço: ${inspection.residentialAddress}",
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. FOTO DA OCORRÊNCIA + THUMBNAIL DO PONTO NO MAPA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Coluna Esquerda: Fotografia da Ocorrência
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(95.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .clickable(onClick = onOpenZoom)
                ) {
                    val photoFile = File(inspection.photoPath)
                    if (photoFile.exists()) {
                        AsyncImage(
                            model = photoFile,
                            contentDescription = "Foto da fiscalização",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Overlay de Zoom
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Ampliar",
                                fontSize = 9.5.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Contador de fotos
                    if (inspection.allPhotoPaths.size > 1) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(topStart = 8.dp),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Text(
                                text = "📸 ${inspection.allPhotoPaths.size} fotos",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Coluna Direita: THUMBNAIL DO PONTO NO MAPA
                MapPointThumbnail(
                    latitude = inspection.latitude,
                    longitude = inspection.longitude,
                    administrativeRegion = inspection.administrativeRegion,
                    protocolNumber = inspection.protocolNumber,
                    modifier = Modifier
                        .weight(1f)
                        .height(95.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CORPO DO CARD COM OS DEMAIS PARÂMETROS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 4. QUANTOS ANIMAIS MONITORADOS E QUANTOS COM SINAIS DE MAUS TRATOS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total monitorados
                    Surface(
                        color = Color(0xFFFDE68A).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            val monitoredTitle = if (inspection.isDog) "Cães Monitorados" else "Animais Monitorados"
                            Text(
                                text = monitoredTitle,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF451A03)
                            )
                            val animalUnit = if (inspection.isDog) {
                                if (inspection.horseCount == 1) "cão" else "cães"
                            } else {
                                if (inspection.horseCount == 1) "animal" else "animais"
                            }
                            Text(
                                text = "${inspection.speciesEmoji} ${inspection.horseCount} $animalUnit",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF451A03)
                            )
                        }
                    }

                    // Com sinais de maus tratos
                    val hasMistreatment = inspection.mistreatedHorseCount > 0
                    Surface(
                        color = if (hasMistreatment) Color(0xFFFECDD3).copy(alpha = 0.85f) else Color(0xFFD1FAE5).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Sinais de Maus-Tratos",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasMistreatment) Color(0xFF881337) else Color(0xFF065F46)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (hasMistreatment) {
                                    "⚠️ ${inspection.mistreatedHorseCount} com sinais"
                                } else {
                                    "✓ Nenhum sinal"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (hasMistreatment) Color(0xFF881337) else Color(0xFF065F46)
                            )
                        }
                    }
                }

                // PARÂMETRO: SE O APOIO DA SEAGRI E PM FOI MARCADO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Apoio SEAGRI
                    val seagriMarked = inspection.requiresSeagriApprehension
                    Surface(
                        color = if (seagriMarked) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                        border = if (seagriMarked) BorderStroke(1.dp, Color(0xFFF59E0B)) else null,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text(
                                text = if (seagriMarked) "🚨 SEAGRI: MARCADO" else "⚪ SEAGRI: Não marcado",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (seagriMarked) Color(0xFF92400E) else Color(0xFF64748B)
                            )
                            if (seagriMarked) {
                                Text(
                                    text = if (inspection.seagriNotes.isNotBlank()) inspection.seagriNotes else "Apreensão requisitada",
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF78350F),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Apoio PMDF
                    val pmdfMarked = inspection.requiresPmdfSupport
                    Surface(
                        color = if (pmdfMarked) Color(0xFFDBEAFE) else Color(0xFFF1F5F9),
                        border = if (pmdfMarked) BorderStroke(1.dp, Color(0xFF3B82F6)) else null,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text(
                                text = if (pmdfMarked) "👮 PMDF: MARCADO" else "⚪ PMDF: Não marcado",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pmdfMarked) Color(0xFF1E40AF) else Color(0xFF64748B)
                            )
                            if (pmdfMarked) {
                                Text(
                                    text = if (inspection.pmdfNotes.isNotBlank()) inspection.pmdfNotes else "Apoio policial requisitado",
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF1E3A8A),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Apoio de Outro Órgão / Providências de Apreensão
                val otherAgencyMarked = inspection.requiresOtherAgencySupport
                if (otherAgencyMarked) {
                    Surface(
                        color = Color(0xFFF3E8FF),
                        border = BorderStroke(1.dp, Color(0xFFA855F7)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text(
                                text = "🏛️ OUTRO ÓRGÃO: ${inspection.otherAgencyName.ifBlank { "REQUISITADO" }}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B21A8)
                            )
                            if (inspection.otherAgencyNotes.isNotBlank()) {
                                Text(
                                    text = inspection.otherAgencyNotes,
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF581C87),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                // Tutores e CPFs (se houver)
                if (inspection.allTutors.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tutores / Resp.: ${inspection.tutorsSummary}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF334155),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                // Descrição física do animal
                if (inspection.horseDescription.isNotBlank()) {
                    Text(
                        text = "Descrição: ${inspection.horseDescription}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        maxLines = 2
                    )
                }

                // Ações do Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF14532D)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF14532D), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", fontSize = 11.5.sp, color = Color(0xFF14532D), fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        onClick = onExportPdf,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("history_export_pdf_${inspection.id}"),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF14532D)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontSize = 11.5.sp, color = Color(0xFF14532D), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoZoomDialog(
    photoPaths: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    var currentIndex by remember {
        mutableIntStateOf(initialIndex.coerceIn(0, kotlin.math.max(0, photoPaths.lastIndex)))
    }
    val currentPath = photoPaths.getOrNull(currentIndex) ?: ""

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val file = File(currentPath)
            if (file.exists()) {
                AsyncImage(
                    model = file,
                    contentDescription = "Foto ampliada com marca d'água da fiscalização",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Top Bar with Counter and Close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (photoPaths.size > 1) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "📸 Foto ${currentIndex + 1} de ${photoPaths.size}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Fechar visualização",
                            tint = Color.White
                        )
                    }
                }
            }

            // Navigation Arrows (Previous / Next)
            if (photoPaths.size > 1) {
                if (currentIndex > 0) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                            .size(44.dp)
                    ) {
                        IconButton(onClick = { currentIndex-- }) {
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
                            .padding(end = 16.dp)
                            .size(44.dp)
                    ) {
                        IconButton(onClick = { currentIndex++ }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Próxima foto",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoZoomDialog(
    photoPath: String,
    onDismiss: () -> Unit
) {
    PhotoZoomDialog(
        photoPaths = listOf(photoPath),
        initialIndex = 0,
        onDismiss = onDismiss
    )
}
