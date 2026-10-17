package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.HorseInspection
import com.example.ui.viewmodel.InspectionViewModel
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    viewModel: InspectionViewModel,
    onInspectionClick: (HorseInspection) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allInspections by viewModel.inspections.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todas") }
    var showBackupDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("Todas", "Cavalo", "Cachorro", "Gato", "Galináceos", "Outros")

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val count = viewModel.importBackup(context, uri)
                    Toast.makeText(context, "$count vistorias e fotos restauradas!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao restaurar backup: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val filteredList = allInspections.filter { insp ->
        val matchesSearch = searchQuery.isBlank() ||
                insp.protocolNumber.contains(searchQuery, ignoreCase = true) ||
                insp.administrativeRegion.contains(searchQuery, ignoreCase = true) ||
                insp.speciesDisplay.contains(searchQuery, ignoreCase = true) ||
                insp.auditorName.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Todas" -> true
            "Cavalo" -> insp.isHorse
            "Cachorro" -> insp.isDog
            "Gato" -> insp.isCat
            "Galináceos" -> insp.isPoultry
            "Outros" -> !insp.isHorse && !insp.isDog && !insp.isCat && !insp.isPoultry
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // Search & Backup Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar protocolo, espécie, RA...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("history_search_input")
            )

            IconButton(
                onClick = { showBackupDialog = true },
                modifier = Modifier
                    .background(Color(0xFF00684A), RoundedCornerShape(10.dp))
                    .size(48.dp)
                    .testTag("backup_button")
            ) {
                Icon(Icons.Default.Backup, contentDescription = "Backup", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Species Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { opt ->
                FilterChip(
                    selected = selectedFilter == opt,
                    onClick = { selectedFilter = opt },
                    label = { Text(opt, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Total de Registros: ${filteredList.size}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (allInspections.isEmpty()) "Nenhuma vistoria realizada ainda." else "Nenhum resultado para os filtros.",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { insp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInspectionClick(insp) }
                            .testTag("history_item_${insp.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(insp.speciesEmoji, fontSize = 24.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = insp.protocolNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${insp.speciesDisplay} • ${insp.administrativeRegion}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF00684A),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Data: ${insp.formattedCaptureDate} • ${insp.horseCount} animal(is)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val pdf = viewModel.generatePdf(context, insp)
                                    if (pdf != null) {
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdf)
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório"))
                                    }
                                }
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Color(0xFF00684A))
                            }

                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Backup & Restore Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Text("Backup e Restauração", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "O backup completo exporta todas as vistorias cadastradas e seus laudos fotográficos em um arquivo .ZIP seguro.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val zip = viewModel.exportBackup(context)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zip)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/zip"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, "Backup Fiscalização DF")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Exportar Backup"))
                                    showBackupDialog = false
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erro ao gerar backup: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00684A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar Cópia Completa (.ZIP)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch("application/zip")
                            showBackupDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restaurar Arquivo de Backup")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}
