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
 * na aba de conhecimento do aplicativo.
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
        // Banner Explicativo Institucional
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GARANTIAS OBRIGATÓRIAS AOS ANIMAIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Os 5 Domínios constituem o parâmetro técnico adotado para avaliar a qualidade de vida e caracterizar infrações de maus-tratos em fiscalizações.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedDomainNumber == 0,
                    onClick = { selectedDomainNumber = 0 },
                    label = { Text("Todos (5)", fontSize = 12.sp) },
                    modifier = Modifier.testTag("filter_domain_all")
                )
            }
            items(allDomains, key = { it.number }) { domain ->
                FilterChip(
                    selected = selectedDomainNumber == domain.number,
                    onClick = { selectedDomainNumber = domain.number },
                    label = { Text("${domain.number}. ${domain.shortName}", fontSize = 12.sp) },
                    modifier = Modifier.testTag("filter_domain_${domain.number}")
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
                    tint = MaterialTheme.colorScheme.primary
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

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("domain_card_${domain.number}")
            .animateContentSize(tween(300)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "DOMÍNIO ${domain.number}",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = domain.tag,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Título e Subtítulo do Domínio
            Text(
                text = domain.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = domain.subtitle,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Seção de Destaque: "O que deve ser garantido ao animal"
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "O QUE DEVE SER GARANTIDO:",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = domain.whatMustBeGuaranteed,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
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
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    domain.auditIndicators.forEach { indicator ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(indicator, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Sinais Positivos (Conformidade)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1B5E20).copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sinais de Atendimento / Regularidade:",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            domain.positiveIndicators.forEach { pos ->
                                Text(
                                    text = "✓ $pos",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1B5E20),
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
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Indícios de Violação / Maus-Tratos:",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            domain.violationIndicators.forEach { viol ->
                                Text(
                                    text = "⚠ $viol",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
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
