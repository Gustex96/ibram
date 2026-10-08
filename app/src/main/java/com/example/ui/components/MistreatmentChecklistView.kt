package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.DfConstants

@Composable
fun MistreatmentChecklistView(
    selectedAlertIndicators: List<String>,
    onToggleAlertIndicator: (String) -> Unit,
    onClearAllAlerts: () -> Unit,
    selectedAdequateIndicators: List<String> = emptyList(),
    onToggleAdequateIndicator: (String) -> Unit = {},
    onClearAllAdequate: () -> Unit = {},
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    val primaryAccent = if (selectedTab == 0) Color(0xFFC62828) else Color(0xFF2E7D32)
    val containerBg = if (selectedTab == 0) Color(0xFFFFF8F8) else Color(0xFFF6FBF7)
    val cardBorder = if (selectedTab == 0) Color(0xFFFFCDD2) else Color(0xFFA5D6A7)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("welfare_checklist_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = primaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Avaliação Preliminar de Bem-Estar Animal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = primaryAccent
                    )
                }

                Surface(
                    color = if (selectedTab == 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Res. CFMV 1.236/2018 & CRMV",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual Tabs: Alertas vs Adequados
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tab 0: Alertas / Maus-Tratos
                    val isTab0 = selectedTab == 0
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedTab = 0 },
                        color = if (isTab0) Color(0xFFFFEBEE) else Color.Transparent,
                        border = if (isTab0) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️ Inadequações (${selectedAlertIndicators.size})",
                                fontSize = 11.sp,
                                fontWeight = if (isTab0) FontWeight.Bold else FontWeight.Medium,
                                color = if (isTab0) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Tab 1: Adequados / Bem-Estar
                    val isTab1 = selectedTab == 1
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedTab = 1 },
                        color = if (isTab1) Color(0xFFE8F5E9) else Color.Transparent,
                        border = if (isTab1) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅ Adequados (${selectedAdequateIndicators.size})",
                                fontSize = 11.sp,
                                fontWeight = if (isTab1) FontWeight.Bold else FontWeight.Medium,
                                color = if (isTab1) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "checklist_content"
            ) { tab ->
                if (tab == 0) {
                    // TAB 0: ALERTAS DE MAUS-TRATOS
                    Column {
                        Text(
                            text = "Assinale os sinais de sofrimento ou comprometimento do bem-estar observados:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DfConstants.MISTREATMENT_CATEGORIES.forEach { category ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "${category.icon} ${category.domain}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF37474F)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                category.items.forEach { item ->
                                    val isChecked = selectedAlertIndicators.contains(item)
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onToggleAlertIndicator(item) }
                                            .border(
                                                width = if (isChecked) 1.5.dp else 0.5.dp,
                                                color = if (isChecked) Color(0xFFC62828) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        color = if (isChecked) Color(0xFFFFEBEE) else Color.White
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isChecked) Color(0xFFC62828) else Color.Transparent)
                                                    .border(
                                                        width = 1.5.dp,
                                                        color = if (isChecked) Color(0xFFC62828) else MaterialTheme.colorScheme.outline,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isChecked) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Text(
                                                text = item,
                                                fontSize = 12.sp,
                                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isChecked) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 15.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Footer Tab 0
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedAlertIndicators.size} inadequação(ões) apontada(s)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedAlertIndicators.isNotEmpty()) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (selectedAlertIndicators.isNotEmpty()) {
                                TextButton(
                                    onClick = onClearAllAlerts,
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = Color(0xFFC62828),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Limpar Alertas",
                                        fontSize = 11.sp,
                                        color = Color(0xFFC62828)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: CONDIÇÕES ADEQUADAS / BEM-ESTAR
                    Column {
                        Text(
                            text = "Assinale as condições adequadas e parâmetros positivos de bem-estar observados:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DfConstants.ADEQUATE_CATEGORIES.forEach { category ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "${category.icon} ${category.domain}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                category.items.forEach { item ->
                                    val isChecked = selectedAdequateIndicators.contains(item)
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onToggleAdequateIndicator(item) }
                                            .border(
                                                width = if (isChecked) 1.5.dp else 0.5.dp,
                                                color = if (isChecked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        color = if (isChecked) Color(0xFFE8F5E9) else Color.White
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isChecked) Color(0xFF2E7D32) else Color.Transparent)
                                                    .border(
                                                        width = 1.5.dp,
                                                        color = if (isChecked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isChecked) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Text(
                                                text = item,
                                                fontSize = 12.sp,
                                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isChecked) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 15.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Footer Tab 1
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedAdequateIndicators.size} ponto(s) adequado(s)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedAdequateIndicators.isNotEmpty()) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (selectedAdequateIndicators.isNotEmpty()) {
                                TextButton(
                                    onClick = onClearAllAdequate,
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Limpar Adequados",
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
