package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DfConstants

/**
 * Caixa de Diálogo com Botões Switch adaptando o
 * ANEXO D – PROTOCOLO DE PERÍCIA EM BEM-ESTAR ANIMAL – PPBEA
 * (Resolução CFMV nº 1.236/2018 e Manual de Verificação Inicial de Maus-Tratos)
 * para fiscalização de cães, gatos, cavalos, galináceos e outras espécies.
 */
@Composable
fun PpbeaSwitchDialog(
    species: String = "Cavalo",
    selectedAlertIndicators: List<String>,
    onToggleAlertIndicator: (String) -> Unit,
    onClearAllAlerts: () -> Unit,
    selectedAdequateIndicators: List<String>,
    onToggleAdequateIndicator: (String) -> Unit,
    onClearAllAdequate: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit = onDismiss
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedDomainFilter by remember { mutableStateOf<String?>(null) }

    val alertCategories = remember(species) {
        DfConstants.getMistreatmentCategories(species)
    }
    val adequateCategories = remember(species) {
        DfConstants.getAdequateCategories(species)
    }

    val primaryColor = if (selectedTab == 0) Color(0xFFC62828) else Color(0xFF2E7D32)
    val totalSelected = if (selectedTab == 0) selectedAlertIndicators.size else selectedAdequateIndicators.size

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("dialog_ppbea_switch"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Cabeçalho Oficial do Protocolo PPBEA
                Surface(
                    color = if (selectedTab == 0) Color(0xFFB71C1C) else Color(0xFF1B5E20),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Default.Warning else Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ANEXO D – PROTOCOLO PPBEA",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color.White.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = species.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Perícia em Bem-Estar Animal • Res. CFMV 1.236/2018",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .testTag("btn_close_ppbea_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar diálogo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // 2. Abas: Alertas de Maus-Tratos vs Condições Favoráveis
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = primaryColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = primaryColor,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            selectedDomainFilter = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⚠️", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Alertas de Maus-Tratos (${selectedAlertIndicators.size})",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (selectedTab == 0) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_ppbea_mistreatment")
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            selectedDomainFilter = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "✅", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Condições Adequadas (${selectedAdequateIndicators.size})",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (selectedTab == 1) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_ppbea_adequate")
                    )
                }

                // 3. Barra de Filtro de Domínios (Nutrição, Ambiente, Saúde, Comportamento, Manejo)
                val currentCategories = if (selectedTab == 0) {
                    alertCategories.map { it.domain to it.icon }
                } else {
                    adequateCategories.map { it.domain to it.icon }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedDomainFilter == null,
                        onClick = { selectedDomainFilter = null },
                        label = { Text("Todos os Domínios", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor.copy(alpha = 0.15f),
                            selectedLabelColor = primaryColor
                        )
                    )

                    currentCategories.forEach { (domain, icon) ->
                        val isSelected = selectedDomainFilter == domain
                        val shortTitle = domain.substringAfter(". ").substringBefore(" (")
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDomainFilter = if (isSelected) null else domain
                            },
                            label = { Text("$icon $shortTitle", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor.copy(alpha = 0.15f),
                                selectedLabelColor = primaryColor
                            )
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // 4. Lista Rolável com os Indicadores e Botões SWITCH
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            if (selectedTab == 0) Color(0xFFFFFDFD) else Color(0xFFFBFDFB)
                        )
                ) {
                    if (selectedTab == 0) {
                        // Alertas de Maus-Tratos com Switches
                        val filteredAlerts = if (selectedDomainFilter == null) {
                            alertCategories
                        } else {
                            alertCategories.filter { it.domain == selectedDomainFilter }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredAlerts.forEach { category ->
                                item {
                                    Surface(
                                        color = Color(0xFFFFEBEE),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.5.dp, Color(0xFFEF9A9A)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp, bottom = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = category.icon, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = category.domain,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = Color(0xFFB71C1C)
                                            )
                                        }
                                    }
                                }

                                items(category.items) { itemText ->
                                    val isChecked = selectedAlertIndicators.contains(itemText)
                                    PpbeaSwitchItemCard(
                                        text = itemText,
                                        isChecked = isChecked,
                                        isMistreatment = true,
                                        onToggle = { onToggleAlertIndicator(itemText) }
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    } else {
                        // Condições Favoráveis com Switches
                        val filteredAdequate = if (selectedDomainFilter == null) {
                            adequateCategories
                        } else {
                            adequateCategories.filter { it.domain == selectedDomainFilter }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredAdequate.forEach { category ->
                                item {
                                    Surface(
                                        color = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.5.dp, Color(0xFFA5D6A7)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp, bottom = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = category.icon, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = category.domain,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                }

                                items(category.items) { itemText ->
                                    val isChecked = selectedAdequateIndicators.contains(itemText)
                                    PpbeaSwitchItemCard(
                                        text = itemText,
                                        isChecked = isChecked,
                                        isMistreatment = false,
                                        onToggle = { onToggleAdequateIndicator(itemText) }
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 5. Barra Inferior de Ações: Limpar Seleção & Concluir Avaliação
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (selectedTab == 0) onClearAllAlerts() else onClearAllAdequate()
                            },
                            enabled = totalSelected > 0,
                            modifier = Modifier.testTag("btn_clear_tab_switches")
                        ) {
                            Text(
                                text = "Limpar Aba",
                                fontSize = 12.sp,
                                color = if (totalSelected > 0) primaryColor else MaterialTheme.colorScheme.outline
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_confirm_ppbea_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Concluir Avaliação ($totalSelected)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card individual com texto descritivo e Botão SWITCH para cada indicador do ANEXO D.
 */
@Composable
private fun PpbeaSwitchItemCard(
    text: String,
    isChecked: Boolean,
    isMistreatment: Boolean,
    onToggle: () -> Unit
) {
    val activeBorder = if (isMistreatment) Color(0xFFEF9A9A) else Color(0xFFA5D6A7)
    val activeBg = if (isMistreatment) Color(0xFFFFF5F5) else Color(0xFFF1F8F4)
    val inactiveBg = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle() }
            .testTag("switch_item_${text.hashCode()}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) activeBg else inactiveBg
        ),
        border = BorderStroke(
            width = if (isChecked) 1.5.dp else 1.dp,
            color = if (isChecked) activeBorder else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isChecked) {
                        if (isMistreatment) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isMistreatment) "⚠️" else "✅",
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = text,
                    fontSize = 12.5.sp,
                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isChecked) {
                        if (isMistreatment) Color(0xFFB71C1C) else Color(0xFF1B5E20)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    lineHeight = 16.sp
                )
            }

            // BOTÃO SWITCH CONFORME SOLICITADO PELO AUDITOR COM RÓTULOS SIM E NÃO
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = if (isChecked) "SIM" else "NÃO",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isChecked) {
                        if (isMistreatment) Color(0xFFC62828) else Color(0xFF2E7D32)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = isChecked,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = if (isMistreatment) Color(0xFFC62828) else Color(0xFF2E7D32),
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("switch_toggle_${text.hashCode()}")
                )
            }
        }
    }
}
