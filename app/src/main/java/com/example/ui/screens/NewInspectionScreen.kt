package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.AnimalSpecies
import com.example.data.model.HorseInspection
import com.example.ui.viewmodel.InspectionViewModel
import com.example.util.CpfValidator
import com.example.util.WatermarkUtils
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInspectionScreen(
    viewModel: InspectionViewModel,
    onInspectionSaved: (HorseInspection) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val species by viewModel.draftSpecies.collectAsState()
    val customSpeciesName by viewModel.draftCustomSpecies.collectAsState()
    val animalCount by viewModel.draftAnimalCount.collectAsState()
    val mistreatedCount by viewModel.draftMistreatedCount.collectAsState()
    val region by viewModel.draftAdministrativeRegion.collectAsState()
    val quadra by viewModel.draftQuadra.collectAsState()
    val conjunto by viewModel.draftConjunto.collectAsState()
    val numero by viewModel.draftNumero.collectAsState()
    val lat by viewModel.draftLatitude.collectAsState()
    val lng by viewModel.draftLongitude.collectAsState()
    val photos by viewModel.draftPhotos.collectAsState()
    val notes by viewModel.draftNotes.collectAsState()
    val description by viewModel.draftDescription.collectAsState()
    val tutors by viewModel.draftTutors.collectAsState()
    val mistreatments by viewModel.draftMistreatments.collectAsState()
    val adequates by viewModel.draftAdequates.collectAsState()
    val bodyCondition by viewModel.draftBodyCondition.collectAsState()
    val hydration by viewModel.draftHydration.collectAsState()
    val posture by viewModel.draftPosture.collectAsState()
    val hoofDental by viewModel.draftHoofDental.collectAsState()
    val otherAgencies by viewModel.draftOtherAgencies.collectAsState()

    var newTutorName by remember { mutableStateOf("") }
    var newTutorCpf by remember { mutableStateOf("") }
    var raExpanded by remember { mutableStateOf(false) }
    var bcsExpanded by remember { mutableStateOf(false) }
    var hydrationExpanded by remember { mutableStateOf(false) }
    var postureExpanded by remember { mutableStateOf(false) }
    var hoofDentalExpanded by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val raList = listOf(
        "Plano Piloto", "Ceilândia", "Taguatinga", "Samambaia", "Gama",
        "Planaltina", "Águas Claras", "Santa Maria", "Guará", "São Sebastião",
        "Recanto das Emas", "Riacho Fundo", "Sobradinho", "Vicente Pires",
        "Brazlândia", "Paranoá", "Itapoã", "Sudoeste/Octogonal", "Sobradinho II",
        "Jardim Botânico", "Lago Sul", "Lago Norte", "Park Way", "Candangolândia",
        "Cruzeiro", "Varjão", "Estrutural", "Fercal", "Sol Nascente/Pôr do Sol",
        "Arniqueira", "Arapoanga", "Água Quente"
    )

    val activeSpeciesText = if (species == AnimalSpecies.OUTROS && customSpeciesName.isNotBlank()) {
        "${species.emoji} $customSpeciesName"
    } else {
        "${species.emoji} ${species.title}"
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val savedPath = WatermarkUtils.applyWatermarkAndSave(
                context = context,
                inputUri = tempCameraUri!!,
                latitude = lat,
                longitude = lng,
                timestamp = System.currentTimeMillis(),
                administrativeRegion = region,
                protocol = "DRAFT",
                speciesTitle = activeSpeciesText
            )
            if (savedPath != null) {
                viewModel.addDraftPhoto(savedPath)
                Toast.makeText(context, "Foto salva e adicionada à galeria!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val photosDir = File(context.cacheDir, "photos").apply { if (!exists()) mkdirs() }
            val photoFile = File(photosDir, "temp_capture_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permissão da câmera necessária para fotos.", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val savedPath = WatermarkUtils.applyWatermarkAndSave(
                context = context,
                inputUri = uri,
                latitude = lat,
                longitude = lng,
                timestamp = System.currentTimeMillis(),
                administrativeRegion = region,
                protocol = "DRAFT",
                speciesTitle = activeSpeciesText
            )
            if (savedPath != null) {
                viewModel.addDraftPhoto(savedPath)
                Toast.makeText(context, "Foto importada com marca d'água!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Active Species Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00684A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "VISTORIA DE CAMPO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB703),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = activeSpeciesText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "DF Legal • Fauna",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 1: Quantitativo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Quantitativo de Animais",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = animalCount.toString(),
                        onValueChange = { viewModel.draftAnimalCount.value = it.toIntOrNull() ?: 1 },
                        label = { Text("Total de Animais") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("animal_count_input")
                    )
                    OutlinedTextField(
                        value = mistreatedCount.toString(),
                        onValueChange = { viewModel.draftMistreatedCount.value = it.toIntOrNull() ?: 0 },
                        label = { Text("Com Sinais de Maus-Tratos") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("mistreated_count_input")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 2: Localização e GPS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. Localização e Logradouro",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    OutlinedButton(
                        onClick = {
                            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                val loc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                if (loc != null) {
                                    viewModel.draftLatitude.value = loc.latitude
                                    viewModel.draftLongitude.value = loc.longitude
                                    Toast.makeText(context, "GPS atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Sinal GPS indisponível no momento.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Conceda permissão de localização no aparelho.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("get_gps_button")
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Atualizar GPS", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = raExpanded,
                    onExpandedChange = { raExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = region,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Região Administrativa (RA)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = raExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor().testTag("inspection_ra_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = raExpanded,
                        onDismissRequest = { raExpanded = false }
                    ) {
                        raList.forEach { ra ->
                            DropdownMenuItem(
                                text = { Text(ra) },
                                onClick = {
                                    viewModel.draftAdministrativeRegion.value = ra
                                    raExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quadra,
                        onValueChange = { viewModel.draftQuadra.value = it },
                        label = { Text("Quadra / Rua") },
                        modifier = Modifier.weight(1.2f).testTag("quadra_input")
                    )
                    OutlinedTextField(
                        value = conjunto,
                        onValueChange = { viewModel.draftConjunto.value = it },
                        label = { Text("Conjunto") },
                        modifier = Modifier.weight(0.9f).testTag("conjunto_input")
                    )
                    OutlinedTextField(
                        value = numero,
                        onValueChange = { viewModel.draftNumero.value = it },
                        label = { Text("Nº/Lote") },
                        modifier = Modifier.weight(0.9f).testTag("numero_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📍 Coordenadas: ${String.format(Locale.US, "%.6f°, %.6f° (SIRGAS 2000)", lat, lng)}",
                        fontSize = 11.sp,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 3: Registro Fotográfico (Enfileiradas com proporção e galeria)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Registro Fotográfico Georreferenciado",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "As fotos recebem marca d'água permanente e são gravadas na galeria do celular.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                val photosDir = File(context.cacheDir, "photos").apply { if (!exists()) mkdirs() }
                                val photoFile = File(photosDir, "temp_capture_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00684A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("take_photo_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tirar Foto")
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("gallery_photo_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galeria")
                    }
                }

                // Photos List Enfileiradas com Espaçamento
                if (photos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Fotografias Anexadas (${photos.size}):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(photos) { idx, path ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                modifier = Modifier
                                    .width(140.dp)
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .background(Color.Black)
                                    ) {
                                        AsyncImage(
                                            model = File(path),
                                            contentDescription = "Foto ${idx + 1}",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        IconButton(
                                            onClick = { viewModel.removeDraftPhoto(path) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(28.dp)
                                                .background(Color(0xCC000000), CircleShape)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Remover",
                                                tint = Color(0xFFFF8A80),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Foto ${idx + 1} de ${photos.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(6.dp),
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 4: Avaliação Fisiológica e Zootécnica
        val bcsOptions = listOf(
            "1 - Caquético / Emaciação Severa",
            "2 - Abaixo do Peso / Magro",
            "3 - Ideal (Nutrição Adequada)",
            "4 - Sobrepeso",
            "5 - Obeso"
        )
        val hydrationOptions = listOf(
            "Normal (Turgor cutâneo < 2s)",
            "Desidratação Leve (2-4s)",
            "Desidratação Moderada (4-6s)",
            "Desidratação Severa / Olhos Enovoados (> 6s)"
        )
        val postureOptions = listOf(
            "Alerta e Ativo",
            "Apático / Letárgico",
            "Claudicação / Dificuldade Locomotora",
            "Prostrado / Decúbito Prolongado",
            "Sinais Evidentes de Dor"
        )
        val hoofDentalOptions = listOf(
            "Cascos íntegros / sem lesões aparentes",
            "Cascos deformados / rachaduras",
            "Ferrageamento irregular / desgastado",
            "Desgaste dentário acentuado / feridas orais",
            "Sem alterações evidentes"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4. Avaliação Fisiológica e Zootécnica",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Parâmetros clínicos e escore de integridade física do animal",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Escore de Condição Corporal (ECC)
                ExposedDropdownMenuBox(
                    expanded = bcsExpanded,
                    onExpandedChange = { bcsExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = bodyCondition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Escore de Condição Corporal (ECC)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bcsExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor().testTag("bcs_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = bcsExpanded,
                        onDismissRequest = { bcsExpanded = false }
                    ) {
                        bcsOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.draftBodyCondition.value = opt
                                    bcsExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Estado de Hidratação
                ExposedDropdownMenuBox(
                    expanded = hydrationExpanded,
                    onExpandedChange = { hydrationExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = hydration,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado de Hidratação") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hydrationExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor().testTag("hydration_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = hydrationExpanded,
                        onDismissRequest = { hydrationExpanded = false }
                    ) {
                        hydrationOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.draftHydration.value = opt
                                    hydrationExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Postura e Comportamento
                ExposedDropdownMenuBox(
                    expanded = postureExpanded,
                    onExpandedChange = { postureExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = posture,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Postura e Comportamento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = postureExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor().testTag("posture_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = postureExpanded,
                        onDismissRequest = { postureExpanded = false }
                    ) {
                        postureOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.draftPosture.value = opt
                                    postureExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Condição de Cascos / Dentição
                ExposedDropdownMenuBox(
                    expanded = hoofDentalExpanded,
                    onExpandedChange = { hoofDentalExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = hoofDental,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cascos, Membros e Dentição") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hoofDentalExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor().testTag("hoof_dental_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = hoofDentalExpanded,
                        onDismissRequest = { hoofDentalExpanded = false }
                    ) {
                        hoofDentalOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.draftHoofDental.value = opt
                                    hoofDentalExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 5: Indicadores de Bem-Estar Adequado
        val adequateOptions = listOf(
            "Acesso contínuo à água limpa e fresca",
            "Alimentação de boa qualidade e em quantidade adequada",
            "Abrigo higienizado com proteção contra sol e chuva",
            "Espaço físico amplo e seguro para locomoção",
            "Controle sanitário, vacinação e vermifugação em dia",
            "Comportamento dócil, sem sinais de dor ou medo"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00684A), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "5. Condições Favoráveis de Bem-Estar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00684A)
                    )
                }
                Text(
                    text = "Pontos positivos de manejo verificados durante a inspeção",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                adequateOptions.forEach { opt ->
                    val selected = adequates.contains(opt)
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.toggleAdequate(opt) },
                        label = { Text(opt, fontSize = 12.sp) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF00684A)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE8F5E9),
                            selectedLabelColor = Color(0xFF00684A)
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 6: Indicadores de Maus-Tratos (Res. CFMV nº 1.236/2018)
        val mistreatmentOptions = listOf(
            "Desnutrição severa / Caquexia",
            "Desidratação evidente",
            "Lesões cutâneas / Feridas abertas",
            "Sinais de arreamento inadequado / chicoteamento",
            "Cascos deformados / Claudicação evidente",
            "Privação de água limpa ou alimento",
            "Alojamento insalubre / sem abrigo contra intempéries",
            "Sobrepeso de carga excessiva",
            "Trabalho em estado de exaustão"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "6. Constatação de Maus-Tratos",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                }
                Text(
                    text = "Parâmetros fundamentados na Resolução CFMV nº 1.236/2018",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                mistreatmentOptions.forEach { opt ->
                    val selected = mistreatments.contains(opt)
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.toggleMistreatment(opt) },
                        label = { Text(opt, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFEBEE),
                            selectedLabelColor = Color(0xFFC62828)
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 7: Tutores / Responsáveis
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "7. Identificação dos Tutores / Responsáveis",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newTutorName,
                        onValueChange = { newTutorName = it },
                        label = { Text("Nome do Tutor") },
                        modifier = Modifier.weight(1.3f).testTag("tutor_name_input")
                    )
                    OutlinedTextField(
                        value = newTutorCpf,
                        onValueChange = { newTutorCpf = it },
                        label = { Text("CPF ou CNPJ") },
                        modifier = Modifier.weight(1f).testTag("tutor_cpf_input")
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newTutorName.isNotBlank() || newTutorCpf.isNotBlank()) {
                            viewModel.addTutor(newTutorName, newTutorCpf)
                            newTutorName = ""
                            newTutorCpf = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00684A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_tutor_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Adicionar Tutor")
                }

                if (tutors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    tutors.forEachIndexed { tIdx, tut ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(tut.name.ifBlank { "Tutor sem nome" }, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                if (tut.cpf.isNotBlank()) {
                                    Text("Doc: ${CpfValidator.formatCpfOrCnpj(tut.cpf)}", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            }
                            IconButton(onClick = { viewModel.removeTutor(tIdx) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 8: Notas de Campo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "8. Descrição dos Animais, Órgãos Acionados e Notas de Campo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.draftDescription.value = it },
                    label = { Text("Descrição detalhada do animal (pelagem, marcas, porte)") },
                    modifier = Modifier.fillMaxWidth().testTag("animal_description_input"),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = otherAgencies,
                    onValueChange = { viewModel.draftOtherAgencies.value = it },
                    label = { Text("Órgãos de apoio acionados (PMDF, BPMA, SEAGRI, DEMA...)") },
                    modifier = Modifier.fillMaxWidth().testTag("other_agencies_input"),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { viewModel.draftNotes.value = it },
                    label = { Text("Observações operacionais e encaminhamentos (SEAGRI / PMDF)") },
                    modifier = Modifier.fillMaxWidth().testTag("field_notes_input"),
                    minLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                isSaving = true
                viewModel.saveCurrentInspection { saved ->
                    isSaving = false
                    Toast.makeText(context, "Vistoria ${saved.protocolNumber} gravada com sucesso!", Toast.LENGTH_LONG).show()
                    onInspectionSaved(saved)
                }
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("save_inspection_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00684A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Salvar e Gerar Relatório Técnico",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
