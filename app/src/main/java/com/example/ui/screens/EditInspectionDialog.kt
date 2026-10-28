package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.AnimalSpecies
import com.example.data.model.EnquadramentoMausTratos
import com.example.data.model.GrauBemEstar
import com.example.data.model.HorseInspection
import com.example.data.model.IndividualAnimalEvaluation
import com.example.data.model.IndividualAnimalSerializer
import com.example.data.model.ParecerDomain
import com.example.data.model.PpbeaCriteriaRepository
import com.example.data.model.PpbeaDiagnosis
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
private fun SectionHeader(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(28.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val riskLevel by remember { mutableIntStateOf(inspection.riskLevel) }
    val safetyRiskAssessment by remember { mutableStateOf(inspection.safetyRiskAssessment) }
    val context = LocalContext.current
    val isDog = inspection.isDog

    // Gestão Dinâmica de Fotos (Anexar e Retirar Fotos durante Edição)
    var photoPaths by remember { mutableStateOf(inspection.allPhotoPaths) }
    var selectedPhotoIndex by remember { mutableIntStateOf(0) }
    var previewPhotoPath by remember { mutableStateOf<String?>(null) }
    var isProcessingPhoto by remember { mutableStateOf(false) }
    var showLiveCamera by remember { mutableStateOf(false) }
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
                        selectedPhotoIndex = photoPaths.lastIndex
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
                        selectedPhotoIndex = photoPaths.lastIndex
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
            showLiveCamera = true
        } else {
            Toast.makeText(context, "Permissão de câmera necessária.", Toast.LENGTH_SHORT).show()
        }
    }

    // Estados para o Protocolo de Perícia em Bem-Estar Animal (PPBEA)
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
    var showAnatomyDialog by remember { mutableStateOf(false) }
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

    var tutorsList by remember {
        mutableStateOf(
            if (inspection.allTutors.isNotEmpty()) inspection.allTutors
            else listOf(TutorEntry(inspection.tutorName, inspection.tutorCpf))
        )
    }

    var showRaSelector by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Editar Fiscalização",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Levantamento: ${inspection.protocolNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancelar", fontWeight = FontWeight.SemiBold)
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
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("save_changes_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salvar Alterações", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 0. IDENTIFICAÇÃO DO AUDITOR FISCAL & SELEÇÃO DE ESPÉCIE
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auditor_species_header_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Identificação do Auditor Fiscal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Auditor Fiscal Responsável",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = auditorName.ifBlank { "Não informado" },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Ação Fiscal",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        AppOutlinedTextField(
                            value = auditorName,
                            onValueChange = { auditorName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auditor_input_field"),
                            placeholder = { Text("Nome do auditor fiscal ou matrícula...") },
                            label = { Text("Nome / Matrícula do Auditor") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Exibição da Espécie sob Fiscalização
                        Text(
                            text = "ESPÉCIE DO LEVANTAMENTO FISCAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFF16A34A),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("active_selected_species_card"),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = inspection.speciesEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = inspection.speciesDisplay,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1B5E20)
                                        )
                                        Text(
                                            text = "${inspection.species} • Registrado no levantamento",
                                            fontSize = 11.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF16A34A).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Ativa",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 1. SECTION: FOTOGRAFIAS DA FISCALIZAÇÃO COM MARCA D'ÁGUA
                SectionHeader(
                    title = "1. Fotografias & Marca D'Água Automática",
                    description = "Tire ou anexe múltiplas fotos. Cada imagem receberá marca d'água permanente com GPS, data/hora e RA."
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("photo_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        if (isProcessingPhoto) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Aplicando marca d'água fiscalizatória permanente...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Gravando coordenadas GPS, RA e data/hora nas imagens",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (photoPaths.isNotEmpty()) {
                            val activeIndex = selectedPhotoIndex.coerceIn(0, photoPaths.lastIndex)
                            val activePhotoPath = photoPaths[activeIndex]
                            val activeFile = File(activePhotoPath)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Fotografias Registradas (${photoPaths.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "📸 Foto ${activeIndex + 1} de ${photoPaths.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Main Preview Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(230.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black)
                            ) {
                                AsyncImage(
                                    model = activeFile,
                                    contentDescription = "Foto ${activeIndex + 1} com marca d'água",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { previewPhotoPath = activePhotoPath },
                                    contentScale = ContentScale.Fit
                                )

                                // Top-right action icons (Fullscreen & Delete)
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        color = Color(0xAA000000),
                                        shape = CircleShape,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        IconButton(
                                            onClick = { previewPhotoPath = activePhotoPath },
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Fullscreen,
                                                contentDescription = "Expandir imagem",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        color = Color(0xAA000000),
                                        shape = CircleShape,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                photoPaths = photoPaths.filterIndexed { i, _ -> i != activeIndex }
                                                if (selectedPhotoIndex >= photoPaths.size - 1) {
                                                    selectedPhotoIndex = maxOf(0, photoPaths.size - 2)
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remover foto",
                                                tint = Color(0xFFFF8A80),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Thumbnail strip if there are multiple photos
                            if (photoPaths.size > 1) {
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    itemsIndexed(photoPaths) { idx, path ->
                                        val isSelected = idx == activeIndex
                                        Box(
                                            modifier = Modifier
                                                .size(62.dp)
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
                                                    text = "Foto ${idx + 1}",
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

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Buttons to add more photos
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val hasCam = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasCam) {
                                            showLiveCamera = true
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+ Tirar Foto", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+ Anexar Fotos", fontSize = 12.sp)
                                }
                            }
                        } else if (!isProcessingPhoto) {
                            // Empty state (0 photos)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(64.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Nenhuma foto anexada",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Tire ou anexe fotos para documentar a ação fiscalizatória. Todas as imagens recebem marca d'água permanente com GPS e RA.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val hasCam = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (hasCam) {
                                                showLiveCamera = true
                                            } else {
                                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        },
                                        modifier = Modifier.weight(1f).testTag("take_photo_button")
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tirar Foto")
                                    }

                                    OutlinedButton(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        modifier = Modifier.weight(1f).testTag("pick_gallery_button")
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Anexar Fotos")
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. SECTION: ENDEREÇO DA AÇÃO FISCAL & LOCALIZAÇÃO
                SectionHeader(
                    title = "2. Endereço da Ação Fiscal & Localização (DF)",
                    description = "Informe o endereço completo (RA, Quadra, Conjunto e Número) e confirme as coordenadas de GPS."
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Endereço da Ocorrência",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Região Administrativa (RA)
                        Text(
                            text = "Região Administrativa (RA) *",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showRaSelector = true }
                                .testTag("ra_selector"),
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
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = ra.ifBlank { "Selecione a Região Administrativa" },
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Alterar RA",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quadra
                        AppOutlinedTextField(
                            value = quadra,
                            onValueChange = { quadra = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("address_quadra_field"),
                            label = { Text("Quadra (Q.)") },
                            placeholder = { Text("Ex: Quadra 04, QNM 18, SQS 308, Chácara 12...") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Conjunto e Número (lado a lado)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppOutlinedTextField(
                                value = conjunto,
                                onValueChange = { conjunto = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("address_conjunto_field"),
                                label = { Text("Conjunto (Conj.)") },
                                placeholder = { Text("Ex: Conj. A, B...") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            AppOutlinedTextField(
                                value = numero,
                                onValueChange = { numero = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("address_numero_field"),
                                label = { Text("Número / Lote (Nº)") },
                                placeholder = { Text("Ex: Lote 05, Casa 02, S/N...") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // GPS Coordinates Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Coordenadas GPS Registradas:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val coordsFormatted = String.format(
                                    java.util.Locale.US,
                                    "Lat: %.6f° | Lon: %.6f° (SIRGAS 2000 / WGS 84)",
                                    inspection.latitude,
                                    inspection.longitude
                                )
                                Text(
                                    text = coordsFormatted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // 3. SECTION: QUANTIDADE DE ANIMAIS & AVALIAÇÃO PPBEA
                SectionHeader(
                    title = if (isDog) "3. Quantidade de Cães & Avaliação PPBEA" else "3. Quantidade de Animais & Avaliação PPBEA",
                    description = "Contagem dos animais e perícia de bem-estar animal com botões switch (PROTOCOLO PPBEA)."
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = if (isDog) "Total de Cães" else "Total de Animais",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isDog) "Contagem para contenção e acolhimento" else "Contagem para contenção e transporte",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Stepper controller
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (horseCount > 1) {
                                            horseCount -= 1
                                            if (individualEvaluations.isNotEmpty()) {
                                                mistreatedHorseCount = individualEvaluations.count { it.ppbeaDiagnosis?.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS }
                                            } else if (ppbeaDiagnosis?.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS) {
                                                mistreatedHorseCount = horseCount
                                            } else {
                                                mistreatedHorseCount = 0
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                        .testTag("decrease_horses_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Diminuir",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .width(54.dp)
                                        .height(44.dp)
                                        .testTag("horse_count_display")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = horseCount.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        horseCount += 1
                                        if (individualEvaluations.isNotEmpty()) {
                                            mistreatedHorseCount = individualEvaluations.count { it.ppbeaDiagnosis?.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS }
                                        } else if (ppbeaDiagnosis?.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS) {
                                            mistreatedHorseCount = horseCount
                                        } else {
                                            mistreatedHorseCount = 0
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                        .testTag("increase_horses_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Aumentar",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick-selection presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1, 2, 3, 5, 10).forEach { count ->
                                val isSelected = horseCount == count
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            horseCount = count
                                            if (individualEvaluations.isNotEmpty()) {
                                                mistreatedHorseCount = individualEvaluations.count { it.ppbeaDiagnosis?.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS }
                                            } else if (ppbeaDiagnosis?.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS) {
                                                mistreatedHorseCount = count
                                            } else {
                                                mistreatedHorseCount = 0
                                            }
                                        }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = if (count == 10) "10+" else "$count",
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // PROTOCOLO PPBEA CARD
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = if (mistreatmentIndicators.isNotEmpty())
                                Color(0xFFFFF5F5)
                            else if (adequateIndicators.isNotEmpty())
                                Color(0xFFF1F8F4)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp,
                                if (mistreatmentIndicators.isNotEmpty()) Color(0xFFEF9A9A)
                                else if (adequateIndicators.isNotEmpty()) Color(0xFFA5D6A7)
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (mistreatmentIndicators.isNotEmpty()) Icons.Default.Warning else Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = if (mistreatmentIndicators.isNotEmpty()) Color(0xFFC62828) else Color(0xFF2E7D32),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "PROTOCOLO PPBEA",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = if (mistreatmentIndicators.isNotEmpty()) Color(0xFFC62828) else Color(0xFF2E7D32)
                                        )
                                    }

                                    Surface(
                                        color = if (mistreatmentIndicators.isNotEmpty()) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${mistreatmentIndicators.size} Alertas • ${adequateIndicators.size} Adequados",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (mistreatmentIndicators.isNotEmpty()) Color(0xFFC62828) else Color(0xFF2E7D32),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // 4 INDICADORES OFICIAIS DO PROTOCOLO PPBEA
                                Text(
                                    text = "Os 4 Indicadores de Bem-Estar Animal (Situação Boa vs Ruim):",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 1. Nutricional
                                    val nutStatus = ppbeaDiagnosis?.nutricionalParecer
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (nutStatus) {
                                            ParecerDomain.ADEQUADO -> Color(0xFFE8F5E9)
                                            ParecerDomain.REGULAR -> Color(0xFFFFF8E1)
                                            ParecerDomain.INADEQUADO -> Color(0xFFFFEBEE)
                                            null -> MaterialTheme.colorScheme.surface
                                        },
                                        border = BorderStroke(1.dp, when (nutStatus) {
                                            ParecerDomain.ADEQUADO -> Color(0xFF81C784)
                                            ParecerDomain.REGULAR -> Color(0xFFFFD54F)
                                            ParecerDomain.INADEQUADO -> Color(0xFFEF9A9A)
                                            null -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        })
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("🥣", fontSize = 15.sp)
                                            Text("Nutrição", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = nutStatus?.label ?: "Pendente",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = when (nutStatus) {
                                                    ParecerDomain.ADEQUADO -> Color(0xFF2E7D32)
                                                    ParecerDomain.REGULAR -> Color(0xFFF57C00)
                                                    ParecerDomain.INADEQUADO -> Color(0xFFC62828)
                                                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }

                                    // 2. Conforto
                                    val confStatus = ppbeaDiagnosis?.confortoParecer
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (confStatus) {
                                            ParecerDomain.ADEQUADO -> Color(0xFFE8F5E9)
                                            ParecerDomain.REGULAR -> Color(0xFFFFF8E1)
                                            ParecerDomain.INADEQUADO -> Color(0xFFFFEBEE)
                                            null -> MaterialTheme.colorScheme.surface
                                        },
                                        border = BorderStroke(1.dp, when (confStatus) {
                                            ParecerDomain.ADEQUADO -> Color(0xFF81C784)
                                            ParecerDomain.REGULAR -> Color(0xFFFFD54F)
                                            ParecerDomain.INADEQUADO -> Color(0xFFEF9A9A)
                                            null -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        })
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("🏠", fontSize = 15.sp)
                                            Text("Conforto", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = confStatus?.label ?: "Pendente",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = when (confStatus) {
                                                    ParecerDomain.ADEQUADO -> Color(0xFF2E7D32)
                                                    ParecerDomain.REGULAR -> Color(0xFFF57C00)
                                                    ParecerDomain.INADEQUADO -> Color(0xFFC62828)
                                                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }

                                    // 3. Saúde
                                    val saudeStatus = ppbeaDiagnosis?.saudeParecer
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (saudeStatus) {
                                            ParecerDomain.ADEQUADO -> Color(0xFFE8F5E9)
                                            ParecerDomain.REGULAR -> Color(0xFFFFF8E1)
                                            ParecerDomain.INADEQUADO -> Color(0xFFFFEBEE)
                                            null -> MaterialTheme.colorScheme.surface
                                        },
                                        border = BorderStroke(1.dp, when (saudeStatus) {
                                            ParecerDomain.ADEQUADO -> Color(0xFF81C784)
                                            ParecerDomain.REGULAR -> Color(0xFFFFD54F)
                                            ParecerDomain.INADEQUADO -> Color(0xFFEF9A9A)
                                            null -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        })
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("🩺", fontSize = 15.sp)
                                            Text("Saúde", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = saudeStatus?.label ?: "Pendente",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = when (saudeStatus) {
                                                    ParecerDomain.ADEQUADO -> Color(0xFF2E7D32)
                                                    ParecerDomain.REGULAR -> Color(0xFFF57C00)
                                                    ParecerDomain.INADEQUADO -> Color(0xFFC62828)
                                                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }

                                    // 4. Comportamental
                                    val compStatus = ppbeaDiagnosis?.comportamentalParecer
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (compStatus) {
                                            ParecerDomain.ADEQUADO -> Color(0xFFE8F5E9)
                                            ParecerDomain.REGULAR -> Color(0xFFFFF8E1)
                                            ParecerDomain.INADEQUADO -> Color(0xFFFFEBEE)
                                            null -> MaterialTheme.colorScheme.surface
                                        },
                                        border = BorderStroke(1.dp, when (compStatus) {
                                            ParecerDomain.ADEQUADO -> Color(0xFF81C784)
                                            ParecerDomain.REGULAR -> Color(0xFFFFD54F)
                                            ParecerDomain.INADEQUADO -> Color(0xFFEF9A9A)
                                            null -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        })
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("🧠", fontSize = 15.sp)
                                            Text("Comport.", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = compStatus?.label ?: "Pendente",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = when (compStatus) {
                                                    ParecerDomain.ADEQUADO -> Color(0xFF2E7D32)
                                                    ParecerDomain.REGULAR -> Color(0xFFF57C00)
                                                    ParecerDomain.INADEQUADO -> Color(0xFFC62828)
                                                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // BOTÃO PRINCIPAL: "Abrir Avaliação (Editar)"
                                PpbeaOpenEvaluationButton(
                                    onClick = { showPpbeaSelectionModal = true },
                                    hasDiagnosis = ppbeaDiagnosis != null,
                                    individualCount = individualEvaluations.size
                                )

                                // Resumo das Avaliações Individualizadas (se houver animais cadastrados)
                                if (individualEvaluations.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        color = Color(0xFFF3EDF7),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.2.dp, Color(0xFFD0BCFF)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showIndividualizedScreen = true }
                                            .testTag("summary_individualized_evaluations")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
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
                                                        fontSize = 13.5.sp,
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

                                            Spacer(modifier = Modifier.height(8.dp))

                                            individualEvaluations.forEachIndexed { idx, item ->
                                                val statusText = if (item.isEvaluated) {
                                                    if (item.ppbeaDiagnosis?.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS)
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
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFF49454F),
                                                    modifier = Modifier.padding(vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Card de Diagnóstico Geral Realizado
                                if (ppbeaDiagnosis != null && ppbeaDiagnosis?.isEvaluated == true) {
                                    val diag = ppbeaDiagnosis!!
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = when (diag.grauBemEstar) {
                                            GrauBemEstar.MUITO_BAIXO, GrauBemEstar.BAIXO -> Color(0xFFFFEBEE)
                                            GrauBemEstar.REGULAR -> Color(0xFFFFF8E1)
                                            GrauBemEstar.ALTO, GrauBemEstar.MUITO_ALTO -> Color(0xFFE8F5E9)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.2.dp, Color(diag.grauBemEstar.colorHex)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("card_ppbea_diagnosis_summary")
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = "⚖️", fontSize = 14.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Diagnóstico: ${diag.grauBemEstar.label.uppercase()}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color(diag.grauBemEstar.colorHex)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = diag.ocorrenciaMausTratos.label,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(diag.ocorrenciaMausTratos.colorHex)
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Pareceres: Nutrição [${diag.nutricionalParecer.label}] • Conforto [${diag.confortoParecer.label}] • Saúde [${diag.saudeParecer.label}] • Comportamento [${diag.comportamentalParecer.label}]",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Botão "✓ Aplicar Resultado ao Relatório Geral"
                                            Button(
                                                onClick = {
                                                    val inadequacoes = mutableListOf<String>()
                                                    if (diag.nutricionalParecer != ParecerDomain.ADEQUADO) inadequacoes.add("Nutrição")
                                                    if (diag.confortoParecer != ParecerDomain.ADEQUADO) inadequacoes.add("Conforto")
                                                    if (diag.saudeParecer != ParecerDomain.ADEQUADO) inadequacoes.add("Saúde")
                                                    if (diag.comportamentalParecer != ParecerDomain.ADEQUADO) inadequacoes.add("Comportamento")

                                                    val textoSintese = buildString {
                                                        append("Protocolo PPBEA (Res. CFMV nº 1.236/2018). ")
                                                        append("Constatado Grau de Bem-Estar ${diag.grauBemEstar.label.uppercase()}. ")
                                                        if (diag.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS) {
                                                            append("Restam configurados indícios técnicos de MAUS-TRATOS")
                                                            if (inadequacoes.isNotEmpty()) {
                                                                append(" (inadequações em: ${inadequacoes.distinct().joinToString(", ")}).")
                                                            } else {
                                                                append(".")
                                                            }
                                                        } else if (diag.ocorrenciaMausTratos == EnquadramentoMausTratos.PRECISA_RECOMENDACOES_RETORNO) {
                                                            append("Não configura crime imediato de maus-tratos, mas exige termo de orientação com retorno obrigatório.")
                                                        } else {
                                                            append("NÃO CONFIGURA MAUS-TRATOS. Condições adequadas de bem-estar constatadas.")
                                                        }
                                                    }
                                                    horseDescription = textoSintese
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14532D)),
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "✓ Aplicar Resultado ao Relatório Geral",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color.White
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        ppbeaDiagnosis = null
                                                        answersMap.clear()
                                                        mistreatmentIndicators = emptyList()
                                                        adequateIndicators = emptyList()
                                                        mistreatedHorseCount = 0
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Limpar Diagnóstico", fontSize = 11.sp, color = Color(0xFFC62828))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Descrição e Parecer do Relatório Geral",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Surface(
                                onClick = { showAnatomyDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.testTag("button_open_horse_atlas")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_owl),
                                        contentDescription = "Consultar Conhecimento (5 Domínios e Atlas)",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Conhecimento",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        AppOutlinedTextField(
                            value = horseDescription,
                            onValueChange = { horseDescription = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("horse_description_field"),
                            placeholder = {
                                Text("Descreva a situação física dos animais, parecer técnico geral e constatações periciais da fiscalização...")
                            },
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // 4. SECTION: PROVIDÊNCIAS OPERACIONAIS
                SectionHeader(
                    title = if (isDog) "4. Apoio Policial & Providências Interinstitucionais" else "4. Apreensão, Apoio Policial e Outros Órgãos",
                    description = if (isDog) "Defina se a ação fiscal precisará de apoio policial (BPMA / PMDF / DEMA) ou requisição de outro órgão para apoio e apreensão." else "Defina se a ação fiscal necessita de apreensão pela SEAGRI, apoio da Polícia Militar ou requisição de outro órgão público para apoio aos auditores."
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // 4.1 RECOLHIMENTO / APREENSÃO (Apenas para Equinos)
                        if (!isDog) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Ação fiscal necessita de apreensão por parte da SEAGRI?",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (requiresSeagriApprehension) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (requiresSeagriApprehension) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFFFFF3E0),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "SOLICITADO",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE65100),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Recolhimento e transporte de equinos para o curral público da SEAGRI-DF",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Toggle Buttons (SIM / NÃO)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                                            .clickable { requiresSeagriApprehension = true }
                                            .testTag("seagri_yes_button"),
                                        color = if (requiresSeagriApprehension) Color(0xFFE65100) else MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = "SIM",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (requiresSeagriApprehension) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                            .clickable { requiresSeagriApprehension = false }
                                            .testTag("seagri_no_button"),
                                        color = if (!requiresSeagriApprehension) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = "NÃO",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!requiresSeagriApprehension) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            if (requiresSeagriApprehension) {
                                Spacer(modifier = Modifier.height(10.dp))
                                AppOutlinedTextField(
                                    value = seagriNotes,
                                    onValueChange = { seagriNotes = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("seagri_notes_field"),
                                    placeholder = {
                                        Text("Ex: Necessidade de caminhão gaiola da SEAGRI para equinos soltos...")
                                    },
                                    label = { Text("Observações da Apreensão (SEAGRI)") },
                                    minLines = 2,
                                    maxLines = 3,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // 4.2 PMDF / BPMA SUPPORT
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isDog) "Apoio da Polícia Militar / BPMA ou DEMA?" else "Apoio da Polícia Militar?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (requiresPmdfSupport) Color(0xFF1565C0) else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (requiresPmdfSupport) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFFE3F2FD),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "PMDF / BPMA",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1565C0),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isDog) "Apoio da PMDF/BPMA para garantia de segurança e flagrante delito de maus-tratos (Lei Sansão)" else "Irá precisar de apoio da Polícia Militar (segurança da equipe, BPMA ou BPTran)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Toggle Buttons (SIM / NÃO)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                                        .clickable { requiresPmdfSupport = true }
                                        .testTag("pmdf_yes_button"),
                                        color = if (requiresPmdfSupport) Color(0xFF1565C0) else MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "SIM",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (requiresPmdfSupport) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                        .clickable { requiresPmdfSupport = false }
                                        .testTag("pmdf_no_button"),
                                    color = if (!requiresPmdfSupport) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "NÃO",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!requiresPmdfSupport) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        if (requiresPmdfSupport) {
                            Spacer(modifier = Modifier.height(10.dp))
                            AppOutlinedTextField(
                                value = pmdfNotes,
                                onValueChange = { pmdfNotes = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pmdf_notes_field"),
                                placeholder = {
                                    Text(
                                        if (isDog) "Ex: Risco de agressão no local, necessidade de apoio do BPMA para segurança e flagrante de maus-tratos..."
                                        else "Ex: Risco de hostilidade no local, apoio do BPMA para segurança e contenção de trânsito..."
                                    )
                                },
                                label = { Text("Justificativa do Apoio Policial (PMDF)") },
                                minLines = 2,
                                maxLines = 3,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // 4.3 REQUISIÇÃO DE OUTRO ÓRGÃO
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Requerer outro órgão ou outras providências de apreensão?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (requiresOtherAgencySupport) Color(0xFF6A1B9A) else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (requiresOtherAgencySupport) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFFF3E5F5),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "INTERINSTITUCIONAL",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF6A1B9A),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Apoio aos auditores fiscais ou requisição de providências (DEMA/PCDF, Zoonoses/Dival, DF Legal, IBAMA, CBMDF, etc.)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Toggle Buttons (SIM / NÃO)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                                        .clickable { requiresOtherAgencySupport = true }
                                        .testTag("other_agency_yes_button"),
                                    color = if (requiresOtherAgencySupport) Color(0xFF6A1B9A) else MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "SIM",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (requiresOtherAgencySupport) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                        .clickable { requiresOtherAgencySupport = false }
                                        .testTag("other_agency_no_button"),
                                    color = if (!requiresOtherAgencySupport) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "NÃO",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!requiresOtherAgencySupport) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        if (requiresOtherAgencySupport) {
                            Spacer(modifier = Modifier.height(10.dp))

                            // Sugestões rápidas
                            val agencySuggestions = listOf(
                                "DEMA / PCDF (Polícia Civil)",
                                "Zoonoses / Dival",
                                "DF Legal",
                                "IBAMA",
                                "CBMDF (Bombeiros)",
                                "Conselho Tutelar"
                            )
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("other_agency_name_field"),
                                placeholder = { Text("Ex: DEMA / PCDF, Vigilância Ambiental (Zoonoses), DF Legal...") },
                                label = { Text("Órgão ou Entidade Requisitada") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            AppOutlinedTextField(
                                value = otherAgencyNotes,
                                onValueChange = { otherAgencyNotes = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("other_agency_notes_field"),
                                placeholder = {
                                    Text("Ex: Requerer acolhimento e laudo veterinário pela Zoonoses, apoio do DF Legal para desobstrução, ou requisição de inquérito à DEMA/PCDF...")
                                },
                                label = { Text("Providências Requeridas e Justificativa de Apoio / Apreensão") },
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // 5. SECTION: OBSERVAÇÕES COMPLEMENTARES DE CAMPO
                SectionHeader(
                    title = "5. Observações Complementares de Campo (Opcional)",
                    description = "Fatos adicionais ou notas de campo que complementem o Parecer Geral (opcional)."
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Notas Adicionais de Campo:",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        AppOutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("image_notes_field"),
                            placeholder = {
                                Text("Caso haja observações complementares sobre a diligência fiscal que não constem no Parecer Geral...")
                            },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // 6. SECTION: IDENTIFICAÇÃO DOS TUTORES / RESPONSÁVEIS
                SectionHeader(
                    title = "6. Identificação dos Tutores & CPFs",
                    description = "Cadastre um ou múltiplos tutores e CPFs dos responsáveis identificados na ação fiscal."
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        tutorsList.forEachIndexed { index, tutor ->
                            val isDocValid = CpfValidator.isValid(tutor.cpf)
                            val isDocFilled = tutor.cpf.isNotBlank()
                            val isCnpj = CpfValidator.isCnpj(tutor.cpf)
                            val docType = CpfValidator.getDocumentType(tutor.cpf)

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (index == 0) "Tutor 1 (Principal)" else "Tutor ${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        if (tutorsList.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    tutorsList = tutorsList.filterIndexed { i, _ -> i != index }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remover tutor",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    AppOutlinedTextField(
                                        value = tutor.name,
                                        onValueChange = { newName ->
                                            tutorsList = tutorsList.mapIndexed { i, t ->
                                                if (i == index) t.copy(name = newName) else t
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("tutor_name_field_$index"),
                                        label = { Text("Nome do Tutor / Empresa ${index + 1}") },
                                        placeholder = { Text("Ex: João da Silva ou Agropecuária Ltda") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AppOutlinedTextField(
                                            value = tutor.cpf,
                                            onValueChange = { newCpf ->
                                                tutorsList = tutorsList.mapIndexed { i, t ->
                                                    if (i == index) t.copy(cpf = newCpf) else t
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("tutor_cpf_field_$index"),
                                            label = { Text("CPF / CNPJ do Tutor ${index + 1}") },
                                            placeholder = { Text("000.000.000-00 ou CNPJ") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            trailingIcon = {
                                                if (isDocFilled) {
                                                    if (isDocValid) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "$docType Válido",
                                                            tint = Color(0xFF2E7D32)
                                                        )
                                                    } else {
                                                        Icon(
                                                            imageVector = Icons.Default.Warning,
                                                            contentDescription = "Documento Incompleto",
                                                            tint = Color(0xFFE65100)
                                                        )
                                                    }
                                                }
                                            },
                                            supportingText = {
                                                if (isDocFilled && !isDocValid) {
                                                    Text(
                                                        text = if (isCnpj) "CNPJ com 14 dígitos." else "CPF (11 dígitos) ou CNPJ (14 dígitos).",
                                                        color = Color(0xFFE65100),
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        FilledTonalButton(
                                            onClick = {
                                                CnpjConsultationHelper.abrirConsultaReceitaFederal(context, tutor.cpf)
                                            },
                                            modifier = Modifier
                                                .height(56.dp)
                                                .testTag("consultar_cnpj_button_$index"),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
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
                            onClick = { tutorsList = tutorsList + TutorEntry("", "") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_tutor_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("+ Adicionar Outro Tutor")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
                individualEvaluations = emptyList()
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
            initialAnimalCount = horseCount,
            isDog = isDog,
            isSamplingMode = true,
            onDismiss = { showPpbeaEvaluationDialog = false },
            onSaveDiagnosis = { diagnosis, samplingCount ->
                horseCount = samplingCount
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

                if (diagnosis.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS) {
                    mistreatedHorseCount = samplingCount
                } else {
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

    if (showAnatomyDialog) {
        val currentSpecies = if (isDog) AnimalSpecies.DOG else AnimalSpecies.HORSE
        DfSafetyInfoDialog(
            species = currentSpecies,
            onDismiss = { showAnatomyDialog = false }
        )
    }

    if (previewPhotoPath != null) {
        Dialog(
            onDismissRequest = { previewPhotoPath = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = File(previewPhotoPath!!),
                    contentDescription = "Visualização expandida",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { previewPhotoPath = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color(0x88000000), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                }
            }
        }
    }

    if (showLiveCamera) {
        CameraCaptureScreen(
            initialLatitude = inspection.latitude,
            initialLongitude = inspection.longitude,
            administrativeRegion = ra.ifBlank { inspection.administrativeRegion },
            protocolNumber = inspection.protocolNumber,
            capturedPhotosCount = photoPaths.size,
            onPhotoCaptured = { uri, exactLat, exactLng, exactTime, dynamicRa, photoTag, orientationDegrees ->
                isProcessingPhoto = true
                coroutineScope.launch(Dispatchers.IO) {
                    val watermarked = WatermarkUtils.applyWatermarkAndSave(
                        context = context,
                        inputUri = uri,
                        latitude = exactLat ?: inspection.latitude,
                        longitude = exactLng ?: inspection.longitude,
                        administrativeRegion = dynamicRa.ifBlank { ra.ifBlank { inspection.administrativeRegion } },
                        protocol = inspection.protocolNumber,
                        captureTimeMillis = exactTime ?: System.currentTimeMillis(),
                        exemplarIndex = photoPaths.size + 1,
                        photoTag = photoTag,
                        deviceOrientationDegrees = orientationDegrees
                    )
                    withContext(Dispatchers.Main) {
                        if (watermarked != null) {
                            photoPaths = photoPaths + watermarked.absolutePath
                            selectedPhotoIndex = photoPaths.lastIndex
                            Toast.makeText(context, "Nova foto capturada e anexada!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Erro ao gravar marca d'água.", Toast.LENGTH_SHORT).show()
                        }
                        isProcessingPhoto = false
                    }
                }
            },
            onPickFromGallery = {
                galleryLauncher.launch("image/*")
            },
            onClose = {
                showLiveCamera = false
            }
        )
    }
}
