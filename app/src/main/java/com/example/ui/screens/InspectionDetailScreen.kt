package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.DfConstants
import com.example.ui.viewmodel.InspectionViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionDetailScreen(
    inspectionId: Long,
    viewModel: InspectionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val allInspections by viewModel.allInspections.collectAsStateWithLifecycle()
    val inspection = allInspections.find { it.id == inspectionId }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showZoomPhoto by remember { mutableStateOf(false) }
    var selectedPhotoIndex by remember { mutableIntStateOf(0) }

    BackHandler {
        onNavigateBack()
    }

    if (inspection == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Registro não encontrado")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = inspection.protocolNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = inspection.administrativeRegion,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Watermarked Photo & Multi-Photo Gallery
            val allPhotos = inspection.allPhotoPaths
            val activeIndex = selectedPhotoIndex.coerceIn(0, kotlin.math.max(0, allPhotos.lastIndex))
            val activePhotoPath = allPhotos.getOrNull(activeIndex) ?: inspection.photoPath

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showZoomPhoto = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.Black)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    val file = File(activePhotoPath)
                    if (file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Foto ${activeIndex + 1} da ocorrência com marca d'água",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Top Left: Carimbo OK & Photo Counter
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Carimbo Fiscalizatório OK",
                                color = Color(0xFF81C784),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        if (allPhotos.size > 1) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "📸 ${activeIndex + 1} de ${allPhotos.size}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Top Right: Fullscreen Zoom
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ampliar Foto", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Thumbnail strip if multiple photos
            if (allPhotos.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(allPhotos) { idx, path ->
                        val isSelected = idx == activeIndex
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPhotoIndex = idx }
                        ) {
                            AsyncImage(
                                model = File(path),
                                contentDescription = "Miniatura ${idx + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xCC000000),
                                shape = RoundedCornerShape(bottomEnd = 4.dp),
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Quantitativo de Cavalos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Quantidade de Cavalos",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${inspection.horseCount} cavalo(s)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (inspection.mistreatedHorseCount > 0) {
                        Surface(
                            color = Color(0xFFFFCDD2),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${inspection.mistreatedHorseCount} c/ sinais de maus-tratos",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB71C1C),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Sem sinais de maus-tratos",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 2.1 Horse Description Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Descrição do(s) Cavalo(s)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val descText = if (inspection.horseDescription.isNotBlank()) inspection.horseDescription else "Nenhuma descrição física específica registrada."
                    Text(
                        text = descText,
                        fontSize = 13.sp
                    )
                }
            }

            // 2.2 Mistreatment Indicators Card
            if (inspection.mistreatmentList.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Constatações da Fiscalização de Maus-Tratos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFB71C1C)
                            )
                        }
                        Text(
                            text = "Fundamentado na Res. CFMV nº 1.236/2018 e CRMV-MS",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        inspection.mistreatmentList.forEach { ind ->
                            Text(
                                text = "• $ind",
                                fontSize = 12.sp,
                                color = Color(0xFFB71C1C),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 2.3 Adequate Welfare Indicators Card
            if (inspection.adequateList.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6FBF7)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Condições Adequadas de Bem-Estar Constatadas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        Text(
                            text = "Parâmetros positivos de bem-estar (Res. CFMV nº 1.236/2018)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        inspection.adequateList.forEach { ind ->
                            Text(
                                text = "✓ $ind",
                                fontSize = 12.sp,
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 2.4 Operational Provisions (SEAGRI & PMDF)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Apreensão SEAGRI & Apoio da Polícia Militar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // SEAGRI Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ação fiscal necessita de apreensão (SEAGRI):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Surface(
                            color = if (inspection.requiresSeagriApprehension) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (inspection.requiresSeagriApprehension) "SIM (SOLICITADO)" else "NÃO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (inspection.requiresSeagriApprehension) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (inspection.requiresSeagriApprehension && inspection.seagriNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Obs: ${inspection.seagriNotes}",
                            fontSize = 12.sp,
                            color = Color(0xFFE65100)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // PMDF Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Irá precisar de apoio da Polícia Militar:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Surface(
                            color = if (inspection.requiresPmdfSupport) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (inspection.requiresPmdfSupport) "SIM (PMDF / BPMA)" else "NÃO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (inspection.requiresPmdfSupport) Color(0xFF1565C0) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (inspection.requiresPmdfSupport && inspection.pmdfNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Justificativa: ${inspection.pmdfNotes}",
                            fontSize = 12.sp,
                            color = Color(0xFF1565C0)
                        )
                    }
                }
            }

            // 3. Location & GPS
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
                        Column {
                            Text(
                                text = "Endereço da Ação Fiscal & RA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = inspection.formattedAddress,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val geoUri = Uri.parse("geo:${inspection.latitude},${inspection.longitude}?q=${inspection.latitude},${inspection.longitude}(Fiscalizacao+Equina)")
                                val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                try {
                                    context.startActivity(mapIntent)
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Abrir Mapa", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val coordsFormatted = String.format(java.util.Locale.US, "Latitude: %.6f° | Longitude: %.6f°", inspection.latitude, inspection.longitude)
                    Text(
                        text = coordsFormatted,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Data e Hora da Captura: ${inspection.formattedCaptureDate}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4. Tutores / Responsáveis
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val tutors = inspection.allTutors
                    Text(
                        text = if (tutors.size > 1) "Tutores / Responsáveis (${tutors.size})" else "Tutor / Responsável",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (tutors.isEmpty()) {
                        Text(
                            text = "Não identificado no momento da vistoria",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        tutors.forEachIndexed { index, tutor ->
                            if (index > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Text(
                                text = if (tutors.size > 1) "Tutor #${index + 1}" else "Identificação:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Nome: ${if (tutor.name.isNotBlank()) tutor.name else "Não informado"}",
                                fontSize = 13.sp
                            )
                            Text(
                                text = "CPF: ${if (tutor.cpf.isNotBlank()) tutor.cpf else "Não informado"}",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // 5. Notes
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Notas de Campo / Descrição",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val notesText = if (inspection.imageNotes.isNotBlank()) inspection.imageNotes else "Sem observações de campo cadastradas."
                    Text(
                        text = notesText,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Export PDF
            Button(
                onClick = { viewModel.exportSinglePdf(inspection) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("detail_export_pdf_button"),
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEditDialog) {
        EditInspectionDialog(
            inspection = inspection,
            onDismiss = { showEditDialog = false },
            onSave = { notes, ra, quadra, conjunto, numero, horseDescription, horseCount, mistreatedHorseCount, mistreatmentIndicators, adequateIndicators, requiresSeagriApprehension, seagriNotes, requiresPmdfSupport, pmdfNotes, risk, safetyRiskAssessment, tutor, cpf, additionalTutors ->
                viewModel.updateInspection(
                    original = inspection,
                    newNotes = notes,
                    newRa = ra,
                    newQuadra = quadra,
                    newConjunto = conjunto,
                    newNumero = numero,
                    newHorseDescription = horseDescription,
                    newHorseCount = horseCount,
                    newMistreatedHorseCount = mistreatedHorseCount,
                    newMistreatmentIndicators = mistreatmentIndicators,
                    newAdequateIndicators = adequateIndicators,
                    newRequiresSeagriApprehension = requiresSeagriApprehension,
                    newSeagriNotes = seagriNotes,
                    newRequiresPmdfSupport = requiresPmdfSupport,
                    newPmdfNotes = pmdfNotes,
                    newRiskLevel = risk,
                    newSafetyRiskAssessment = safetyRiskAssessment,
                    newTutorName = tutor,
                    newTutorCpf = cpf,
                    newAdditionalTutors = additionalTutors,
                    onSuccess = {
                        showEditDialog = false
                    }
                )
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Registro?") },
            text = { Text("Deseja realmente excluir este registro permanentemente?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteInspection(inspection)
                        showDeleteConfirm = false
                        onNavigateBack()
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showZoomPhoto) {
        PhotoZoomDialog(
            photoPaths = inspection.allPhotoPaths,
            initialIndex = selectedPhotoIndex,
            onDismiss = { showZoomPhoto = false }
        )
    }
}
