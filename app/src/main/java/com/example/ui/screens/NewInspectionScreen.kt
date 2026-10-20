package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.ui.components.AppOutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.DfConstants
import com.example.ui.components.AnimalSpecies
import com.example.ui.components.MistreatmentChecklistView
import com.example.ui.viewmodel.InspectionViewModel
import com.example.util.CnpjConsultationHelper
import com.example.util.CpfValidator
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInspectionScreen(
    viewModel: InspectionViewModel,
    onNavigateBack: () -> Unit,
    onSavedSuccessfully: (Long) -> Unit
) {
    val context = LocalContext.current
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var showRaDialog by rememberSaveable { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPhotoIndex by rememberSaveable { mutableIntStateOf(0) }
    var previewPhotoPath by remember { mutableStateOf<String?>(null) }
    var showLiveCamera by rememberSaveable { mutableStateOf(false) }
    var showAnatomyDialog by rememberSaveable { mutableStateOf(false) }
    var showPpbeaDialog by rememberSaveable { mutableStateOf(false) }
    var showPpbeaEvaluationDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    // Permission Launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.fetchCurrentLocation(true)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.addDraftPhotoUri(tempCameraUri!!)
        }
    }

    val multipleGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addDraftPhotoUris(uris)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showLiveCamera = true
        } else {
            Toast.makeText(context, "Permissão de câmera necessária para tirar fotos.", Toast.LENGTH_SHORT).show()
        }
    }

    // Request location on screen start
    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasFine) {
            viewModel.fetchCurrentLocation(true)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nova Fiscalização",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Levantamento: ${draft.protocolNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
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
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                                val currentAuditor = draft.auditorName.ifBlank { viewModel.auditorIdentification.ifBlank { "Não informado" } }
                                Text(
                                    text = currentAuditor,
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
                        value = draft.auditorName,
                        onValueChange = {
                            viewModel.updateDraftAuditorName(it)
                            viewModel.setAuditorId(it)
                        },
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

                    // Seletor de Espécie
                    Text(
                        text = "ESPÉCIE DO LEVANTAMENTO FISCAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isDog = draft.isDog
                        // Opção Cachorro
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.updateDraftSpecies("Cachorro") }
                                .border(
                                    width = if (isDog) 2.dp else 1.dp,
                                    color = if (isDog) Color(0xFF16A34A) else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .testTag("select_species_dog_button"),
                            color = if (isDog) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🐕", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Cachorro",
                                        fontWeight = if (isDog) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isDog) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Caninos",
                                        fontSize = 10.sp,
                                        color = if (isDog) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Opção Cavalo
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.updateDraftSpecies("Cavalo") }
                                .border(
                                    width = if (!isDog) 2.dp else 1.dp,
                                    color = if (!isDog) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .testTag("select_species_horse_button"),
                            color = if (!isDog) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🐎", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Cavalo",
                                        fontWeight = if (!isDog) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (!isDog) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Equinos",
                                        fontSize = 10.sp,
                                        color = if (!isDog) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
                    val photos = draft.allPhotoPaths

                    if (draft.isWatermarking) {
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

                    if (photos.isNotEmpty()) {
                        val activeIndex = selectedPhotoIndex.coerceIn(0, photos.lastIndex)
                        val activePhotoPath = photos[activeIndex]
                        val activeFile = File(activePhotoPath)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fotografias Registradas (${photos.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "📸 Foto ${activeIndex + 1} de ${photos.size}",
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
                                            viewModel.removeDraftPhoto(activePhotoPath)
                                            if (selectedPhotoIndex >= photos.size - 1) {
                                                selectedPhotoIndex = kotlin.math.max(0, photos.size - 2)
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
                        if (photos.size > 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(photos) { idx, path ->
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
                                onClick = { multipleGalleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Anexar Fotos", fontSize = 12.sp)
                            }
                        }
                    } else if (!draft.isWatermarking) {
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
                                text = "Nenhuma foto capturada",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Tire ou anexe múltiplas fotos. Cada imagem terá registro georreferenciado automático com coordenadas GPS, RA e data/hora.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
                                    onClick = { multipleGalleryLauncher.launch("image/*") },
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

                    // 1. Região Administrativa (RA) reposicionada no bloco de endereço
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
                            .clickable { showRaDialog = true }
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
                                    text = draft.administrativeRegion,
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

                    // 2. Quadra
                    AppOutlinedTextField(
                        value = draft.quadra,
                        onValueChange = { viewModel.updateQuadra(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("address_quadra_field"),
                        label = { Text("Quadra (Q.)") },
                        placeholder = { Text("Ex: Quadra 04, QNM 18, SQS 308, Chácara 12...") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Conjunto e Número (lado a lado)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppOutlinedTextField(
                            value = draft.conjunto,
                            onValueChange = { viewModel.updateConjunto(it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("address_conjunto_field"),
                            label = { Text("Conjunto (Conj.)") },
                            placeholder = { Text("Ex: Conj. A, B...") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        AppOutlinedTextField(
                            value = draft.numero,
                            onValueChange = { viewModel.updateNumero(it) },
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
                                text = "Coordenadas GPS Atuais:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val coordsFormatted = String.format(
                                java.util.Locale.US,
                                "Lat: %.6f° | Lon: %.6f°",
                                draft.latitude,
                                draft.longitude
                            )
                            Text(
                                text = coordsFormatted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier.testTag("refresh_gps_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Atualizar GPS",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 3. SECTION: QUANTIDADE DE ANIMAIS & AVALIAÇÃO PPBEA
            val isDog = draft.isDog
            SectionHeader(
                title = if (isDog) "3. Quantidade de Cães & Avaliação PPBEA" else "3. Quantidade de Animais & Avaliação PPBEA",
                description = "Contagem dos animais e perícia de bem-estar animal com botões switch (PROTOCOLO PPBEA – ANEXO D)."
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
                                text = if (isDog) "Total de Cães" else "Total de Equinos",
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

                        // Stepper controller guaranteed to fit within screen
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (draft.horseCount > 1) {
                                        viewModel.updateHorseCount(draft.horseCount - 1)
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
                                        text = draft.horseCount.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.updateHorseCount(draft.horseCount + 1) },
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
                            val isSelected = draft.horseCount == count
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.updateHorseCount(count) }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = if (count == 10) "10+" else "$count",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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

                    // Sinais de Maus-Tratos Field
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
                                    text = if (isDog) "Cães c/ Sinais de Maus-Tratos" else "Sinais de Maus-Tratos",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (draft.mistreatedHorseCount > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface
                                )
                                if (draft.mistreatedHorseCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFFFFEBEE),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ALERTA",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC62828),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (isDog) "Caninos com ferimentos, sarna, caquexia ou correntes curtas" else "Animais com ferimentos, caquexia ou arreios lesivos",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Stepper controller for mistreated horses
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (draft.mistreatedHorseCount > 0) {
                                        viewModel.updateMistreatedHorseCount(draft.mistreatedHorseCount - 1)
                                    }
                                },
                                enabled = draft.mistreatedHorseCount > 0,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (draft.mistreatedHorseCount > 0) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    )
                                    .testTag("decrease_mistreated_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Diminuir maus-tratos",
                                    tint = if (draft.mistreatedHorseCount > 0) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (draft.mistreatedHorseCount > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .width(54.dp)
                                    .height(44.dp)
                                    .testTag("mistreated_count_display")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = draft.mistreatedHorseCount.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = if (draft.mistreatedHorseCount > 0) Color.White else MaterialTheme.colorScheme.onSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (draft.mistreatedHorseCount < draft.horseCount) {
                                        viewModel.updateMistreatedHorseCount(draft.mistreatedHorseCount + 1)
                                    }
                                },
                                enabled = draft.mistreatedHorseCount < draft.horseCount,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (draft.mistreatedHorseCount < draft.horseCount) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                                    .testTag("increase_mistreated_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Aumentar maus-tratos",
                                    tint = if (draft.mistreatedHorseCount < draft.horseCount) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // CAMPO 3: AVALIAÇÃO DE BEM-ESTAR ANIMAL (ANEXO D - PPBEA - RESOLUÇÃO CFMV 1.236/2018)
                    // Formato: Caixa de Diálogo com Botões Switch
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = if (draft.mistreatmentIndicators.isNotEmpty())
                            Color(0xFFFFF5F5)
                        else if (draft.adequateIndicators.isNotEmpty())
                            Color(0xFFF1F8F4)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            if (draft.mistreatmentIndicators.isNotEmpty()) Color(0xFFEF9A9A)
                            else if (draft.adequateIndicators.isNotEmpty()) Color(0xFFA5D6A7)
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
                                        imageVector = if (draft.mistreatmentIndicators.isNotEmpty()) Icons.Default.Warning else Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (draft.mistreatmentIndicators.isNotEmpty()) Color(0xFFC62828) else Color(0xFF2E7D32),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "PROTOCOLO PPBEA – ANEXO D",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = if (draft.mistreatmentIndicators.isNotEmpty()) Color(0xFFC62828) else Color(0xFF2E7D32)
                                        )
                                        Text(
                                            text = "Perícia em Bem-Estar Animal • Res. CFMV 1.236/2018",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    color = if (draft.mistreatmentIndicators.isNotEmpty()) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${draft.mistreatmentIndicators.size} Alertas • ${draft.adequateIndicators.size} Adequados",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (draft.mistreatmentIndicators.isNotEmpty()) Color(0xFFC62828) else Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 4 INDICADORES OFICIAIS DO PROTOCOLO PPBEA (ANEXO D - RES. CFMV 1.236/2018)
                            Text(
                                text = "Os 4 Indicadores de Bem-Estar Animal (Situação Boa vs Ruim):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 1. Nutricional
                                val nutStatus = draft.ppbeaDiagnosis?.nutricionalParecer
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (nutStatus) {
                                        com.example.data.model.ParecerDomain.ADEQUADO -> Color(0xFFE8F5E9)
                                        com.example.data.model.ParecerDomain.REGULAR -> Color(0xFFFFF8E1)
                                        com.example.data.model.ParecerDomain.INADEQUADO -> Color(0xFFFFEBEE)
                                        null -> MaterialTheme.colorScheme.surface
                                    },
                                    border = BorderStroke(1.dp, when (nutStatus) {
                                        com.example.data.model.ParecerDomain.ADEQUADO -> Color(0xFF81C784)
                                        com.example.data.model.ParecerDomain.REGULAR -> Color(0xFFFFD54F)
                                        com.example.data.model.ParecerDomain.INADEQUADO -> Color(0xFFEF9A9A)
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
                                                com.example.data.model.ParecerDomain.ADEQUADO -> Color(0xFF2E7D32)
                                                com.example.data.model.ParecerDomain.REGULAR -> Color(0xFFF57C00)
                                                com.example.data.model.ParecerDomain.INADEQUADO -> Color(0xFFC62828)
                                                null -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }

                                // 2. Conforto
                                val confStatus = draft.ppbeaDiagnosis?.confortoParecer
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (confStatus) {
                                        com.example.data.model.ParecerDomain.ADEQUADO -> Color(0xFFE8F5E9)
                                        com.example.data.model.ParecerDomain.REGULAR -> Color(0xFFFFF8E1)
                                        com.example.data.model.ParecerDomain.INADEQUADO -> Color(0xFFFFEBEE)
                                        null -> MaterialTheme.colorScheme.surface
                                    },
                                    border = BorderStroke(1.dp, when (confStatus) {
                                        com.example.data.model.ParecerDomain.ADEQUADO -> Color(0xFF81C784)
                                        com.example.data.model.ParecerDomain.REGULAR -> Color(0xFFFFD54F)
                                        com.example.data.model.ParecerDomain.INADEQUADO -> Color(0xFFEF9A9A)
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
                                                com.example.data.model.ParecerDomain.ADEQUADO -> Color(0xFF2E7D32)
                                                com.example.data.model.ParecerDomain.REGULAR -> Color(0xFFF57C00)
                                                com.example.data.model.ParecerDomain.INADEQUADO -> Color(0xFFC62828)
                                                null -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }

                                // 3. Saúde
                                val saudeStatus = draft.ppbeaDiagnosis?.saudeParecer
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (saudeStatus) {
                                        com.example.data.model.ParecerDomain.ADEQUADO -> Color(0xFFE8F5E9)
                                        com.example.data.model.ParecerDomain.REGULAR -> Color(0xFFFFF8E1)
                                        com.example.data.model.ParecerDomain.INADEQUADO -> Color(0xFFFFEBEE)
                                        null -> MaterialTheme.colorScheme.surface
                                    },
                                    border = BorderStroke(1.dp, when (saudeStatus) {
                                        com.example.data.model.ParecerDomain.ADEQUADO -> Color(0xFF81C784)
                                        com.example.data.model.ParecerDomain.REGULAR -> Color(0xFFFFD54F)
                                        com.example.data.model.ParecerDomain.INADEQUADO -> Color(0xFFEF9A9A)
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
                                                com.example.data.model.ParecerDomain.ADEQUADO -> Color(0xFF2E7D32)
                                                com.example.data.model.ParecerDomain.REGULAR -> Color(0xFFF57C00)
                                                com.example.data.model.ParecerDomain.INADEQUADO -> Color(0xFFC62828)
                                                null -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }

                                // 4. Comportamental
                                val compStatus = draft.ppbeaDiagnosis?.comportamentalParecer
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (compStatus) {
                                        com.example.data.model.ParecerDomain.ADEQUADO -> Color(0xFFE8F5E9)
                                        com.example.data.model.ParecerDomain.REGULAR -> Color(0xFFFFF8E1)
                                        com.example.data.model.ParecerDomain.INADEQUADO -> Color(0xFFFFEBEE)
                                        null -> MaterialTheme.colorScheme.surface
                                    },
                                    border = BorderStroke(1.dp, when (compStatus) {
                                        com.example.data.model.ParecerDomain.ADEQUADO -> Color(0xFF81C784)
                                        com.example.data.model.ParecerDomain.REGULAR -> Color(0xFFFFD54F)
                                        com.example.data.model.ParecerDomain.INADEQUADO -> Color(0xFFEF9A9A)
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
                                                com.example.data.model.ParecerDomain.ADEQUADO -> Color(0xFF2E7D32)
                                                com.example.data.model.ParecerDomain.REGULAR -> Color(0xFFF57C00)
                                                com.example.data.model.ParecerDomain.INADEQUADO -> Color(0xFFC62828)
                                                null -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 1. BOTÃO PRINCIPAL: Caixa de Diálogo com Botões Switch (Sim/Não) e Resultado
                            Button(
                                onClick = { showPpbeaEvaluationDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_open_ppbea_evaluation_dialog"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (draft.ppbeaDiagnosis?.ocorrenciaMausTratos == com.example.data.model.EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS) Color(0xFFC62828) else Color(0xFF1B5E20),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (draft.ppbeaDiagnosis != null) "Editar Avaliação & Diagnóstico (PROTOCOLO PPBEA)" else "Abrir Avaliação PPBEA (Switches Sim/Não)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // 2. Card de Diagnóstico Geral Realizado Conforme image.png
                            if (draft.ppbeaDiagnosis != null) {
                                val diag = draft.ppbeaDiagnosis!!
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = when (diag.grauBemEstar) {
                                        com.example.data.model.GrauBemEstar.MUITO_BAIXO, com.example.data.model.GrauBemEstar.BAIXO -> Color(0xFFFFEBEE)
                                        com.example.data.model.GrauBemEstar.REGULAR -> Color(0xFFFFF8E1)
                                        com.example.data.model.GrauBemEstar.ALTO, com.example.data.model.GrauBemEstar.MUITO_ALTO -> Color(0xFFE8F5E9)
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

                                            Surface(
                                                color = if (diag.gerouBoletimOcorrencia) Color(0xFFFFCDD2) else Color(0xFFC8E6C9),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = if (diag.gerouBoletimOcorrencia) "BO POLICIAL" else "SEM BO",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (diag.gerouBoletimOcorrencia) Color(0xFFB71C1C) else Color(0xFF1B5E20),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                                                if (diag.nutricionalParecer != com.example.data.model.ParecerDomain.ADEQUADO) inadequacoes.add("Nutrição")
                                                if (diag.confortoParecer != com.example.data.model.ParecerDomain.ADEQUADO) inadequacoes.add("Conforto")
                                                if (diag.saudeParecer != com.example.data.model.ParecerDomain.ADEQUADO) inadequacoes.add("Saúde")
                                                if (diag.comportamentalParecer != com.example.data.model.ParecerDomain.ADEQUADO) inadequacoes.add("Comportamento")

                                                val textoSintese = buildString {
                                                    append("Protocolo PPBEA (Res. CFMV nº 1.236/2018). ")
                                                    append("Constatado Grau de Bem-Estar ${diag.grauBemEstar.label.uppercase()}. ")
                                                    if (diag.ocorrenciaMausTratos == com.example.data.model.EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS) {
                                                        append("Restam configurados indícios técnicos de MAUS-TRATOS")
                                                        if (inadequacoes.isNotEmpty()) {
                                                            append(" (inadequações em: ${inadequacoes.distinct().joinToString(", ")}).")
                                                        } else {
                                                            append(".")
                                                        }
                                                    } else if (diag.ocorrenciaMausTratos == com.example.data.model.EnquadramentoMausTratos.PRECISA_RECOMENDACOES_RETORNO) {
                                                        append("Não configura crime imediato de maus-tratos, mas exige termo de orientação com retorno obrigatório.")
                                                    } else {
                                                        append("NÃO CONFIGURA MAUS-TRATOS. Condições adequadas de bem-estar constatadas.")
                                                    }
                                                }
                                                viewModel.updateHorseDescription(textoSintese)
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
                                                onClick = { viewModel.clearPpbeaDiagnosis() },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Limpar Diagnóstico", fontSize = 11.sp, color = Color(0xFFC62828))
                                            }
                                        }
                                    }
                                }
                            }

                            // Preview dos Indicadores Selecionados
                            if (draft.mistreatmentIndicators.isNotEmpty() || draft.adequateIndicators.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Indicadores assinalados na ocorrência (toque no X para remover):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                // Alertas de Maus-Tratos assinalados
                                draft.mistreatmentIndicators.forEach { alertItem ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFFEBEE),
                                        border = BorderStroke(0.5.dp, Color(0xFFEF9A9A))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "⚠️", fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = alertItem,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFB71C1C),
                                                    lineHeight = 14.sp
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.toggleMistreatmentIndicator(alertItem) },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remover indicador",
                                                    tint = Color(0xFFC62828),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Condições Adequadas assinaladas
                                draft.adequateIndicators.forEach { adItem ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFE8F5E9),
                                        border = BorderStroke(0.5.dp, Color(0xFFA5D6A7))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "✅", fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = adItem,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF2E7D32),
                                                    lineHeight = 14.sp
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.toggleAdequateIndicator(adItem) },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remover indicador",
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(14.dp)
                                                )
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
                                    contentDescription = if (isDog) "Consultar 5 Domínios e Guia Canino" else "Consultar Atlas Anatômico e 5 Domínios",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isDog) "5 Domínios & Guia" else "Atlas & 5 Domínios",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    AppOutlinedTextField(
                        value = draft.horseDescription,
                        onValueChange = { viewModel.updateHorseDescription(it) },
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
                                        color = if (draft.requiresSeagriApprehension) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (draft.requiresSeagriApprehension) {
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
                                        .clickable { viewModel.updateRequiresSeagriApprehension(true) }
                                        .testTag("seagri_yes_button"),
                                    color = if (draft.requiresSeagriApprehension) Color(0xFFE65100) else MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "SIM",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (draft.requiresSeagriApprehension) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                        .clickable { viewModel.updateRequiresSeagriApprehension(false) }
                                        .testTag("seagri_no_button"),
                                    color = if (!draft.requiresSeagriApprehension) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "NÃO",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!draft.requiresSeagriApprehension) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        if (draft.requiresSeagriApprehension) {
                            Spacer(modifier = Modifier.height(10.dp))
                            AppOutlinedTextField(
                                value = draft.seagriNotes,
                                onValueChange = { viewModel.updateSeagriNotes(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("seagri_notes_field"),
                                placeholder = {
                                    Text("Ex: Necessidade de caminhão gaiola da SEAGRI para 2 equinos soltos...")
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
                                    color = if (draft.requiresPmdfSupport) Color(0xFF1565C0) else MaterialTheme.colorScheme.onSurface
                                )
                                if (draft.requiresPmdfSupport) {
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
                                    .clickable { viewModel.updateRequiresPmdfSupport(true) }
                                    .testTag("pmdf_yes_button"),
                                color = if (draft.requiresPmdfSupport) Color(0xFF1565C0) else MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "SIM",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (draft.requiresPmdfSupport) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                    .clickable { viewModel.updateRequiresPmdfSupport(false) }
                                    .testTag("pmdf_no_button"),
                                color = if (!draft.requiresPmdfSupport) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "NÃO",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!draft.requiresPmdfSupport) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    if (draft.requiresPmdfSupport) {
                        Spacer(modifier = Modifier.height(10.dp))
                        AppOutlinedTextField(
                            value = draft.pmdfNotes,
                            onValueChange = { viewModel.updatePmdfNotes(it) },
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

                    // 4.3 REQUISIÇÃO DE OUTRO ÓRGÃO / OUTRAS PROVIDÊNCIAS DE APREENSÃO
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
                                    color = if (draft.requiresOtherAgencySupport) Color(0xFF6A1B9A) else MaterialTheme.colorScheme.onSurface
                                )
                                if (draft.requiresOtherAgencySupport) {
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
                                    .clickable { viewModel.updateRequiresOtherAgencySupport(true) }
                                    .testTag("other_agency_yes_button"),
                                color = if (draft.requiresOtherAgencySupport) Color(0xFF6A1B9A) else MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "SIM",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (draft.requiresOtherAgencySupport) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                    .clickable { viewModel.updateRequiresOtherAgencySupport(false) }
                                    .testTag("other_agency_no_button"),
                                color = if (!draft.requiresOtherAgencySupport) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "NÃO",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!draft.requiresOtherAgencySupport) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    if (draft.requiresOtherAgencySupport) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Sugestões rápidas de órgãos públicos no DF
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
                                val isSelected = draft.otherAgencyName.equals(agency, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.updateOtherAgencyName(agency)
                                    },
                                    label = { Text(agency, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFF3E5F5),
                                        selectedLabelColor = Color(0xFF6A1B9A)
                                    )
                                )
                            }
                        }

                        AppOutlinedTextField(
                            value = draft.otherAgencyName,
                            onValueChange = { viewModel.updateOtherAgencyName(it) },
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
                            value = draft.otherAgencyNotes,
                            onValueChange = { viewModel.updateOtherAgencyNotes(it) },
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
                        value = draft.imageNotes,
                        onValueChange = { viewModel.updateImageNotes(it) },
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
                    draft.tutors.forEachIndexed { index, tutor ->
                        val isDocValid = CpfValidator.isValid(tutor.cpf)
                        val isDocFilled = tutor.cpf.isNotBlank()
                        val isCnpj = CpfValidator.isCnpj(tutor.cpf)
                        val docType = CpfValidator.getDocumentType(tutor.cpf)

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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

                                    if (draft.tutors.size > 1) {
                                        IconButton(
                                            onClick = { viewModel.removeTutor(index) },
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
                                    onValueChange = { viewModel.updateTutor(index, it, tutor.cpf) },
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
                                        onValueChange = { viewModel.updateTutor(index, tutor.name, it) },
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
                        onClick = { viewModel.addTutor() },
                        modifier = Modifier.fillMaxWidth().testTag("add_tutor_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ Adicionar Outro Tutor")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7. ACTIONS: SALVAR / EXPORTAR PDF
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 1: Save and generate PDF directly
                Button(
                    onClick = {
                        viewModel.saveDraft(
                            onSuccess = { saved ->
                                Toast.makeText(context, "Fiscalização salva! Gerando PDF...", Toast.LENGTH_SHORT).show()
                                viewModel.exportSinglePdf(saved)
                                onSavedSuccessfully(saved.id)
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_and_pdf_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Salvar & Gerar Relatório PDF",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Button 2: Save only
                OutlinedButton(
                    onClick = {
                        viewModel.saveDraft(
                            onSuccess = { saved ->
                                Toast.makeText(context, "Fiscalização registrada com sucesso!", Toast.LENGTH_SHORT).show()
                                onSavedSuccessfully(saved.id)
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_only_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apenas Salvar no Aplicativo",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // CAIXA DE DIÁLOGO COM BOTÕES SWITCH (ANEXO D - PPBEA - RES. CFMV 1.236/2018)
    if (showPpbeaDialog) {
        val currentSpeciesName = if (draft.isDog) "Cachorro" else draft.species
        com.example.ui.components.PpbeaSwitchDialog(
            species = currentSpeciesName,
            selectedAlertIndicators = draft.mistreatmentIndicators,
            onToggleAlertIndicator = { item ->
                viewModel.toggleMistreatmentIndicator(item)
            },
            onClearAllAlerts = {
                viewModel.clearAllMistreatmentAlerts()
            },
            selectedAdequateIndicators = draft.adequateIndicators,
            onToggleAdequateIndicator = { item ->
                viewModel.toggleAdequateIndicator(item)
            },
            onClearAllAdequate = {
                viewModel.clearAllAdequateIndicators()
            },
            onDismiss = { showPpbeaDialog = false },
            onConfirm = { showPpbeaDialog = false }
        )
    }

    // CAIXA DE DIÁLOGO DE AVALIAÇÃO PPBEA COM SWITCHES SIM/NÃO, RESULTADO E DIAGNÓSTICO GERAL (IMAGE.PNG)
    if (showPpbeaEvaluationDialog) {
        com.example.ui.components.PpbeaEvaluationDialog(
            initialDiagnosis = draft.ppbeaDiagnosis,
            onDismiss = { showPpbeaEvaluationDialog = false },
            onSaveDiagnosis = { diagnosis ->
                viewModel.setPpbeaDiagnosis(diagnosis)
                showPpbeaEvaluationDialog = false
            }
        )
    }

    // Região Administrativa Picker Dialog
    if (showRaDialog) {
        RaSelectionDialog(
            currentRa = draft.administrativeRegion,
            onSelectRa = { selected ->
                viewModel.updateAdministrativeRegion(selected)
                showRaDialog = false
            },
            onDismiss = { showRaDialog = false }
        )
    }

    // Atlas Anatômico do Cavalo & Guia Técnico
    if (showAnatomyDialog) {
        val currentSpecies = if (draft.isDog) AnimalSpecies.DOG else AnimalSpecies.HORSE
        DfSafetyInfoDialog(
            species = currentSpecies,
            onDismiss = { showAnatomyDialog = false }
        )
    }

    // Fullscreen Photo Preview Dialog
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

    // Live Camera Screen with real-time GPS & RA HUD overlay
    if (showLiveCamera) {
        CameraCaptureScreen(
            initialLatitude = draft.latitude,
            initialLongitude = draft.longitude,
            administrativeRegion = draft.administrativeRegion,
            protocolNumber = draft.protocolNumber,
            capturedPhotosCount = draft.allPhotoPaths.size,
            onPhotoCaptured = { uri, exactLat, exactLng, exactTime, dynamicRa, photoTag, orientationDegrees ->
                viewModel.addDraftPhotoUri(
                    uri = uri,
                    exactLatitude = exactLat,
                    exactLongitude = exactLng,
                    exactTimestampMillis = exactTime,
                    exactRa = dynamicRa,
                    photoTag = photoTag,
                    orientationDegrees = orientationDegrees
                )
            },
            onPickFromGallery = {
                multipleGalleryLauncher.launch("image/*")
            },
            onClose = {
                showLiveCamera = false
            }
        )
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

@Composable
fun RaSelectionDialog(
    currentRa: String,
    onSelectRa: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var filterText by remember { mutableStateOf("") }
    val filteredList = remember(filterText) {
        if (filterText.isBlank()) DfConstants.REGIOES_ADMINISTRATIVAS
        else DfConstants.REGIOES_ADMINISTRATIVAS.filter {
            it.contains(filterText, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Selecione a Região Administrativa")
        },
        text = {
            Column(modifier = Modifier.height(380.dp)) {
                AppOutlinedTextField(
                    value = filterText,
                    onValueChange = { filterText = it },
                    placeholder = { Text("Filtrar RA...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredList.size) { index ->
                        val ra = filteredList[index]
                        val isSelected = ra == currentRa

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectRa(ra) },
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ra,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun createTempCameraUri(context: Context): Uri? {
    return try {
        val photosDir = File(context.cacheDir, "photos").apply {
            if (!exists()) mkdirs()
        }
        val uniqueSuffix = java.util.UUID.randomUUID().toString().take(6)
        val photoFile = File(photosDir, "camera_capture_${System.currentTimeMillis()}_$uniqueSuffix.jpg")
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    } catch (_: Exception) {
        null
    }
}
