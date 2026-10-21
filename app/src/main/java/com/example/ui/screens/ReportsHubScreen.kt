package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
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
    viewModel: InspectionViewModel,
    onSwitchSpecies: () -> Unit = {}
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
            .background(Color(0xFFF4FAF6))
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("screen_reports_hub"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // 1. Header Card - Relatório de Apoio (Card Branco Refinado com Estilo Institucional)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
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
                    AsyncImage(
                        model = R.drawable.logo_meio_ambiente,
                        contentDescription = "Logo Meio Ambiente",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RELATÓRIO DE APOIO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF14532D),
                            letterSpacing = 0.5.sp
                        )
                        val activeAuditor = viewModel.auditorIdentification.ifBlank { "Equipe Operacional" }
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                        ) {
                            Text(
                                text = "Auditor Fiscal: $activeAuditor",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF14532D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "Levantamento Operacional com Registro Georreferenciado",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        onClick = onSwitchSpecies,
                        shape = CircleShape,
                        color = Color(0xFF14532D),
                        shadowElevation = 2.dp,
                        modifier = Modifier.testTag("btn_reports_switch_species")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Trocar Espécie",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Trocar Espécie",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Filtro de Intervalo de Datas
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF14532D),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1. Intervalo de Datas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        if (startDate != null || endDate != null) {
                            TextButton(
                                onClick = {
                                    viewModel.reportStartDate.value = null
                                    viewModel.reportEndDate.value = null
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Limpar Filtro",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14532D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset chips com paleta verde
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
                                label = { Text("Todo o Período", fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                                    selectedLabelColor = Color(0xFF022C22),
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF475569)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (startDate == null && endDate == null) Color(0xFF14532D).copy(alpha = 0.3f) else Color(0xFFE2E8F0),
                                    enabled = true,
                                    selected = startDate == null && endDate == null
                                )
                            )
                        }
                        item {
                            val isToday = isTodaySelected(startDate, endDate)
                            FilterChip(
                                selected = isToday,
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
                                label = { Text("Hoje", fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                                    selectedLabelColor = Color(0xFF022C22),
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF475569)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isToday) Color(0xFF14532D).copy(alpha = 0.3f) else Color(0xFFE2E8F0),
                                    enabled = true,
                                    selected = isToday
                                )
                            )
                        }
                        item {
                            val is7Days = isDaysAgoSelected(startDate, 7)
                            FilterChip(
                                selected = is7Days,
                                onClick = {
                                    val now = System.currentTimeMillis()
                                    viewModel.reportStartDate.value = now - (7L * 24 * 60 * 60 * 1000)
                                    viewModel.reportEndDate.value = now
                                },
                                label = { Text("Últimos 7 dias", fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                                    selectedLabelColor = Color(0xFF022C22),
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF475569)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (is7Days) Color(0xFF14532D).copy(alpha = 0.3f) else Color(0xFFE2E8F0),
                                    enabled = true,
                                    selected = is7Days
                                )
                            )
                        }
                        item {
                            val is30Days = isDaysAgoSelected(startDate, 30)
                            FilterChip(
                                selected = is30Days,
                                onClick = {
                                    val now = System.currentTimeMillis()
                                    viewModel.reportStartDate.value = now - (30L * 24 * 60 * 60 * 1000)
                                    viewModel.reportEndDate.value = now
                                },
                                label = { Text("Últimos 30 dias", fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                                    selectedLabelColor = Color(0xFF022C22),
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF475569)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (is30Days) Color(0xFF14532D).copy(alpha = 0.3f) else Color(0xFFE2E8F0),
                                    enabled = true,
                                    selected = is30Days
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Exibição do período ativo em pílula
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val dateLabel = if (startDate != null && endDate != null) {
                            "Período ativo: ${sdf.format(Date(startDate!!))} até ${sdf.format(Date(endDate!!))}"
                        } else {
                            "Período ativo: Todos os registros arquivados"
                        }
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF14532D),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = dateLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }

        // 3. Filtro por Região Administrativa (RA)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
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
                                tint = Color(0xFF14532D),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "2. Região Administrativa (RA)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        if (selectedRa != null && selectedRa != "Todas as RAs") {
                            TextButton(
                                onClick = { viewModel.reportSelectedRa.value = null },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Limpar RA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14532D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRaPicker = true }
                            .testTag("report_ra_filter"),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (selectedRa != null) Color(0xFF14532D) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedRa ?: "Todas as Regiões Administrativas (DF)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Surface(
                                color = Color(0xFFD1FAE5),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "Alterar",
                                    color = Color(0xFF14532D),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Resumo & Ação de Exportação Consolidada
        item {
            val totalAnimals = reportInspections.sumOf { it.horseCount }
            val mistreatedCount = reportInspections.sumOf { it.mistreatedHorseCount }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumo dos Registros Filtrados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color(0xFF14532D)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${reportInspections.size}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Ocorrências",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalAnimals",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Animais",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$mistreatedCount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = if (mistreatedCount > 0) Color(0xFFDC2626) else Color(0xFF14532D)
                            )
                            Text(
                                text = "Maus-tratos",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Aviso legal e institucional em card suave
                    Surface(
                        onClick = { viewModel.openPdfDisclaimerDialog() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7).copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Aviso",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Aviso de Apoio Fiscal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Documento gerado por aplicativo independente para fins de apoio, sem vínculo oficial com o GDF.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF78350F),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.exportConsolidatedPdf() },
                        enabled = reportInspections.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_consolidated_pdf_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF14532D),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE2E8F0),
                            disabledContentColor = Color(0xFF94A3B8)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gerar Dossiê PDF Consolidado",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (reportInspections.isEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Nenhum registro encontrado para os filtros selecionados.",
                            color = Color(0xFFDC2626),
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // 5. Previews dos registros incluídos
        if (reportInspections.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Registros Incluídos no Dossiê (${reportInspections.size}):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF14532D)
                    )
                }
            }

            items(reportInspections, key = { it.id }) { item ->
                ReportPreviewItem(
                    inspection = item,
                    onExportIndividual = { viewModel.exportSinglePdf(item) }
                )
            }
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
fun ReportPreviewItem(
    inspection: HorseInspection,
    onExportIndividual: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("report_item_${inspection.id}"),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                val file = File(inspection.photoPath)
                if (file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = inspection.speciesEmoji,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = inspection.protocolNumber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF022C22),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = inspection.formattedAddress,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                val animalUnit = if (inspection.isDog) "Cães" else "Animais"
                Text(
                    text = "Data: ${inspection.formattedShortDate} • $animalUnit: ${inspection.horseCount}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                if (inspection.auditorName.isNotBlank() || inspection.tutorName.isNotBlank()) {
                    val info = buildString {
                        if (inspection.auditorName.isNotBlank()) append("Auditor: ${inspection.auditorName}")
                        if (inspection.tutorName.isNotBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append("Tutor: ${inspection.tutorName}")
                        }
                    }
                    Text(
                        text = info,
                        fontSize = 10.sp,
                        color = Color(0xFF14532D),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                onClick = onExportIndividual,
                shape = CircleShape,
                color = Color(0xFFD1FAE5),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Exportar Individual",
                        tint = Color(0xFF14532D),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
