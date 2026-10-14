package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.HorseInspection
import com.example.ui.components.AnimalSpecies
import com.example.ui.components.SpeciesSelectorTabs
import com.example.ui.viewmodel.InspectionViewModel
import com.example.util.CsvExportUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Aba dedicada a Estatísticas de Relatórios e Levantamentos Operacionais.
 * Apresenta métricas de auditoria de bem-estar animal, requisições de apoio aos órgãos públicos (SEAGRI, PMDF),
 * distribuição por Região Administrativa (RA do DF) e indicadores de maus-tratos.
 */
@Composable
fun StatisticsScreen(
    viewModel: InspectionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allInspections by viewModel.allInspections.collectAsStateWithLifecycle()
    val species = viewModel.selectedSpecies

    // Filtro de Período (0: Todo Histórico, 1: Últimos 7 dias, 2: Últimos 30 dias, 3: Este Mês)
    var selectedPeriodFilter by remember { mutableIntStateOf(0) }

    val filteredInspections = remember(allInspections, species, selectedPeriodFilter) {
        val speciesFiltered = when (species) {
            AnimalSpecies.HORSE -> allInspections.filter { !it.isDog }
            AnimalSpecies.DOG -> allInspections.filter { it.isDog }
        }

        val now = System.currentTimeMillis()
        when (selectedPeriodFilter) {
            1 -> {
                // Últimos 7 dias
                val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)
                speciesFiltered.filter { it.captureTimestamp >= sevenDaysAgo }
            }
            2 -> {
                // Últimos 30 dias
                val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
                speciesFiltered.filter { it.captureTimestamp >= thirtyDaysAgo }
            }
            3 -> {
                // Este mês
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                speciesFiltered.filter { it.captureTimestamp >= cal.timeInMillis }
            }
            else -> speciesFiltered
        }
    }

    // Métricas calculadas
    val totalInspections = filteredInspections.size
    val totalAnimals = filteredInspections.sumOf { it.horseCount }
    val mistreatedAnimals = filteredInspections.sumOf { it.mistreatedHorseCount }
    val adequateAnimals = (totalAnimals - mistreatedAnimals).coerceAtLeast(0)
    val mistreatmentRate = if (totalAnimals > 0) (mistreatedAnimals.toFloat() / totalAnimals) * 100 else 0f

    val seagriCount = filteredInspections.count { it.requiresSeagriApprehension }
    val pmdfCount = filteredInspections.count { it.requiresPmdfSupport }
    val otherAgencyCount = filteredInspections.count { it.requiresOtherAgencySupport }

    // Distribuição por Região Administrativa (Top 8 RAs)
    val raDistribution = remember(filteredInspections) {
        filteredInspections
            .groupBy { it.administrativeRegion.ifBlank { "Não especificada" } }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(8)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("screen_statistics"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Cabeçalho Institucional de Estatísticas
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
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
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Estatísticas",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ESTATÍSTICAS & LEVANTAMENTOS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        val auditor = viewModel.auditorIdentification.ifBlank { "Equipe Operacional" }
                        Text(
                            text = "Auditor Fiscal: $auditor",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Métricas de Fiscalização e Bem-Estar Animal no DF",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Seletor de Espécie (Equinos / Caninos)
        item {
            SpeciesSelectorTabs(
                selectedSpecies = viewModel.selectedSpecies,
                onSpeciesSelected = { viewModel.selectSpecies(it) }
            )
        }

        // 3. Filtro de Período Temporal
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Período de Análise:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "$totalInspections levantamento(s)",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val periods = listOf(
                            "Todo o Histórico",
                            "Últimos 7 dias",
                            "Últimos 30 dias",
                            "Este Mês"
                        )
                        items(periods.indices.toList()) { index ->
                            val isSelected = selectedPeriodFilter == index
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPeriodFilter = index },
                                label = { Text(periods[index], fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. Cards de Métricas Principais (KPIs em Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Levantamentos",
                        value = totalInspections.toString(),
                        subtitle = "Vistorias no período",
                        icon = Icons.Default.Assessment,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Animais Auditados",
                        value = totalAnimals.toString(),
                        subtitle = if (species == AnimalSpecies.HORSE) "Equinos monitorados" else "Cães monitorados",
                        icon = Icons.Default.Pets,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Com Sinais de Maus-Tratos",
                        value = mistreatedAnimals.toString(),
                        subtitle = "${String.format(Locale("pt", "BR"), "%.1f", mistreatmentRate)}% dos animais",
                        icon = Icons.Default.Warning,
                        accentColor = if (mistreatedAnimals > 0) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Em Situação Adequada",
                        value = adequateAnimals.toString(),
                        subtitle = "Sem inconformidades",
                        icon = Icons.Default.CheckCircle,
                        accentColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. Barra de Progresso Comparativa do Bem-Estar
        if (totalAnimals > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Proporção de Conformidade dos Animais",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$totalAnimals total",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Barra de Progresso
                        LinearProgressIndicator(
                            progress = { if (totalAnimals > 0) adequateAnimals.toFloat() / totalAnimals else 1f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF2E7D32),
                            trackColor = if (mistreatedAnimals > 0) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF2E7D32), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Adequados: $adequateAnimals (${String.format(Locale("pt", "BR"), "%.1f", (adequateAnimals.toFloat() / totalAnimals) * 100)}%)",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFD32F2F), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Com Alertas: $mistreatedAnimals (${String.format(Locale("pt", "BR"), "%.1f", mistreatmentRate)}%)",
                                    fontSize = 10.5.sp,
                                    color = if (mistreatedAnimals > 0) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (mistreatedAnimals > 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Requisições de Apoio Operacional (SEAGRI / PMDF / Outros Órgãos)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Requisições de Apoio Interinstitucional",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // SEAGRI
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (seagriCount > 0) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = if (seagriCount > 0) BorderStroke(1.dp, Color(0xFFFFB74D)) else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "SEAGRI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (seagriCount > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Apreensão",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$seagriCount requisitado(s)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (seagriCount > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // PMDF
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (pmdfCount > 0) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = if (pmdfCount > 0) BorderStroke(1.dp, Color(0xFF90CAF9)) else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "PMDF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pmdfCount > 0) Color(0xFF1565C0) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Apoio Policial",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$pmdfCount requisitado(s)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (pmdfCount > 0) Color(0xFF1565C0) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Outros Órgãos
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (otherAgencyCount > 0) Color(0xFFF3E5F5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = if (otherAgencyCount > 0) BorderStroke(1.dp, Color(0xFFCE93D8)) else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Outros Órgãos",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (otherAgencyCount > 0) Color(0xFF7B1FA2) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "DPDF / Vigilância",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$otherAgencyCount requisitado(s)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (otherAgencyCount > 0) Color(0xFF7B1FA2) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Distribuição Geográfica por Região Administrativa (RA do DF)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
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
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Distribuição por Região Administrativa (RA)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${raDistribution.size} RAs",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (raDistribution.isEmpty()) {
                        Text(
                            text = "Nenhum dado registrado para o período selecionado.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val maxCount = raDistribution.maxOfOrNull { it.second } ?: 1
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            raDistribution.forEach { (ra, count) ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = ra,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$count vistoria(s)",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    LinearProgressIndicator(
                                        progress = { count.toFloat() / maxCount },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Botões de Ação para Exportação dos Dados Estatísticos
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Exportação de Dados & Prestação de Contas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gere relatórios estatísticos consolidados e planilhas com as fiscalizações filtradas:",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (filteredInspections.isEmpty()) {
                                    Toast.makeText(context, "Nenhum registro para exportar.", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.exportMultipleConsolidatedPdf(filteredInspections)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dossiê PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                if (filteredInspections.isEmpty()) {
                                    Toast.makeText(context, "Nenhum registro para exportar em CSV.", Toast.LENGTH_SHORT).show()
                                } else {
                                    val file = CsvExportUtils.exportInspectionsToCsv(context, filteredInspections)
                                    if (file != null) {
                                        CsvExportUtils.shareCsvFile(context, file)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Planilha CSV", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
