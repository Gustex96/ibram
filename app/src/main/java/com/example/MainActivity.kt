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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.AlertDialog
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
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.InstitutionalNoticeDialog
import com.example.ui.components.UpdateFeedbackDialog
import com.example.ui.screens.DfSafetyInfoDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InspectionDetailScreen
import com.example.ui.screens.NewInspectionScreen
import com.example.ui.screens.ReportsHubScreen
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
    var showInstitutionalNoticeDialog by rememberSaveable { mutableStateOf(true) }

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
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_meio_ambiente),
                                    contentDescription = "Logo Meio Ambiente",
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Levantamento Operacional",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "APOIO FISCAL",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Apoio em Fiscalização & Monitoramento Animal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        },
                        actions = {
                            // Botão de Luz e Cor do Background (Proteção Ocular / Sensibilidade nos Olhos)
                            IconButton(
                                onClick = { showVisualModeDialog = true },
                                modifier = Modifier.testTag("action_visual_mode")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when (viewModel.visualComfortMode) {
                                        VisualComfortMode.LIGHT -> MaterialTheme.colorScheme.primaryContainer
                                        VisualComfortMode.EYE_CARE -> ComposeColor(0xFFE2D6BE)
                                        VisualComfortMode.DARK -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val icon = when (viewModel.visualComfortMode) {
                                            VisualComfortMode.LIGHT -> Icons.Default.LightMode
                                            VisualComfortMode.EYE_CARE -> Icons.Default.Visibility
                                            VisualComfortMode.DARK -> Icons.Default.DarkMode
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = "Controle de Cor e Luz do Aplicativo",
                                            tint = when (viewModel.visualComfortMode) {
                                                VisualComfortMode.LIGHT -> MaterialTheme.colorScheme.onPrimaryContainer
                                                VisualComfortMode.EYE_CARE -> ComposeColor(0xFF4A3E26)
                                                VisualComfortMode.DARK -> MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }

                            // Botão para verificar atualizações no GitHub com indicador de alerta
                            IconButton(
                                onClick = {
                                    if (viewModel.availableUpdate != null) {
                                        viewModel.openUpdateDialog()
                                    } else {
                                        viewModel.checkForUpdates(isManualCheck = true)
                                    }
                                },
                                modifier = Modifier.testTag("action_check_updates")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (viewModel.availableUpdate != null)
                                            MaterialTheme.colorScheme.errorContainer
                                        else
                                            MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (viewModel.isCheckingUpdate) {
                                                androidx.compose.material3.CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (viewModel.availableUpdate != null)
                                                        Icons.Default.NewReleases
                                                    else
                                                        Icons.Default.SystemUpdate,
                                                    contentDescription = "Verificar Atualizações no GitHub",
                                                    tint = if (viewModel.availableUpdate != null)
                                                        MaterialTheme.colorScheme.error
                                                    else
                                                        MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }
                                        }
                                    }
                                    // Badge indicador de alerta se nova versão estiver disponível
                                    if (viewModel.availableUpdate != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .align(Alignment.TopEnd)
                                                .background(Color.Red, CircleShape)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { showInfoDialog = true },
                                modifier = Modifier.testTag("action_info_dialog")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_owl),
                                            contentDescription = "Menu Conhecimento - Atlas Anatômico do Cavalo e Guia Técnico",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Botão do Aviso Institucional DIFIS-IV / IBRAM
                            IconButton(
                                onClick = { showInstitutionalNoticeDialog = true },
                                modifier = Modifier.testTag("action_institutional_notice")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "Aviso Institucional DIFIS-IV • IBRAM",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Painel") },
                            label = { Text("Painel") },
                            modifier = Modifier.testTag("nav_tab_home")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Histórico") },
                            label = { Text("Histórico") },
                            modifier = Modifier.testTag("nav_tab_history")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = "Relatórios") },
                            label = { Text("Relatórios PDF") },
                            modifier = Modifier.testTag("nav_tab_reports")
                        )
                    }
                },
                floatingActionButton = {
                    if (selectedTab != 2) {
                        FloatingActionButton(
                            onClick = { viewModel.navigateTo("NEW_INSPECTION") },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("main_add_fab")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Nova Fiscalização")
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
                                onNavigateToDetail = { id -> viewModel.navigateTo("DETAIL", id) }
                            )
                            1 -> HistoryScreen(
                                viewModel = viewModel,
                                onNavigateToNewInspection = { viewModel.navigateTo("NEW_INSPECTION") }
                            )
                            2 -> ReportsHubScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
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
                DfSafetyInfoDialog(onDismiss = { showInfoDialog = false })
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

    // Diálogo global de aviso em destaque para exportação de PDF com botão OK
    if (viewModel.showPdfDisclaimerDialog || viewModel.pendingPdfAction != null) {
        com.example.ui.components.PdfDisclaimerDialog(
            onDismiss = { viewModel.dismissPdfDisclaimer() },
            onConfirm = { viewModel.confirmPdfDisclaimer() }
        )
    }
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
