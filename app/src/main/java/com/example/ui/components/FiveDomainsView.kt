package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnimalWelfareDomain
import com.example.data.model.FiveDomainsData

/**
 * Visualizador completo e interativo dos 5 Domínios do Bem-Estar Animal
 * na aba de conhecimento do aplicativo, com paleta verde institucional e cartões brancos limpos.
 */
@Composable
fun FiveDomainsView(
    modifier: Modifier = Modifier
) {
    var selectedDomainNumber by remember { mutableIntStateOf(0) } // 0: Todos, 1 a 5: domínio específico
    var searchQuery by remember { mutableStateOf("") }

    val allDomains = FiveDomainsData.domains

    val filteredDomains = remember(selectedDomainNumber, searchQuery) {
        val base = if (selectedDomainNumber == 0) {
            allDomains
        } else {
            allDomains.filter { it.number == selectedDomainNumber }
        }

        if (searchQuery.isBlank()) {
            base
        } else {
            val q = searchQuery.trim().lowercase()
            base.filter { domain ->
                domain.title.lowercase().contains(q) ||
                        domain.subtitle.lowercase().contains(q) ||
                        domain.whatMustBeGuaranteed.lowercase().contains(q) ||
                        domain.auditIndicators.any { it.lowercase().contains(q) } ||
                        domain.positiveIndicators.any { it.lowercase().contains(q) } ||
                        domain.violationIndicators.any { it.lowercase().contains(q) }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("five_domains_view")
    ) {
        // Banner Explicativo Institucional (Card Branco Refinado)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = Color.White,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF14532D),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GARANTIAS OBRIGATÓRIAS AOS ANIMAIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = Color(0xFF14532D)
                    )
                    Text(
                        text = "Os 5 Domínios constituem o parâmetro técnico adotado para avaliar a qualidade de vida e caracterizar infrações de maus-tratos em fiscalizações.",
                        fontSize = 11.sp,
                        color = Color(0xFF334155),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Seletor Horizontal de Domínios
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                val isSelected = selectedDomainNumber == 0
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedDomainNumber = 0 },
                    label = { Text("Todos (5)", fontSize = 11.5.sp) },
                    modifier = Modifier.testTag("filter_domain_all"),
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
            items(allDomains, key = { it.number }) { domain ->
                val isSelected = selectedDomainNumber == domain.number
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedDomainNumber = domain.number },
                    label = { Text("${domain.number}. ${domain.shortName}", fontSize = 11.5.sp) },
                    modifier = Modifier.testTag("filter_domain_${domain.number}"),
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

        // Campo de Busca Rápida por Palavra-Chave nos Domínios
        AppOutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("search_domains_field"),
            placeholder = { Text("Buscar nos 5 domínios (ex: água, sombra, dor, corrente...)", fontSize = 11.5.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF14532D)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpar busca", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        // Lista dos Cards dos 5 Domínios
        if (filteredDomains.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum critério encontrado para \"$searchQuery\".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(filteredDomains, key = { it.number }) { domain ->
                    DomainDetailCard(domain = domain)
                }
            }
        }
    }
}

/**
 * Card detalhado de cada um dos 5 Domínios de Bem-Estar Animal.
 */
@Composable
private fun DomainDetailCard(
    domain: AnimalWelfareDomain
) {
    var isExpanded by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("domain_card_${domain.number}")
            .animateContentSize(tween(300)),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Cabeçalho do Card com Número e Tag do Domínio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "DOMÍNIO ${domain.number}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF022C22)
                    )
                }

                Surface(
                    color = Color(0xFFD1FAE5),
                    shape = CircleShape
                ) {
                    Text(
                        text = domain.tag,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF14532D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Título e Subtítulo do Domínio
            Text(
                text = domain.title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = domain.subtitle,
                fontSize = 11.5.sp,
                color = Color(0xFF14532D),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Seção de Destaque: "O que deve ser garantido ao animal"
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF0FDF4),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF14532D),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "O QUE DEVE SER GARANTIDO:",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14532D)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = domain.whatMustBeGuaranteed,
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B),
                        lineHeight = 16.5.sp
                    )
                }
            }

            // Alternador de Detalhes de Fiscalização
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Ocultar critérios de campo" else "Ver critérios de fiscalização e indicadores",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF14532D)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF14532D),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    // 1. Critérios de Avaliação Técnica
                    Text(
                        text = "O que verificar na fiscalização in loco:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    domain.auditIndicators.forEach { indicator ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", fontSize = 12.sp, color = Color(0xFF14532D), fontWeight = FontWeight.Bold)
                            Text(indicator, fontSize = 11.5.sp, color = Color(0xFF334155), lineHeight = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Sinais Positivos (Conformidade)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFD1FAE5).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF14532D),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sinais de Atendimento / Regularidade:",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14532D)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            domain.positiveIndicators.forEach { pos ->
                                Text(
                                    text = "✓ $pos",
                                    fontSize = 11.sp,
                                    color = Color(0xFF065F46),
                                    lineHeight = 14.5.sp,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. Sinais de Alerta / Violação (Maus-Tratos)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFEE2E2).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Indícios de Violação / Maus-Tratos:",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            domain.violationIndicators.forEach { viol ->
                                Text(
                                    text = "⚠ $viol",
                                    fontSize = 11.sp,
                                    color = Color(0xFF7F1D1D),
                                    lineHeight = 14.5.sp,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
