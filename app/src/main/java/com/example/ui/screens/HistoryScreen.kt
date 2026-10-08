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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Home
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
import com.example.util.FirestoreSyncManager
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: InspectionViewModel,
    onNavigateToNewInspection: () -> Unit
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
    val isSyncing by viewModel.isSyncingCloud.collectAsStateWithLifecycle()

    // Multi-Selection state
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showMultiplePdfDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }

    var showOfflineBackupDialog by remember { mutableStateOf(false) }
    var cloudSyncFailReason by remember { mutableStateOf("") }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
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

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Barra de Pesquisa Regional / Geral
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("history_search_field"),
            placeholder = { Text("Filtrar por RA, protocolo, tutor, notas...") },
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

        // 2. Chips de Filtro Rápido por Região Administrativa (RA)
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
                    label = { Text("Todas as RAs", fontSize = 11.5.sp) }
                )
            }
            items(availableRas) { ra ->
                val isSelected = selectedRaFilter == ra
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedRaFilter = if (isSelected) null else ra
                    },
                    label = { Text(ra, fontSize = 11.5.sp) }
                )
            }
        }

        // 3. Barra de Seleção Múltipla e Ações em Lote (PDFs e CSV)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedIds.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${selectedIds.size} selecionado(s)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Registros (${displayedInspections.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
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
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.SelectAll, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Selecionar Todos", fontSize = 11.sp)
                                }
                            } else {
                                TextButton(
                                    onClick = { selectedIds = emptySet() },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Deselect, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Desmarcar", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

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
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("export_multiple_pdf_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedIds.isNotEmpty()) "PDFs (${selectedIds.size})" else "Exportar PDFs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
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
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("export_csv_button")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedIds.isNotEmpty()) "CSV (${selectedIds.size})" else "Exportar CSV",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Sincronizar Nuvem (Firestore)
                    OutlinedButton(
                        onClick = { showCloudSyncDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("cloud_sync_button"),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isSyncing) "Sincronizando..." else "Nuvem", fontSize = 11.sp)
                    }
                }
            }
        }

        // Indicador em tempo real de sincronização em segundo plano
        if (isSyncing) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Conectando e sincronizando com o Firebase... Aguarde.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            modifier = Modifier.padding(12.dp),
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
                                    text = "Reúne todos os ${targetList.size} relatórios em um único documento oficial com capa e sumário.",
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
                            modifier = Modifier.padding(12.dp),
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
    if (showCloudSyncDialog) {
        AlertDialog(
            onDismissRequest = { showCloudSyncDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nuvem & Sincronização")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Gerencie o envio (backup) ou a recuperação (download) das suas fiscalizações com a nuvem do Firestore:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Opção 1: Fazer Backup na Nuvem (Enviar)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showCloudSyncDialog = false
                                viewModel.syncAllToCloud { success, message ->
                                    if (success) {
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    } else {
                                        cloudSyncFailReason = message
                                        showOfflineBackupDialog = true
                                    }
                                }
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
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Fazer Backup na Nuvem (Enviar)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Envia todos os ${allInspections.size} registros locais para o banco de dados remoto.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Opção 2: Recuperar / Baixar Registros da Nuvem
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showCloudSyncDialog = false
                                viewModel.restoreAllFromCloud { _, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Recuperar / Baixar da Nuvem",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Baixa e restaura todas as fiscalizações salvas na nuvem para este aparelho.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Opção 3: Salvar Cópia no Celular (Escolher Pasta)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showCloudSyncDialog = false
                                createBackupLauncher.launch(DatabaseBackupManager.generateBackupFilename())
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Salvar Cópia no Celular (Escolher Pasta)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = "Escolha onde salvar o arquivo de backup no seu celular (Downloads, Documentos, Pen Drive, etc.).",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Opção 4: Restaurar Backup do Celular (Escolher Arquivo)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showCloudSyncDialog = false
                                restoreBackupLauncher.launch(arrayOf("application/json", "*/*"))
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
                                    text = "Restaurar Backup do Celular (Escolher Arquivo)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Selecione um arquivo de backup (.json) salvo no aparelho para restaurar os dados.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        text = "Nota: Se estiver sem internet ou sem arquivo de credencial configurado, o app funciona 100% offline preservando tudo localmente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.5.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCloudSyncDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Modal emitido quando o envio para a nuvem não funcionar para o fiscal escolher o local no celular
    if (showOfflineBackupDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineBackupDialog = false },
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
                    text = "Envio para Nuvem Indisponível",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Não foi possível enviar os registros para a nuvem no momento (${cloudSyncFailReason.ifBlank { "Sem conexão ou serviço indisponível" }}).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "Seus dados estão 100% seguros e preservados neste aparelho.",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Deseja escolher agora a pasta no seu celular (Downloads, Documentos, Pen Drive ou Cartão SD) onde salvar uma cópia de segurança do banco de dados?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOfflineBackupDialog = false
                        createBackupLauncher.launch(DatabaseBackupManager.generateBackupFilename())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Escolher Local & Salvar no Celular")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfflineBackupDialog = false }) {
                    Text("Manter apenas no App")
                }
            }
        )
    }

    // Modal de Edição
    if (inspectionToEdit != null) {
        EditInspectionDialog(
            inspection = inspectionToEdit!!,
            onDismiss = { inspectionToEdit = null },
            onSave = { notes, ra, quadra, conjunto, numero, horseDescription, horseCount, mistreatedHorseCount, mistreatmentIndicators, adequateIndicators, requiresSeagriApprehension, seagriNotes, requiresPmdfSupport, pmdfNotes, riskLevel, safetyRiskAssessment, tutorName, tutorCpf, additionalTutors ->
                viewModel.updateInspection(
                    original = inspectionToEdit!!,
                    newNotes = notes,
                    newRa = ra,
                    newQuadra = quadra,
                    newConjunto = conjunto,
                    newNumero = numero,
                    newHorseDescription = horseDescription,
                    newHorseCount = horseCount,
                    newMistreatedHorseCount = mistreatedHorseCount,
                    newMistreatmentIndicators = mistreatmentIndicators,
                    newAdequateIndicators = adequateIndicators,
                    newRequiresSeagriApprehension = requiresSeagriApprehension,
                    newSeagriNotes = seagriNotes,
                    newRequiresPmdfSupport = requiresPmdfSupport,
                    newPmdfNotes = pmdfNotes,
                    newRiskLevel = riskLevel,
                    newSafetyRiskAssessment = safetyRiskAssessment,
                    newTutorName = tutorName,
                    newTutorCpf = tutorCpf,
                    newAdditionalTutors = additionalTutors,
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
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("history_item_${inspection.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardBgColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 3.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // 1. HEADER: Checkbox + NÚMERO DE PROTOCOLO + DATA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
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
                            checkedColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = inspection.protocolNumber,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = inspection.formattedCaptureDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. PARÂMETRO: REGIÃO ADMINISTRATIVA (RA) & ENDEREÇO RESIDENCIAL
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Linha 1: Região Administrativa (RA)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Região Administrativa (RA): ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = inspection.administrativeRegion,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    // Linha 2: Endereço Residencial logo abaixo do campo RA
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Endereço Residencial: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = inspection.residentialAddress,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 3. PARÂMETRO: FOTO DA OCORRÊNCIA + THUMBNAIL DO PONTO NO MAPA (Lado a Lado)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Coluna Esquerda: Fotografia da Ocorrência
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(135.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
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
                                text = "Ampliar Foto",
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
                        .height(135.dp)
                )
            }

            // CORPO DO CARD COM OS DEMAIS PARÂMETROS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // 4. PARÂMETRO: QUANTOS CAVALOS MONITORADOS E QUANTOS COM SINAIS DE MAUS TRATOS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total monitorados
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Cavalos Monitorados",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🐴 ${inspection.horseCount} animal(is)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Com sinais de maus tratos
                    val hasMistreatment = inspection.mistreatedHorseCount > 0
                    Surface(
                        color = if (hasMistreatment) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                        border = BorderStroke(1.dp, if (hasMistreatment) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Sinais de Maus-Tratos",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasMistreatment) Color(0xFFC62828) else Color(0xFF2E7D32)
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
                                color = if (hasMistreatment) Color(0xFFC62828) else Color(0xFF2E7D32)
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
                        color = if (seagriMarked) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = if (seagriMarked) BorderStroke(1.dp, Color(0xFFFFB74D)) else null,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text(
                                text = if (seagriMarked) "🚨 SEAGRI: MARCADO" else "⚪ SEAGRI: Não marcado",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (seagriMarked) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (seagriMarked) {
                                Text(
                                    text = if (inspection.seagriNotes.isNotBlank()) inspection.seagriNotes else "Apreensão requisitada",
                                    fontSize = 9.5.sp,
                                    color = Color(0xFFBF360C),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Apoio PMDF
                    val pmdfMarked = inspection.requiresPmdfSupport
                    Surface(
                        color = if (pmdfMarked) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = if (pmdfMarked) BorderStroke(1.dp, Color(0xFF90CAF9)) else null,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text(
                                text = if (pmdfMarked) "👮 PMDF: MARCADO" else "⚪ PMDF: Não marcado",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pmdfMarked) Color(0xFF1565C0) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (pmdfMarked) {
                                Text(
                                    text = if (inspection.pmdfNotes.isNotBlank()) inspection.pmdfNotes else "Apoio policial requisitado",
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF0D47A1),
                                    maxLines = 1
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tutores / Resp.: ${inspection.tutorsSummary}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Descrição física do cavalo
                if (inspection.horseDescription.isNotBlank()) {
                    Text(
                        text = "Descrição: ${inspection.horseDescription}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                // Ações do Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
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
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", fontSize = 11.5.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary)
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
