package com.example

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.graphics.Color
import com.example.ui.components.UpdateAlertBanner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.data.db.AppDatabase
import com.example.data.model.HorseInspection
import com.example.ui.components.AnimalSpecies
import com.example.ui.components.AppSplashScreen
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.InstitutionalNoticeDialog
import com.example.ui.components.UpdateFeedbackDialog
import com.example.ui.screens.DfSafetyInfoDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InspectionDetailScreen
import com.example.ui.screens.NewInspectionScreen
import com.example.ui.screens.ReportsHubScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VisualComfortMode
import com.example.ui.viewmodel.InspectionViewModel
import com.example.util.WatermarkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: InspectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        seedInitialDemoDataIfEmpty()

        setContent {
            MyApplicationTheme(visualMode = viewModel.visualComfortMode) {
                MainAppNavHost(viewModel = viewModel)
            }
        }
    }

    private fun seedInitialDemoDataIfEmpty() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val current = db.horseInspectionDao().getAllInspections().first()
            if (current.isEmpty()) {
                // Generate initial demo record with photo so user immediately sees functional history and PDF export
                val width = 1080
                val height = 810
                val bmp = createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                val p = Paint().apply { color = AndroidColor.rgb(27, 59, 54) }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p)
                val tp = Paint().apply {
                    color = AndroidColor.rgb(255, 255, 255)
                    textSize = 32f
                    isAntiAlias = true
                }
                canvas.drawText("LEVANTAMENTO DE CAMPO • APOIO À FISCALIZAÇÃO", 60f, 260f, tp)
                tp.textSize = 24f
                tp.color = AndroidColor.rgb(203, 213, 225)
                canvas.drawText("Animais avistados nas margens da DF-001 / EPTG", 60f, 320f, tp)

                val file1 = WatermarkUtils.applyWatermarkToBitmap(
                    context = applicationContext,
                    source = bmp,
                    latitude = -15.834120,
                    longitude = -48.056780,
                    administrativeRegion = "Taguatinga (RA III)",
                    protocol = "DF-MAUS-TRATOS-2026-1042",
                    captureTimeMillis = System.currentTimeMillis() - 86400000L,
                    exemplarIndex = 1
                )

                if (file1 != null) {
                    db.horseInspectionDao().insertInspection(
                        HorseInspection(
                            protocolNumber = "DF-MAUS-TRATOS-2026-1042",
                            photoPath = file1.absolutePath,
                            latitude = -15.834120,
                            longitude = -48.056780,
                            captureTimestamp = System.currentTimeMillis() - 86400000L,
                            administrativeRegion = "Taguatinga (RA III)",
                            quadra = "QNF 24",
                            conjunto = "Conj. A",
                            numero = "Lote 12",
                            horseDescription = "1 cavalo castanho adulto e 2 potros sem ferradura, porte médio",
                            horseCount = 3,
                            mistreatedHorseCount = 1,
                            mistreatmentIndicators = "Caquexia / Emaciação (Costelas, vértebras e ossos pélvicos proeminentes - ECC 1 a 2);;Privação ou ausência de água potável / Recipiente inacessível ou imundo;;Arreios lesivos: pisaduras na cernelha, dorso ou cortes por freio/embocadura",
                            riskLevel = 4,
                            riskJustification = "Animais às margens de rodovias de alta velocidade do DF (EPTG, EPIA, DF-001).",
                            safetyRiskAssessment = "Risco elevado de colisão veicular por pastoreio contíguo à rodovia expressa EPTG em horário de pico.",
                            imageNotes = "Três cavalos pastando soltos próximos à pista marginal da EPTG com risco de evasão.",
                            tutorName = "Marcos Vinicius de Souza",
                            tutorCpf = "458.912.831-20"
                        )
                    )
                }

                // Second record in Ceilândia
                val bmp2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val c2 = Canvas(bmp2)
                p.color = AndroidColor.rgb(38, 50, 56)
                c2.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p)
                c2.drawText("MONITORAMENTO ANIMAL • APOIO À FISCALIZAÇÃO", 60f, 260f, tp)
                c2.drawText("Área residencial com pastoreio delimitado", 60f, 320f, tp)

                val file2 = WatermarkUtils.applyWatermarkToBitmap(
                    context = applicationContext,
                    source = bmp2,
                    latitude = -15.819450,
                    longitude = -48.112340,
                    administrativeRegion = "Ceilândia (RA IX)",
                    protocol = "DF-MAUS-TRATOS-2026-2184",
                    captureTimeMillis = System.currentTimeMillis() - 172800000L,
                    exemplarIndex = 1
                )

                if (file2 != null) {
                    db.horseInspectionDao().insertInspection(
                        HorseInspection(
                            protocolNumber = "DF-MAUS-TRATOS-2026-2184",
                            photoPath = file2.absolutePath,
                            latitude = -15.819450,
                            longitude = -48.112340,
                            captureTimestamp = System.currentTimeMillis() - 172800000L,
                            administrativeRegion = "Ceilândia (RA IX)",
                            quadra = "QNN 18",
                            conjunto = "Conj. E",
                            numero = "Casa 08",
                            horseDescription = "2 éguas tordilhas em bom estado nutricional, pelagem clara",
                            horseCount = 2,
                            mistreatedHorseCount = 0,
                            adequateIndicators = "Escore corporal adequado/ideal (ECC 4 a 6 - boa musculatura e cobertura adiposa);;Água limpa, fresca, potável e acessível à vontade em bebedouro higienizado;;Abrigo adequado contra sol, chuva e vento (baia coberta ou sombra natural farta);;Cascos íntegros, bem aparados ou ferrageamento correto em dia",
                            riskLevel = 2,
                            riskJustification = "Área semiurbana com contenção estável e baixa velocidade.",
                            safetyRiskAssessment = "Risco baixo de evasão para vias coletoras locais de velocidade reduzida.",
                            imageNotes = "Dois animais em lote cercado com acesso a pasto, tutor presente.",
                            tutorName = "Raimundo Nonato",
                            tutorCpf = "231.849.651-78"
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppNavHost(viewModel: InspectionViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }
    var showVisualModeDialog by rememberSaveable { mutableStateOf(false) }
    var showInstitutionalNoticeDialog by rememberSaveable { mutableStateOf(false) }
    var showSplashScreen by rememberSaveable { mutableStateOf(true) }

    // Verificação de atualizações no GitHub ao iniciar o aplicativo
    LaunchedEffect(Unit) {
        viewModel.checkForUpdates(isManualCheck = false)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (viewModel.activeScreenName) {
            "NEW_INSPECTION" -> {
                NewInspectionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { viewModel.navigateTo("MAIN") },
                    onSavedSuccessfully = { newId ->
                        viewModel.navigateTo("DETAIL", newId)
                    }
                )
            }
            "DETAIL" -> {
                InspectionDetailScreen(
                    inspectionId = viewModel.activeDetailId ?: 0L,
                    viewModel = viewModel,
                    onNavigateBack = { viewModel.navigateTo("MAIN") }
                )
            }
            else -> {
            var showAuditorDialog by rememberSaveable { mutableStateOf(false) }

            Scaffold(
                containerColor = Color(0xFFF4FAF6),
                topBar = {
                    Surface(
                        color = Color(0xFFF4FAF6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Esquerda: Logo Circular + Coluna (Título, Auditor, Espécie)
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_meio_ambiente),
                                    contentDescription = "Logo Meio Ambiente",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .clickable { showSplashScreen = true },
                                    contentScale = ContentScale.Crop
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // 1. Título
                                    Text(
                                        text = "Levantamento Operacional",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp,
                                        color = Color(0xFF0F172A),
                                        lineHeight = 22.sp
                                    )

                                    // 2. Campo Auditor: pílula clicável
                                    val auditorName = viewModel.auditorIdentification.ifBlank { "Não informado" }
                                    Surface(
                                        onClick = { showAuditorDialog = true },
                                        shape = CircleShape,
                                        color = Color(0xFFD1FAE5).copy(alpha = 0.7f),
                                        modifier = Modifier.testTag("header_auditor_pill")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = "Auditor: $auditorName • ",
                                                fontSize = 11.sp,
                                                color = Color(0xFF334155)
                                            )
                                            Text(
                                                text = "Definir",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF14532D)
                                            )
                                        }
                                    }

                                    // 3. Espécie Selecionada: pílula verde-menta
                                    val speciesName = if (viewModel.selectedSpecies == AnimalSpecies.OUTROS && !viewModel.customSpeciesName.isNullOrBlank())
                                        viewModel.customSpeciesName!!
                                    else
                                        viewModel.selectedSpecies.displayName
                                    val speciesEmoji = when (viewModel.selectedSpecies) {
                                        AnimalSpecies.HORSE -> "🐎"
                                        AnimalSpecies.DOG -> "🐕"
                                        AnimalSpecies.CAT -> "🐈"
                                        AnimalSpecies.POULTRY -> "🐔"
                                        AnimalSpecies.OUTROS -> "🐾"
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                                        modifier = Modifier.testTag("header_species_pill")
                                    ) {
                                        Text(
                                            text = "$speciesEmoji Espécie: $speciesName",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF022C22),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Direita: Apenas 2 botões circulares (Sol/Lua e Escudo/Segurança)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                // 1. Alternar tema (Sol/Lua)
                                Surface(
                                    onClick = { showVisualModeDialog = true },
                                    shape = CircleShape,
                                    color = Color(0xFFD1FAE5),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("action_visual_mode")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val icon = when (viewModel.visualComfortMode) {
                                            VisualComfortMode.LIGHT -> Icons.Default.LightMode
                                            VisualComfortMode.EYE_CARE -> Icons.Default.Visibility
                                            VisualComfortMode.DARK -> Icons.Default.DarkMode
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = "Alternar claridade",
                                            tint = Color(0xFF065F46),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // 2. Escudo / Segurança (Aviso aos auditores)
                                Surface(
                                    onClick = { showInstitutionalNoticeDialog = true },
                                    shape = CircleShape,
                                    color = Color(0xFFD1FAE5),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("action_institutional_notice")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "Aviso aos Auditores",
                                            tint = Color(0xFF065F46),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    // BARRA DE NAVEGAÇÃO INFERIOR (#EFF8F2, 64dp, 5 ITENS ALINHADOS CONFORME MOCKUP)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        color = Color(0xFFEFF8F2),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val navItems = listOf(
                                Triple(0, "Painel", Icons.Default.Dashboard),
                                Triple(1, "Histórico", Icons.Default.PhotoLibrary),
                                Triple(2, "Dados", Icons.Default.BarChart),
                                Triple(3, "Relatórios", Icons.Default.PictureAsPdf),
                                Triple(4, "Guia", null)
                            )

                            navItems.forEach { (tabIdx, label, iconVector) ->
                                val isSelected = selectedTab == tabIdx
                                val itemTag = when (tabIdx) {
                                    0 -> "nav_tab_home"
                                    1 -> "nav_tab_history"
                                    2 -> "nav_tab_stats"
                                    3 -> "nav_tab_reports"
                                    else -> "nav_tab_knowledge"
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable { selectedTab = tabIdx }
                                        .testTag(itemTag)
                                ) {
                                    if (isSelected) {
                                        Surface(
                                            color = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                                            shape = CircleShape,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (iconVector != null) {
                                                    Icon(
                                                        imageVector = iconVector,
                                                        contentDescription = label,
                                                        tint = Color(0xFF14532D),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_owl),
                                                        contentDescription = label,
                                                        tint = Color(0xFF14532D),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .padding(bottom = 2.dp, top = 3.dp)
                                                .size(22.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (iconVector != null) {
                                                Icon(
                                                    imageVector = iconVector,
                                                    contentDescription = label,
                                                    tint = Color(0xFF475569),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_owl),
                                                    contentDescription = label,
                                                    tint = Color(0xFF475569),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF14532D) else Color(0xFF64748B),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if (selectedTab == 0 || selectedTab == 1) {
                        Surface(
                            onClick = { viewModel.navigateTo("NEW_INSPECTION") },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF14532D),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(56.dp)
                                .testTag("main_add_fab")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Nova Fiscalização",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Banner de alerta de atualização em destaque caso haja nova versão detectada
                    viewModel.availableUpdate?.let { updateInfo ->
                        if (viewModel.showUpdateBanner) {
                            UpdateAlertBanner(
                                updateInfo = updateInfo,
                                onOpenDialog = { viewModel.openUpdateDialog() },
                                onDismissBanner = { viewModel.dismissUpdateBanner() }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (selectedTab) {
                            0 -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToNewInspection = { viewModel.navigateTo("NEW_INSPECTION") },
                                onNavigateToDetail = { id -> viewModel.navigateTo("DETAIL", id) },
                                onSwitchSpecies = { showSplashScreen = true },
                                onOpenAuditorDialog = { showAuditorDialog = true }
                            )
                            1 -> HistoryScreen(
                                viewModel = viewModel,
                                onNavigateToNewInspection = { viewModel.navigateTo("NEW_INSPECTION") }
                            )
                            2 -> StatisticsScreen(
                                viewModel = viewModel
                            )
                            3 -> ReportsHubScreen(
                                viewModel = viewModel
                            )
                            4 -> com.example.ui.screens.KnowledgeScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }

            if (showAuditorDialog) {
                var tempName by remember { mutableStateOf(viewModel.auditorIdentification) }
                AlertDialog(
                    onDismissRequest = { showAuditorDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF14532D))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Identificação do Auditor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column {
                            Text(
                                text = "Informe seu nome ou matrícula para identificação nas vistorias e relatórios oficiais:",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            com.example.ui.components.AppOutlinedTextField(
                                value = tempName,
                                onValueChange = { tempName = it },
                                modifier = Modifier.fillMaxWidth().testTag("dialog_auditor_input"),
                                label = { Text("Auditor Fiscal / Agente") },
                                placeholder = { Text("Ex: Auditor Silva • Matr. 123.456-X") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.setAuditorId(tempName)
                                showAuditorDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14532D))
                        ) {
                            Text("Salvar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAuditorDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (showVisualModeDialog) {
                VisualComfortSelectorDialog(
                    currentMode = viewModel.visualComfortMode,
                    onSelectMode = { mode ->
                        viewModel.setVisualComfort(mode)
                    },
                    onDismiss = { showVisualModeDialog = false }
                )
            }

            if (showInfoDialog) {
                DfSafetyInfoDialog(
                    species = viewModel.selectedSpecies,
                    onDismiss = { showInfoDialog = false }
                )
            }

            if (showInstitutionalNoticeDialog) {
                InstitutionalNoticeDialog(onDismiss = { showInstitutionalNoticeDialog = false })
            }

            // Diálogo de Atualização Disponível (Alerta de nova versão detectada via version.json do GitHub)
            val updateInfo = viewModel.availableUpdate
            if (viewModel.showAppUpdateDialog && updateInfo != null) {
                AppUpdateDialog(
                    updateInfo = updateInfo,
                    onDismiss = { viewModel.dismissUpdateDialog() }
                )
            }

            // Diálogo de Feedback / Diagnóstico de Atualização (ao verificar manualmente ou em caso de erro 404/rede)
            if (viewModel.showUpdateFeedbackDialog) {
                viewModel.updateCheckResult?.let { result ->
                    UpdateFeedbackDialog(
                        result = result,
                        onDismiss = { viewModel.dismissUpdateFeedbackDialog() },
                        onRetryWithUrl = { customUrl -> viewModel.saveCustomUpdateUrl(customUrl) },
                        onResetDefaultUrl = { viewModel.resetUpdateUrlToDefault() },
                        onSimulateUpdate = { viewModel.simulateUpdateCheck() }
                    )
                }
            }
        }
    }

        // Tela de Abertura / Início com imagem de fundo do Cerrado e Carrossel Multinavegação M3 com fotos reais
        if (showSplashScreen) {
            AppSplashScreen(
                initialSpecies = viewModel.selectedSpecies,
                onStart = { chosenSpecies, customName ->
                    viewModel.selectSpecies(chosenSpecies)
                    if (!customName.isNullOrBlank()) {
                        viewModel.setCustomSpecies(customName)
                    }
                    showSplashScreen = false
                    showInstitutionalNoticeDialog = true
                }
            )
        }
    }

    // Diálogo global de aviso em destaque para exportação de PDF com botão OK
    if (viewModel.showPdfDisclaimerDialog || viewModel.pendingPdfAction != null) {
        com.example.ui.components.PdfDisclaimerDialog(
            onDismiss = { viewModel.dismissPdfDisclaimer() },
            onConfirm = { viewModel.confirmPdfDisclaimer() }
        )
    }
}

@Composable
fun VisualComfortSelectorDialog(
    currentMode: VisualComfortMode,
    onSelectMode: (VisualComfortMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cor e Luz do Background",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ajuste o tom e a luz do aplicativo para maior conforto e auxílio aos auditores fiscais com sensibilidade nos olhos em campo:",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                VisualComfortMode.entries.forEach { mode ->
                    val isSelected = mode == currentMode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMode(mode) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (mode) {
                                VisualComfortMode.LIGHT -> Icons.Default.LightMode
                                VisualComfortMode.EYE_CARE -> Icons.Default.Visibility
                                VisualComfortMode.DARK -> Icons.Default.DarkMode
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = mode.subtitle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", fontWeight = FontWeight.Bold)
            }
        }
    )
}
