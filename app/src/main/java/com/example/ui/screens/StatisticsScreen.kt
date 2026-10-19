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
            AnimalSpecies.HORSE -> allInspections.filter { !it.isDog && (it.species.isBlank() || it.species.equals("Cavalo", ignoreCase = true) || it.species.equals("horse", ignoreCase = true)) }
            AnimalSpecies.DOG -> allInspections.filter { it.isDog }
            AnimalSpecies.CAT -> allInspections.filter { it.species.equals("Gato", ignoreCase = true) || it.species.equals("cat", ignoreCase = true) }
            AnimalSpecies.POULTRY -> allInspections.filter { it.species.equals("Galináceos", ignoreCase = true) || it.species.equals("poultry", ignoreCase = true) }
            AnimalSpecies.OUTROS -> allInspections.filter {
                !it.isDog && !it.species.equals("Cavalo", ignoreCase = true) && !it.species.equals("Gato", ignoreCase = true) && !it.species.equals("Galináceos", ignoreCase = true)
            }
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
            .background(Color(0xFFF4FAF6))
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("screen_statistics"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // 1. Cabeçalho Institucional de Estatísticas (Card Branco Refinado)
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
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF14532D),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Estatísticas",
                                tint = Color.White,
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
                            color = Color(0xFF14532D),
                            letterSpacing = 0.5.sp
                        )
                        val auditor = viewModel.auditorIdentification.ifBlank { "Equipe Operacional" }
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                        ) {
                            Text(
                                text = "Auditor Fiscal: $auditor",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF14532D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "Métricas de Fiscalização e Bem-Estar Animal no DF",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // 2. Filtro de Período Temporal
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
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
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Período de Análise:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Surface(
                            color = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "$totalInspections levantamento(s)",
                                fontSize = 11.sp,
                                color = Color(0xFF022C22),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
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
                                label = { Text(periods[index], fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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
                }
            }
        }

        // 3. Cards de Métricas Principais (KPIs em Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Levantamentos",
                        value = totalInspections.toString(),
                        subtitle = "Vistorias no período",
                        icon = Icons.Default.Assessment,
                        accentColor = Color(0xFF14532D),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Animais Auditados",
                        value = totalAnimals.toString(),
                        subtitle = if (species == AnimalSpecies.HORSE) "Equinos monitorados" else "Cães monitorados",
                        icon = Icons.Default.Pets,
                        accentColor = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Com Sinais",
                        value = mistreatedAnimals.toString(),
                        subtitle = "${String.format(Locale("pt", "BR"), "%.1f", mistreatmentRate)}% dos animais",
                        icon = Icons.Default.Warning,
                        accentColor = if (mistreatedAnimals > 0) Color(0xFFDC2626) else Color(0xFF14532D),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Adequados",
                        value = adequateAnimals.toString(),
                        subtitle = "Sem inconformidades",
                        icon = Icons.Default.CheckCircle,
                        accentColor = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Barra de Progresso Comparativa do Bem-Estar
        if (totalAnimals > 0) {
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
                            Text(
                                text = "Proporção de Conformidade dos Animais",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "$totalAnimals total",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Barra de Progresso
                        LinearProgressIndicator(
                            progress = { if (totalAnimals > 0) adequateAnimals.toFloat() / totalAnimals else 1f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF16A34A),
                            trackColor = if (mistreatedAnimals > 0) Color(0xFFFECDD3) else Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF16A34A), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Adequados: $adequateAnimals (${String.format(Locale("pt", "BR"), "%.1f", (adequateAnimals.toFloat() / totalAnimals) * 100)}%)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFDC2626), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Com Alertas: $mistreatedAnimals (${String.format(Locale("pt", "BR"), "%.1f", mistreatmentRate)}%)",
                                    fontSize = 11.sp,
                                    color = if (mistreatedAnimals > 0) Color(0xFFDC2626) else Color(0xFF64748B),
                                    fontWeight = if (mistreatedAnimals > 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Requisições de Apoio Operacional (SEAGRI / PMDF / Outros Órgãos)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF14532D),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Requisições de Apoio Interinstitucional",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // SEAGRI
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (seagriCount > 0) Color(0xFFFEF3C7) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (seagriCount > 0) Color(0xFFF59E0B) else Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "SEAGRI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (seagriCount > 0) Color(0xFF92400E) else Color(0xFF64748B)
                                )
                                Text(
                                    text = "Apreensão",
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$seagriCount pedido(s)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (seagriCount > 0) Color(0xFF92400E) else Color(0xFF0F172A)
                                )
                            }
                        }

                        // PMDF
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (pmdfCount > 0) Color(0xFFDBEAFE) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (pmdfCount > 0) Color(0xFF3B82F6) else Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "PMDF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pmdfCount > 0) Color(0xFF1E40AF) else Color(0xFF64748B)
                                )
                                Text(
                                    text = "Apoio Policial",
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$pmdfCount pedido(s)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (pmdfCount > 0) Color(0xFF1E40AF) else Color(0xFF0F172A)
                                )
                            }
                        }

                        // Outros Órgãos
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (otherAgencyCount > 0) Color(0xFFF3E8FF) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (otherAgencyCount > 0) Color(0xFFA855F7) else Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Outros",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (otherAgencyCount > 0) Color(0xFF6B21A8) else Color(0xFF64748B)
                                )
                                Text(
                                    text = "DPDF/Vig.",
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$otherAgencyCount pedido(s)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (otherAgencyCount > 0) Color(0xFF6B21A8) else Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Distribuição Geográfica por Região Administrativa (RA do DF)
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
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Distribuição por Região Administrativa (RA)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Surface(
                            color = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${raDistribution.size} RAs",
                                fontSize = 11.sp,
                                color = Color(0xFF022C22),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (raDistribution.isEmpty()) {
                        Text(
                            text = "Nenhum dado registrado para o período selecionado.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
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
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "$count vistoria(s)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF14532D)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = { count.toFloat() / maxCount },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFF14532D),
                                        trackColor = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Botões de Ação para Exportação dos Dados Estatísticos
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Exportação de Dados & Prestação de Contas",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gere relatórios estatísticos consolidados e planilhas com as fiscalizações filtradas:",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (filteredInspections.isEmpty()) {
                                    Toast.makeText(context, "Nenhum registro para exportar.", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.exportMultipleConsolidatedPdf(filteredInspections)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF14532D)
                            )
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dossiê PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF14532D)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF14532D), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Planilha CSV", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                maxLines = 1
            )
        }
    }
}
