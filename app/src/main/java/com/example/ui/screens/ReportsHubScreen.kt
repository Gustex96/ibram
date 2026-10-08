package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.HorseInspection
import com.example.ui.viewmodel.InspectionViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReportsHubScreen(
    viewModel: InspectionViewModel
) {
    val reportInspections by viewModel.reportPreviewInspections.collectAsStateWithLifecycle()
    val allInspections by viewModel.allInspections.collectAsStateWithLifecycle()
    val startDate by viewModel.reportStartDate.collectAsStateWithLifecycle()
    val endDate by viewModel.reportEndDate.collectAsStateWithLifecycle()
    val selectedRa by viewModel.reportSelectedRa.collectAsStateWithLifecycle()

    var showRaPicker by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Brasília Ambiental Header Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = R.drawable.logo_brasilia_ambiental,
                        contentDescription = "Logo Brasília Ambiental",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Emissão de Relatórios Oficiais",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Brasília Ambiental • Dossiê Fiscalizatório com Marca D'Água Georreferenciada",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Filter by Date Range
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "1. Intervalo de Datas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = startDate == null && endDate == null,
                                onClick = {
                                    viewModel.reportStartDate.value = null
                                    viewModel.reportEndDate.value = null
                                },
                                label = { Text("Todo o Período") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = isTodaySelected(startDate, endDate),
                                onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    viewModel.reportStartDate.value = cal.timeInMillis
                                    viewModel.reportEndDate.value = System.currentTimeMillis()
                                },
                                label = { Text("Hoje") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = isDaysAgoSelected(startDate, 7),
                                onClick = {
                                    val now = System.currentTimeMillis()
                                    viewModel.reportStartDate.value = now - (7L * 24 * 60 * 60 * 1000)
                                    viewModel.reportEndDate.value = now
                                },
                                label = { Text("Últimos 7 dias") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = isDaysAgoSelected(startDate, 30),
                                onClick = {
                                    val now = System.currentTimeMillis()
                                    viewModel.reportStartDate.value = now - (30L * 24 * 60 * 60 * 1000)
                                    viewModel.reportEndDate.value = now
                                },
                                label = { Text("Últimos 30 dias") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Display active period
                    val dateLabel = if (startDate != null && endDate != null) {
                        "Período selecionado: ${sdf.format(Date(startDate!!))} até ${sdf.format(Date(endDate!!))}"
                    } else {
                        "Período selecionado: Todos os registros arquivados"
                    }
                    Text(
                        text = dateLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 3. Filter by Região Administrativa
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "2. Região Administrativa (RA)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (selectedRa != null && selectedRa != "Todas as RAs") {
                            TextButton(onClick = { viewModel.reportSelectedRa.value = null }) {
                                Text("Limpar RA", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRaPicker = true }
                            .testTag("report_ra_filter"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedRa ?: "Todas as Regiões Administrativas (DF)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Selecionar",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Summary & Action Card
        item {
            val totalHorses = reportInspections.sumOf { it.horseCount }
            val mistreatedCount = reportInspections.sumOf { it.mistreatedHorseCount }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumo dos Registros Selecionados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${reportInspections.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(text = "Ocorrências", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalHorses", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(text = "Cavalos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$mistreatedCount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = if (mistreatedCount > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(text = "Maus-tratos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.exportConsolidatedPdf() },
                        enabled = reportInspections.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("export_consolidated_pdf_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gerar & Exportar Relatório em PDF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    if (reportInspections.isEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Nenhum registro encontrado para os filtros selecionados.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // 5. Previews of included inspections
        if (reportInspections.isNotEmpty()) {
            item {
                Text(
                    text = "Registros Incluídos no Relatório (${reportInspections.size}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(reportInspections, key = { it.id }) { item ->
                ReportPreviewItem(inspection = item, onExportIndividual = { viewModel.exportSinglePdf(item) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (showRaPicker) {
        RaSelectionDialog(
            currentRa = selectedRa ?: "Todas as RAs",
            onSelectRa = {
                viewModel.reportSelectedRa.value = it
                showRaPicker = false
            },
            onDismiss = { showRaPicker = false }
        )
    }
}

private fun isTodaySelected(start: Long?, end: Long?): Boolean {
    if (start == null || end == null) return false
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return start >= cal.timeInMillis
}

private fun isDaysAgoSelected(start: Long?, days: Int): Boolean {
    if (start == null) return false
    val expected = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000)
    return Math.abs(start - expected) < 300000L
}

@Composable
fun ReportPreviewItem(inspection: HorseInspection, onExportIndividual: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black)
            ) {
                val file = File(inspection.photoPath)
                if (file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${inspection.protocolNumber} • ${inspection.formattedAddress}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "Data: ${inspection.formattedShortDate} | Cavalos: ${inspection.horseCount}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (inspection.tutorName.isNotBlank()) {
                    Text(
                        text = "Tutor: ${inspection.tutorName}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onExportIndividual) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "Exportar Individual",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
