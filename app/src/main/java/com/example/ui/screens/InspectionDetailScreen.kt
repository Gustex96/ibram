package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.HorseInspection
import com.example.ui.viewmodel.InspectionViewModel
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionDetailScreen(
    inspection: HorseInspection,
    viewModel: InspectionViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allPhotos = inspection.allPhotoPaths
    var selectedPhotoIndex by remember { mutableIntStateOf(0) }
    var showZoomDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Vistoria ${inspection.protocolNumber}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${inspection.speciesEmoji} ${inspection.speciesDisplay} • ${inspection.administrativeRegion}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.deleteInspection(inspection)
                            Toast.makeText(context, "Vistoria excluída.", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00684A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Main Photo Box
            if (allPhotos.isNotEmpty()) {
                val activePath = allPhotos.getOrNull(selectedPhotoIndex) ?: allPhotos[0]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clickable { showZoomDialog = true }
                    ) {
                        AsyncImage(
                            model = File(activePath),
                            contentDescription = "Foto ${selectedPhotoIndex + 1}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = Color(0xAA000000),
                            shape = RoundedCornerShape(bottomStart = 8.dp),
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ampliar", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Thumbnails if multiple
                if (allPhotos.size > 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(allPhotos) { idx, path ->
                            val isSelected = idx == selectedPhotoIndex
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                                    .clickable { selectedPhotoIndex = idx }
                            ) {
                                AsyncImage(
                                    model = File(path),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isSelected) {
                                    Surface(
                                        color = Color(0xCC00684A),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("#${idx + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action: Export PDF Report
            Button(
                onClick = {
                    val pdfFile = viewModel.generatePdf(context, inspection)
                    if (pdfFile != null) {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, "Relatório Técnico - Vistoria ${inspection.protocolNumber}")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório Técnico"))
                    } else {
                        Toast.makeText(context, "Erro ao gerar PDF", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("share_pdf_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00684A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compartilhar Laudo Técnico (PDF)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Technical Data Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dados Técnicos da Ocorrência", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Espécie: ${inspection.speciesEmoji} ${inspection.speciesDisplay}", fontSize = 13.sp)
                    Text("• Quantitativo: ${inspection.horseCount} animal(is) (${inspection.mistreatedHorseCount} com sinais de maus-tratos)", fontSize = 13.sp)
                    Text("• Região Administrativa: ${inspection.administrativeRegion}", fontSize = 13.sp)
                    Text("• Logradouro: Quadra ${inspection.quadra}, Conj. ${inspection.conjunto}, Nº ${inspection.numero}", fontSize = 13.sp)
                    Text("• GPS: ${String.format(Locale.US, "%.6f°, %.6f° (SIRGAS 2000)", inspection.latitude, inspection.longitude)}", fontSize = 13.sp)
                    Text("• Data/Hora: ${inspection.formattedCaptureDate}", fontSize = 13.sp)
                    Text("• Auditor: ${inspection.auditorName} (${inspection.auditorAgency})", fontSize = 13.sp)
                    if (inspection.horseDescription.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Descrição do Animal: ${inspection.horseDescription}", fontSize = 12.sp, color = Color(0xFF475569))
                    }
                    if (inspection.mistreatmentList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Maus-Tratos Constatados:", fontWeight = FontWeight.Bold, color = Color(0xFFC62828), fontSize = 13.sp)
                        inspection.mistreatmentList.forEach {
                            Text("• $it", fontSize = 12.sp, color = Color(0xFFC62828))
                        }
                    }
                }
            }
        }
    }

    // Zoom Dialog
    if (showZoomDialog && allPhotos.isNotEmpty()) {
        val activePath = allPhotos.getOrNull(selectedPhotoIndex) ?: allPhotos[0]
        Dialog(onDismissRequest = { showZoomDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showZoomDialog = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(activePath),
                    contentDescription = "Foto ampliada",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
