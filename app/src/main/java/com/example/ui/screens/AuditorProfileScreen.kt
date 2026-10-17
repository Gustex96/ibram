package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditorProfile
import com.example.ui.viewmodel.InspectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditorProfileScreen(
    viewModel: InspectionViewModel,
    onContinueToInspection: () -> Unit,
    onChangeSpecies: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.auditorProfile.collectAsState()

    var name by remember(currentProfile) { mutableStateOf(currentProfile.name) }
    var badge by remember(currentProfile) { mutableStateOf(currentProfile.registrationNumber) }
    var agency by remember(currentProfile) { mutableStateOf(currentProfile.agency) }
    var region by remember(currentProfile) { mutableStateOf(currentProfile.administrativeRegion) }
    var phone by remember(currentProfile) { mutableStateOf(currentProfile.phone) }

    val agencyList = listOf("DF Legal", "BPMA / PMDF", "SEAGRI / DIPOVA", "IBRAM", "DEMA / PCDF", "Outro")
    var agencyExpanded by remember { mutableStateOf(false) }

    val raList = listOf(
        "Plano Piloto", "Ceilândia", "Taguatinga", "Samambaia", "Gama",
        "Planaltina", "Águas Claras", "Santa Maria", "Guará", "São Sebastião",
        "Recanto das Emas", "Riacho Fundo", "Sobradinho", "Vicente Pires",
        "Brazlândia", "Paranoá", "Itapoã", "Sudoeste/Octogonal", "Sobradinho II",
        "Jardim Botânico", "Lago Sul", "Lago Norte", "Park Way", "Candangolândia",
        "Cruzeiro", "Varjão", "Estrutural", "Fercal", "Sol Nascente/Pôr do Sol",
        "Arniqueira", "Arapoanga", "Água Quente"
    )
    var raExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // Auditor Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFF00684A),
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Perfil do Auditor Fiscal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Identificação funcional constante nos relatórios",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Nome do Auditor
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Completo do Auditor") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auditor_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Matrícula / Registro
                OutlinedTextField(
                    value = badge,
                    onValueChange = { badge = it },
                    label = { Text("Matrícula / Registro Funcional") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    placeholder = { Text("Ex: 123.456-7") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auditor_badge_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Órgão Fiscalizador (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = agencyExpanded,
                    onExpandedChange = { agencyExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = agency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Órgão Fiscalizador") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = agencyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("auditor_agency_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = agencyExpanded,
                        onDismissRequest = { agencyExpanded = false }
                    ) {
                        agencyList.forEach { ag ->
                            DropdownMenuItem(
                                text = { Text(ag) },
                                onClick = {
                                    agency = ag
                                    agencyExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Região Administrativa de Lotação (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = raExpanded,
                    onExpandedChange = { raExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = region,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Região Administrativa de Lotação") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = raExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("auditor_ra_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = raExpanded,
                        onDismissRequest = { raExpanded = false }
                    ) {
                        raList.forEach { ra ->
                            DropdownMenuItem(
                                text = { Text(ra) },
                                onClick = {
                                    region = ra
                                    raExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confirm Button (Continuar para a Vistoria)
        Button(
            onClick = {
                viewModel.updateAuditorProfile(
                    AuditorProfile(
                        name = name.ifBlank { "Auditor Fiscal de Campo" },
                        registrationNumber = badge,
                        agency = agency,
                        administrativeRegion = region,
                        phone = phone
                    )
                )
                onContinueToInspection()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("continue_to_inspection_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00684A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Continuar para a Vistoria",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
