package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.DfConstants
import com.example.data.model.EnquadramentoMausTratos
import com.example.data.model.GrauBemEstar
import com.example.data.model.HorseInspection
import com.example.data.model.ParecerDomain
import com.example.data.model.PpbeaCriteriaRepository
import com.example.data.model.PpbeaCriterion
import com.example.data.model.IndividualAnimalEvaluation
import com.example.data.model.IndividualAnimalSerializer
import com.example.data.model.PpbeaDiagnosis
import com.example.data.model.PpbeaDomain
import com.example.data.model.TutorEntry
import com.example.ui.components.AppOutlinedTextField
import com.example.ui.components.PpbeaEvaluationDialog
import com.example.ui.components.PpbeaOpenEvaluationButton
import com.example.ui.components.PpbeaSelectionModalDialog
import com.example.util.CnpjConsultationHelper
import com.example.util.CpfValidator
import com.example.util.WatermarkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private fun createEditTempCameraUri(context: Context): Uri? {
    return try {
        val photosDir = File(context.cacheDir, "photos").apply {
            if (!exists()) mkdirs()
        }
        val uniqueSuffix = UUID.randomUUID().toString().take(6)
        val photoFile = File(photosDir, "edit_capture_${System.currentTimeMillis()}_$uniqueSuffix.jpg")
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    } catch (_: Exception) {
        null
    }
}

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
        requiresOtherAgencySupport: Boolean,
        otherAgencyName: String,
        otherAgencyNotes: String,
        riskLevel: Int,
        safetyRiskAssessment: String,
        tutorName: String,
        tutorCpf: String,
        additionalTutors: String,
        auditorName: String,
        ppbeaAnswersData: String,
        photoPaths: List<String>,
        individualEvaluationsData: String
    ) -> Unit
) {
    var auditorName by remember { mutableStateOf(inspection.auditorName) }
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
    var requiresOtherAgencySupport by remember { mutableStateOf(inspection.requiresOtherAgencySupport) }
    var otherAgencyName by remember { mutableStateOf(inspection.otherAgencyName) }
    var otherAgencyNotes by remember { mutableStateOf(inspection.otherAgencyNotes) }
    var riskLevel by remember { mutableIntStateOf(inspection.riskLevel) }
    var safetyRiskAssessment by remember { mutableStateOf(inspection.safetyRiskAssessment) }
    val context = LocalContext.current
    val isDog = inspection.isDog

    // Gestão Dinâmica de Fotos (Anexar e Retirar Fotos durante Edição)
    var photoPaths by remember { mutableStateOf(inspection.allPhotoPaths) }
    var isProcessingPhoto by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            isProcessingPhoto = true
            coroutineScope.launch(Dispatchers.IO) {
                val watermarked = WatermarkUtils.applyWatermarkAndSave(
                    context = context,
                    inputUri = tempCameraUri!!,
                    latitude = inspection.latitude,
                    longitude = inspection.longitude,
                    administrativeRegion = ra.ifBlank { inspection.administrativeRegion },
                    protocol = inspection.protocolNumber,
                    captureTimeMillis = System.currentTimeMillis(),
                    exemplarIndex = photoPaths.size + 1
                )
                withContext(Dispatchers.Main) {
                    if (watermarked != null) {
                        photoPaths = photoPaths + watermarked.absolutePath
                        Toast.makeText(context, "Nova foto capturada e anexada!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Erro ao gravar marca d'água.", Toast.LENGTH_SHORT).show()
                    }
                    isProcessingPhoto = false
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isProcessingPhoto = true
            coroutineScope.launch(Dispatchers.IO) {
                val newlyAdded = mutableListOf<String>()
                val initialSize = photoPaths.size
                uris.forEachIndexed { idx, uri ->
                    val watermarked = WatermarkUtils.applyWatermarkAndSave(
                        context = context,
                        inputUri = uri,
                        latitude = inspection.latitude,
                        longitude = inspection.longitude,
                        administrativeRegion = ra.ifBlank { inspection.administrativeRegion },
                        protocol = inspection.protocolNumber,
                        captureTimeMillis = System.currentTimeMillis(),
                        exemplarIndex = initialSize + idx + 1
                    )
                    if (watermarked != null) {
                        newlyAdded.add(watermarked.absolutePath)
                    }
                }
                withContext(Dispatchers.Main) {
                    if (newlyAdded.isNotEmpty()) {
                        photoPaths = photoPaths + newlyAdded
                        Toast.makeText(context, "${newlyAdded.size} foto(s) anexada(s) com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                    isProcessingPhoto = false
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createEditTempCameraUri(context)
            if (uri != null) {
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Permissão de câmera necessária.", Toast.LENGTH_SHORT).show()
        }
    }

    // Estados para o Protocolo de Perícia em Bem-Estar Animal (PPBEA – Anexo D / Res. CFMV 1.236/2018)
    var individualEvaluations by remember(inspection.id, inspection.individualEvaluationsData) {
        mutableStateOf(
            if (inspection.individualEvaluations.isNotEmpty()) {
                inspection.individualEvaluations
            } else {
                val count = maxOf(1, inspection.horseCount)
                val tutorTag = when {
                    inspection.tutorName.isNotBlank() && inspection.tutorCpf.isNotBlank() -> "${inspection.tutorName} (${inspection.tutorCpf})"
                    inspection.tutorName.isNotBlank() -> inspection.tutorName
                    inspection.tutorCpf.isNotBlank() -> inspection.tutorCpf
                    else -> ""
                }
                val addTutors = inspection.allTutors.drop(1)
                (0 until count).map { idx ->
                    val tag = if (idx == 0) tutorTag else (addTutors.getOrNull(idx - 1)?.let {
                        if (it.cpf.isNotBlank()) "${it.name} (${it.cpf})".trim() else it.name
                    } ?: "")
                    IndividualAnimalEvaluation(
                        id = UUID.randomUUID().toString(),
                        species = inspection.species.ifBlank { "Cavalo" },
                        tagOrCpf = tag,
                        ppbeaAnswersData = if (idx == 0) inspection.ppbeaAnswersData else "",
                        notes = if (idx == 0) inspection.horseDescription else ""
                    )
                }
            }
        )
    }
    var showPpbeaSelectionModal by remember { mutableStateOf(false) }
    var showIndividualizedScreen by remember { mutableStateOf(false) }
    var showPpbeaEvaluationDialog by remember { mutableStateOf(false) }
    var answersMap by remember {
        mutableStateOf(inspection.ppbeaAnswersMap.toMutableMap())
    }
    var ppbeaDiagnosis by remember {
        mutableStateOf<PpbeaDiagnosis?>(
            if (inspection.ppbeaAnswersMap.isNotEmpty()) {
                PpbeaCriteriaRepository.evaluate(inspection.ppbeaAnswersMap)
            } else {
                PpbeaCriteriaRepository.evaluate(emptyMap())
            }
        )
    }
    var parecerRelatorioGeral by remember {
        mutableStateOf(
            horseDescription.ifBlank {
                ppbeaDiagnosis?.observacoes ?: ""
            }
        )
    }

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
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.95f),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFF4FAF6),
            tonalElevation = 6.dp,
            border = BorderStroke(1.2.dp, Color(0xFF6EE7B7))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Verde Floresta (#14532D) Conforme Imagem
                Surface(
                    color = Color(0xFF14532D),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Editar Fiscalização",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Levantamento: ${inspection.protocolNumber} • ${ra.ifBlank { inspection.administrativeRegion }}",
                                fontSize = 12.sp,
                                color = Color(0xFFA7F3D0)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                        }
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
                    // Identificação do Auditor Fiscal e Espécie
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Auditor Fiscal Responsável",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${inspection.speciesEmoji} ${inspection.speciesDisplay}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            AppOutlinedTextField(
                                value = auditorName,
                                onValueChange = { auditorName = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Nome / Matrícula do Auditor") },
                                placeholder = { Text("Ex: Auditor Silva • Matr. 123.456-X") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // 0. Registro Fotográfico Georreferenciado (Anexar / Retirar Fotos)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = Color(0xFF14532D),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Fotos da Fiscalização",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Surface(
                                    color = if (photoPaths.isNotEmpty()) Color(0xFFA7F3D0).copy(alpha = 0.85f) else Color(0xFFFEE2E2),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = if (photoPaths.isEmpty()) "Sem Fotos" else "${photoPaths.size} foto(s)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (photoPaths.isNotEmpty()) Color(0xFF022C22) else Color(0xFF991B1B),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (photoPaths.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        .padding(vertical = 18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Nenhuma foto anexada a este relatório.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Clique nos botões abaixo para tirar nova foto ou selecionar da galeria.",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            } else {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    itemsIndexed(photoPaths) { index, path ->
                                        Box(
                                            modifier = Modifier
                                                .width(135.dp)
                                                .height(145.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF0F172A))
                                        ) {
                                            val file = File(path)
                                            if (file.exists()) {
                                                AsyncImage(
                                                    model = file,
                                                    contentDescription = "Foto ${index + 1}",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("Arquivo ausente", color = Color.White, fontSize = 10.sp)
                                                }
                                            }

                                            // Badge com número da foto
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.65f),
                                                shape = RoundedCornerShape(bottomEnd = 6.dp),
                                                modifier = Modifier.align(Alignment.TopStart)
                                            ) {
                                                Text(
                                                    text = if (index == 0) "Foto 1 (Principal)" else "Foto ${index + 1}",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            // Botão Retirar Foto (Lixeira vermelha)
                                            Surface(
                                                onClick = {
                                                    val updated = photoPaths.toMutableList()
                                                    updated.removeAt(index)
                                                    photoPaths = updated
                                                    Toast.makeText(context, "Foto ${index + 1} retirada.", Toast.LENGTH_SHORT).show()
                                                },
                                                color = Color(0xFFDC2626),
                                                shape = CircleShape,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(28.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Retirar foto",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (isProcessingPhoto) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF14532D)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Processando e aplicando marca d'água oficial...",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF14532D),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Botões de ação para anexar fotos
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                        if (hasCam) {
                                            val uri = createEditTempCameraUri(context)
                                            if (uri != null) {
                                                tempCameraUri = uri
                                                cameraLauncher.launch(uri)
                                            }
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !isProcessingPhoto
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tirar Foto", fontSize = 11.5.sp, maxLines = 1)
                                }

                                Button(
                                    onClick = {
                                        galleryLauncher.launch("image/*")
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14532D)),
                                    enabled = !isProcessingPhoto
                                ) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Anexar Fotos", fontSize = 11.5.sp, color = Color.White, maxLines = 1)
                                }
                            }
                        }
                    }

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
                            AppOutlinedTextField(
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
                                AppOutlinedTextField(
                                    value = conjunto,
                                    onValueChange = { conjunto = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Conjunto (Conj.)") },
                                    placeholder = { Text("Ex: Conj. A...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                AppOutlinedTextField(
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
                                text = if (isDog) "2. Quantidade de Cães *" else "2. Quantidade de Cavalos *",
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
                                        text = if (isDog) "Defina a quantidade de caninos" else "Defina a quantidade de equinos",
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
                        }
                    }

                    // BLOCO ÚNICO CONTÍNUO: PROTOCOLO PPBEA & RELATÓRIO GERAL (CONFORME IMAGEM DO USUÁRIO)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, Color(0xFF6EE7B7))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // (a) Faixa #14532D: PROTOCOLO PPBEA & RELATÓRIO GERAL + pílula Res. CFMV 1.236/2018
                            Surface(
                                color = Color(0xFF14532D),
                                modifier = Modifier.fillMaxWidth()
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
                                            imageVector = Icons.Default.Assignment,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "PROTOCOLO PPBEA &\nRELATÓRIO GERAL",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            lineHeight = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                    Surface(
                                        color = Color(0xFFA7F3D0),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = "Res. CFMV 1.236/2018",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            // 1. BOTÃO PRINCIPAL: "Abrir Avaliação" Conforme Mockup Abrir_avalia__o_9fb09810.png
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF0FDF4))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                PpbeaOpenEvaluationButton(
                                    onClick = { showPpbeaSelectionModal = true },
                                    hasDiagnosis = ppbeaDiagnosis != null,
                                    individualCount = individualEvaluations.size
                                )
                            }

                            // Resumo das Avaliações Individualizadas (se houver animais cadastrados)
                            if (individualEvaluations.isNotEmpty()) {
                                Surface(
                                    color = Color(0xFFF3EDF7),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.2.dp, Color(0xFFD0BCFF)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .clickable { showIndividualizedScreen = true }
                                        .testTag("summary_individualized_evaluations_edit")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text("📋", fontSize = 15.sp)
                                                Text(
                                                    text = "Avaliação Individualizada (${individualEvaluations.size} animais)",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF4F378B)
                                                )
                                            }
                                            Surface(
                                                color = Color(0xFFEADBFC),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Ver / Editar",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF65558F),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        individualEvaluations.forEachIndexed { idx, item ->
                                            val statusText = if (item.isEvaluated) {
                                                if (item.ppbeaDiagnosis?.ocorrenciaMausTratos == com.example.data.model.EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS)
                                                    "⚠️ Configura Maus-Tratos"
                                                else
                                                    "✅ ${item.ppbeaDiagnosis?.grauBemEstar?.label ?: "Adequado"}"
                                            } else {
                                                "⏳ PPBEA Pendente"
                                            }
                                            val idOrResp = buildString {
                                                if (item.tagOrCpf.isNotBlank()) append(item.tagOrCpf)
                                                if (item.tutorQualification.isNotBlank()) {
                                                    if (isNotEmpty()) append(" • Resp: ")
                                                    append(item.tutorQualification)
                                                }
                                                if (isEmpty()) append("Sem identificação")
                                            }
                                            Text(
                                                text = "• #${idx + 1} (${item.species}): $idOrResp — $statusText",
                                                fontSize = 11.sp,
                                                color = Color(0xFF49454F),
                                                modifier = Modifier.padding(vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFFE5E7EB))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Descrição & Parecer do Relatório Geral
                                Text(
                                    text = "Descrição & Parecer do Relatório Geral",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF374151)
                                )

                                AppOutlinedTextField(
                                    value = parecerRelatorioGeral,
                                    onValueChange = {
                                        parecerRelatorioGeral = it
                                        horseDescription = it
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    maxLines = 6,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    // 3. Providências Operacionais
                    Column {
                        val provLabel = if (isDog)
                            "Apoio Policial (PMDF / BPMA)"
                        else
                            "Apreensão pela SEAGRI & Apoio Policial (PMDF)"
                        Text(
                            text = provLabel,
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
                                if (!isDog) {
                                    // SEAGRI (Apenas para Equinos)
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
                                        AppOutlinedTextField(
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
                                }

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
                                    AppOutlinedTextField(
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

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Outro Órgão / Providências
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            text = "Requerer outro órgão ou outras providências de apreensão?",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (requiresOtherAgencySupport) Color(0xFF6A1B9A) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Apoio aos auditores fiscais ou requisição (DEMA/PCDF, Zoonoses/Dival, DF Legal, IBAMA, etc.)",
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
                                                .clickable { requiresOtherAgencySupport = true },
                                            color = if (requiresOtherAgencySupport) Color(0xFF6A1B9A) else Color.Transparent
                                        ) {
                                            Text(
                                                text = "SIM",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (requiresOtherAgencySupport) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                                                .clickable { requiresOtherAgencySupport = false },
                                            color = if (!requiresOtherAgencySupport) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
                                        ) {
                                            Text(
                                                text = "NÃO",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (!requiresOtherAgencySupport) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                if (requiresOtherAgencySupport) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val agencySuggestions = listOf(
                                        "DEMA / PCDF",
                                        "Zoonoses / Dival",
                                        "DF Legal",
                                        "IBAMA",
                                        "CBMDF (Bombeiros)"
                                    )
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(agencySuggestions) { agency ->
                                            val isSelected = otherAgencyName.equals(agency, ignoreCase = true)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { otherAgencyName = agency },
                                                label = { Text(agency, fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFFF3E5F5),
                                                    selectedLabelColor = Color(0xFF6A1B9A)
                                                )
                                            )
                                        }
                                    }

                                    AppOutlinedTextField(
                                        value = otherAgencyName,
                                        onValueChange = { otherAgencyName = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Ex: DEMA / PCDF, Vigilância Ambiental (Zoonoses), DF Legal...") },
                                        label = { Text("Órgão ou Entidade Requisitada") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    AppOutlinedTextField(
                                        value = otherAgencyNotes,
                                        onValueChange = { otherAgencyNotes = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Ex: Requerer acolhimento dos animais pela Zoonoses ou inquérito policial...") },
                                        label = { Text("Providências Requeridas / Apoio e Apreensão") },
                                        minLines = 2,
                                        maxLines = 4,
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
                        AppOutlinedTextField(
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

                                    AppOutlinedTextField(
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
                                        AppOutlinedTextField(
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
                            val ppbeaAnswersToSave = PpbeaCriteriaRepository.mapToAnswersString(answersMap)
                            val indivToSave = IndividualAnimalSerializer.serializeList(individualEvaluations)
                            onSave(
                                notes, ra, quadra, conjunto, numero, horseDescription,
                                horseCount, mistreatedHorseCount, alertIndicatorsToSave,
                                adequateIndicatorsToSave, requiresSeagriApprehension, seagriNotes,
                                requiresPmdfSupport, pmdfNotes, requiresOtherAgencySupport,
                                otherAgencyName, otherAgencyNotes, riskLevel, safetyRiskAssessment,
                                prim.name, prim.cpf, add, auditorName, ppbeaAnswersToSave,
                                photoPaths, indivToSave
                            )
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

    if (showPpbeaSelectionModal) {
        PpbeaSelectionModalDialog(
            onDismiss = { showPpbeaSelectionModal = false },
            onSelectIndividualized = {
                showPpbeaSelectionModal = false
                showIndividualizedScreen = true
            },
            onSelectSampling = {
                showPpbeaSelectionModal = false
                showPpbeaEvaluationDialog = true
            }
        )
    }

    if (showIndividualizedScreen) {
        IndividualizedPpbeaScreen(
            initialEvaluations = individualEvaluations,
            defaultSpecies = inspection.species,
            availableTutors = tutorsList,
            onSaveAndClose = { updatedList: List<IndividualAnimalEvaluation> ->
                individualEvaluations = updatedList
                if (updatedList.isNotEmpty()) {
                    horseCount = updatedList.size
                    val mistreatedCount = updatedList.count {
                        it.ppbeaDiagnosis?.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS
                    }
                    mistreatedHorseCount = mistreatedCount

                    val evaluated = updatedList.mapNotNull { it.ppbeaDiagnosis }
                    if (evaluated.isNotEmpty()) {
                        val hasMausTratos = evaluated.any { it.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS }
                        val hasRecomendacoes = evaluated.any { it.ocorrenciaMausTratos == EnquadramentoMausTratos.PRECISA_RECOMENDACOES_RETORNO }
                        val worstEnquadramento = when {
                            hasMausTratos -> EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS
                            hasRecomendacoes -> EnquadramentoMausTratos.PRECISA_RECOMENDACOES_RETORNO
                            else -> EnquadramentoMausTratos.NAO_CONFIGURA_MAUS_TRATOS
                        }
                        val minGrau = evaluated.minByOrNull { it.grauBemEstar.ordinal }?.grauBemEstar ?: GrauBemEstar.MUITO_ALTO
                        ppbeaDiagnosis = (ppbeaDiagnosis ?: evaluated.first()).copy(
                            ocorrenciaMausTratos = worstEnquadramento,
                            grauBemEstar = minGrau,
                            isEvaluated = true
                        )
                    }

                    // Sincroniza indicadores de alerta de maus-tratos
                    val aggregatedAlerts = mutableListOf<String>()
                    updatedList.forEach { animal ->
                        val ans = animal.ppbeaAnswersMap
                        PpbeaCriteriaRepository.criteria.forEach { crit ->
                            if (ans[crit.id] == crit.simIsRuim) {
                                val tag = "[${crit.domain.title}] ${crit.question.replace("?", "")}"
                                if (!aggregatedAlerts.contains(tag)) aggregatedAlerts.add(tag)
                            }
                        }
                    }
                    mistreatmentIndicators = (mistreatmentIndicators + aggregatedAlerts).distinct()
                }
                showIndividualizedScreen = false
            },
            onCancel = { showIndividualizedScreen = false }
        )
    }

    if (showPpbeaEvaluationDialog) {
        PpbeaEvaluationDialog(
            initialDiagnosis = ppbeaDiagnosis,
            onDismiss = { showPpbeaEvaluationDialog = false },
            onSaveDiagnosis = { diagnosis ->
                ppbeaDiagnosis = diagnosis
                answersMap = diagnosis.answersMap.toMutableMap()

                val newMistreatments = mistreatmentIndicators.toMutableList()
                val newAdequates = adequateIndicators.toMutableList()

                diagnosis.answersMap.forEach { (criterionId, isYes) ->
                    val criterion = PpbeaCriteriaRepository.criteria.find { it.id == criterionId }
                    if (criterion != null) {
                        val isRuim = if (criterion.simIsRuim) isYes else !isYes
                        val tag = "[${criterion.domain.title}] ${criterion.question.replace("?", "")}"
                        if (isRuim) {
                            if (!newMistreatments.contains(tag)) newMistreatments.add(tag)
                            newAdequates.remove(tag)
                        } else {
                            if (!newAdequates.contains(tag)) newAdequates.add(tag)
                            newMistreatments.remove(tag)
                        }
                    }
                }

                mistreatmentIndicators = newMistreatments
                adequateIndicators = newAdequates

                if (diagnosis.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS && mistreatedHorseCount == 0) {
                    mistreatedHorseCount = horseCount
                } else if (diagnosis.ocorrenciaMausTratos == EnquadramentoMausTratos.NAO_CONFIGURA_MAUS_TRATOS && newMistreatments.isEmpty()) {
                    mistreatedHorseCount = 0
                }

                showPpbeaEvaluationDialog = false
            }
        )
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

@Composable
private fun DomainStatusChip(
    label: String,
    isAdequate: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isAdequate) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
        border = BorderStroke(1.dp, if (isAdequate) Color(0xFF86EFAC) else Color(0xFFFCA5A5))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAdequate) Color(0xFF166534) else Color(0xFF991B1B)
            )
            Surface(
                shape = CircleShape,
                color = if (isAdequate) Color(0xFF16A34A) else Color(0xFFDC2626)
            ) {
                Text(
                    text = if (isAdequate) "Adequado" else "Inadequado",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}
