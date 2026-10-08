package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DfConstants
import com.example.data.model.HorseInspection
import com.example.data.model.TutorEntry
import com.example.ui.components.MistreatmentChecklistView
import com.example.util.CnpjConsultationHelper
import com.example.util.CpfValidator

@Composable
fun EditInspectionDialog(
    inspection: HorseInspection,
    onDismiss: () -> Unit,
    onSave: (
        notes: String,
        ra: String,
        quadra: String,
        conjunto: String,
        numero: String,
        horseDescription: String,
        horseCount: Int,
        mistreatedHorseCount: Int,
        mistreatmentIndicators: String,
        adequateIndicators: String,
        requiresSeagriApprehension: Boolean,
        seagriNotes: String,
        requiresPmdfSupport: Boolean,
        pmdfNotes: String,
        riskLevel: Int,
        safetyRiskAssessment: String,
        tutorName: String,
        tutorCpf: String,
        additionalTutors: String
    ) -> Unit
) {
    var notes by remember { mutableStateOf(inspection.imageNotes) }
    var ra by remember { mutableStateOf(inspection.administrativeRegion) }
    var quadra by remember { mutableStateOf(inspection.quadra) }
    var conjunto by remember { mutableStateOf(inspection.conjunto) }
    var numero by remember { mutableStateOf(inspection.numero) }
    var horseDescription by remember { mutableStateOf(inspection.horseDescription) }
    var horseCount by remember { mutableIntStateOf(inspection.horseCount) }
    var mistreatedHorseCount by remember { mutableIntStateOf(inspection.mistreatedHorseCount) }
    var mistreatmentIndicators by remember { mutableStateOf(inspection.mistreatmentList) }
    var adequateIndicators by remember { mutableStateOf(inspection.adequateList) }
    var requiresSeagriApprehension by remember { mutableStateOf(inspection.requiresSeagriApprehension) }
    var seagriNotes by remember { mutableStateOf(inspection.seagriNotes) }
    var requiresPmdfSupport by remember { mutableStateOf(inspection.requiresPmdfSupport) }
    var pmdfNotes by remember { mutableStateOf(inspection.pmdfNotes) }
    var riskLevel by remember { mutableIntStateOf(inspection.riskLevel) }
    var safetyRiskAssessment by remember { mutableStateOf(inspection.safetyRiskAssessment) }
    val context = LocalContext.current
    var tutorsList by remember {
        mutableStateOf(
            if (inspection.allTutors.isNotEmpty()) inspection.allTutors
            else listOf(TutorEntry(inspection.tutorName, inspection.tutorCpf))
        )
    }

    var showRaSelector by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val currentRiskInfo = DfConstants.RISK_LEVELS.find { it.level == riskLevel }
    val currentRiskColor = Color(currentRiskInfo?.colorHex ?: 0xFFF57C00)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(700.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Editar Fiscalização",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Protocolo: ${inspection.protocolNumber}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Endereço da Ação Fiscal (RA, Quadra, Conjunto, Número)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "1. Endereço da Ação Fiscal *",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Região Administrativa (RA)
                            Text(
                                text = "Região Administrativa (RA) *",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showRaSelector = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = ra, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    }
                                    Text("Alterar RA", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quadra
                            OutlinedTextField(
                                value = quadra,
                                onValueChange = { quadra = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Quadra (Q.)") },
                                placeholder = { Text("Ex: Quadra 04, QNM 18, SQS 308...") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Conjunto e Número
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = conjunto,
                                    onValueChange = { conjunto = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Conjunto (Conj.)") },
                                    placeholder = { Text("Ex: Conj. A...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = numero,
                                    onValueChange = { numero = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Número / Lote (Nº)") },
                                    placeholder = { Text("Ex: Nº 15...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    // 2. Quantidade de Cavalos
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "2. Quantidade de Cavalos *",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "Total de animais:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Defina a quantidade de equinos",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (horseCount > 1) horseCount-- },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                            .testTag("edit_decrease_horses")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Diminuir",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = horseCount.toString(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { horseCount++ },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                            .testTag("edit_increase_horses")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Aumentar",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick preset selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(1, 2, 3, 5, 10).forEach { count ->
                                    val isSelected = horseCount == count
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { horseCount = count }
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(6.dp)
                                            ),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = if (count == 10) "10+" else "$count",
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2.1 Cavalos com Possíveis Sinais de Maus-Tratos
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (mistreatedHorseCount > 0) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Sinais de Maus-Tratos",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (mistreatedHorseCount > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                                        )
                                        if (mistreatedHorseCount > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFFC62828),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "ALERTA",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Equinos com ferimentos ou desnutrição",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (mistreatedHorseCount > 0) mistreatedHorseCount-- },
                                        enabled = mistreatedHorseCount > 0,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                if (mistreatedHorseCount > 0) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Diminuir maus-tratos",
                                            tint = if (mistreatedHorseCount > 0) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (mistreatedHorseCount > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .width(48.dp)
                                            .height(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = mistreatedHorseCount.toString(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                color = if (mistreatedHorseCount > 0) Color.White else MaterialTheme.colorScheme.onSurface,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { if (mistreatedHorseCount < horseCount) mistreatedHorseCount++ },
                                        enabled = mistreatedHorseCount < horseCount,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                if (mistreatedHorseCount < horseCount) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Aumentar maus-tratos",
                                            tint = if (mistreatedHorseCount < horseCount) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Forensic Welfare Checklist (Res. CFMV 1.236/2018 & CRMV-MS)
                            val isChecklistVisible = mistreatedHorseCount > 0 || adequateIndicators.isNotEmpty() || mistreatmentIndicators.isNotEmpty()

                            if (isChecklistVisible) {
                                Spacer(modifier = Modifier.height(10.dp))
                                MistreatmentChecklistView(
                                    selectedAlertIndicators = mistreatmentIndicators,
                                    onToggleAlertIndicator = { item ->
                                        val mutable = mistreatmentIndicators.toMutableList()
                                        if (mutable.contains(item)) mutable.remove(item) else mutable.add(item)
                                        mistreatmentIndicators = mutable
                                    },
                                    onClearAllAlerts = { mistreatmentIndicators = emptyList() },
                                    selectedAdequateIndicators = adequateIndicators,
                                    onToggleAdequateIndicator = { item ->
                                        val mutable = adequateIndicators.toMutableList()
                                        if (mutable.contains(item)) mutable.remove(item) else mutable.add(item)
                                        adequateIndicators = mutable
                                    },
                                    onClearAllAdequate = { adequateIndicators = emptyList() },
                                    initialTab = if (mistreatedHorseCount > 0) 0 else 1
                                )
                            }
                        }
                    }

                    // 2.2 Descrição do Cavalo
                    Column {
                        Text(
                            text = "Descrição do(s) Cavalo(s)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = horseDescription,
                            onValueChange = { horseDescription = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ex: Pelagem, porte, ferimentos, raça, sinais de identificação...") },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // 3. Providências Operacionais (SEAGRI & PMDF)
                    Column {
                        Text(
                            text = "Apreensão pela SEAGRI & Apoio Policial (PMDF)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // SEAGRI
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            text = "Ação fiscal necessita de apreensão por parte da SEAGRI?",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (requiresSeagriApprehension) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Recolhimento e transporte ao curral público da SEAGRI-DF",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                                                .clickable { requiresSeagriApprehension = true },
                                            color = if (requiresSeagriApprehension) Color(0xFFE65100) else Color.Transparent
                                        ) {
                                            Text(
                                                text = "SIM",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (requiresSeagriApprehension) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                                                .clickable { requiresSeagriApprehension = false },
                                            color = if (!requiresSeagriApprehension) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
                                        ) {
                                            Text(
                                                text = "NÃO",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (!requiresSeagriApprehension) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                if (requiresSeagriApprehension) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = seagriNotes,
                                        onValueChange = { seagriNotes = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Ex: Caminhão gaiola para 2 equinos soltos...") },
                                        label = { Text("Observações da Apreensão (SEAGRI)") },
                                        minLines = 2,
                                        maxLines = 3,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // PMDF
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            text = "Irá precisar de apoio da Polícia Militar?",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (requiresPmdfSupport) Color(0xFF1565C0) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Apoio da PMDF (segurança da equipe fiscal, BPMA ou BPTran)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                                                .clickable { requiresPmdfSupport = true },
                                            color = if (requiresPmdfSupport) Color(0xFF1565C0) else Color.Transparent
                                        ) {
                                            Text(
                                                text = "SIM",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (requiresPmdfSupport) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                                                .clickable { requiresPmdfSupport = false },
                                            color = if (!requiresPmdfSupport) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
                                        ) {
                                            Text(
                                                text = "NÃO",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (!requiresPmdfSupport) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                if (requiresPmdfSupport) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = pmdfNotes,
                                        onValueChange = { pmdfNotes = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Ex: Segurança da equipe de fiscalização e contenção de trânsito...") },
                                        label = { Text("Justificativa do Apoio Policial (PMDF)") },
                                        minLines = 2,
                                        maxLines = 3,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Notas de Campo
                    Column {
                        Text(
                            text = "Notas de Campo / Descrição da Imagem",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Descreva o ambiente e o estado dos cavalos...") },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // 5. Dados dos Tutores
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tutores / Responsáveis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        tutorsList.forEachIndexed { index, tutor ->
                            val isCpfValid = CpfValidator.isValid(tutor.cpf)
                            val isCpfFilled = tutor.cpf.isNotBlank()

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (index == 0) "Tutor 1 (Principal)" else "Tutor ${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        if (tutorsList.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    val updated = tutorsList.toMutableList()
                                                    updated.removeAt(index)
                                                    tutorsList = updated
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remover tutor",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    OutlinedTextField(
                                        value = tutor.name,
                                        onValueChange = { newName ->
                                            val updated = tutorsList.toMutableList()
                                            updated[index] = tutor.copy(name = newName)
                                            tutorsList = updated
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Nome do Tutor") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = tutor.cpf,
                                            onValueChange = { rawCpf ->
                                                val updated = tutorsList.toMutableList()
                                                updated[index] = tutor.copy(cpf = CpfValidator.format(rawCpf))
                                                tutorsList = updated
                                            },
                                            modifier = Modifier.weight(1f),
                                            label = { Text("CPF / CNPJ") },
                                            placeholder = { Text("000.000.000-00 ou CNPJ") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp),
                                            trailingIcon = {
                                                if (isCpfFilled && isCpfValid) {
                                                    Icon(Icons.Default.Check, contentDescription = "Válido", tint = Color(0xFF2E7D32))
                                                }
                                            }
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        FilledTonalButton(
                                            onClick = {
                                                CnpjConsultationHelper.abrirConsultaReceitaFederal(context, tutor.cpf)
                                            },
                                            modifier = Modifier.height(56.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Consultar\nCNPJ",
                                                    fontSize = 10.sp,
                                                    lineHeight = 11.sp,
                                                    textAlign = TextAlign.Center,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                tutorsList = tutorsList + TutorEntry("", "")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Adicionar Outro Tutor", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val alertIndicatorsToSave = if (mistreatedHorseCount == 0) "" else mistreatmentIndicators.joinToString(";;")
                            val adequateIndicatorsToSave = adequateIndicators.joinToString(";;")
                            val validTutors = tutorsList.filter { it.name.isNotBlank() || it.cpf.isNotBlank() }
                            val prim = validTutors.firstOrNull() ?: tutorsList.firstOrNull() ?: TutorEntry("", "")
                            val add = if (validTutors.size > 1) validTutors.drop(1).joinToString(";;") { "${it.name}|${it.cpf}" } else ""
                            onSave(notes, ra, quadra, conjunto, numero, horseDescription, horseCount, mistreatedHorseCount, alertIndicatorsToSave, adequateIndicatorsToSave, requiresSeagriApprehension, seagriNotes, requiresPmdfSupport, pmdfNotes, riskLevel, safetyRiskAssessment, prim.name, prim.cpf, add)
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salvar Alterações", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showRaSelector) {
        RaSelectionDialog(
            currentRa = ra,
            onSelectRa = {
                ra = it
                showRaSelector = false
            },
            onDismiss = { showRaSelector = false }
        )
    }
}
