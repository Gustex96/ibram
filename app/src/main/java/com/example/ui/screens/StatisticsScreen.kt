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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.InspectionViewModel

@Composable
fun StatisticsScreen(
    viewModel: InspectionViewModel,
    modifier: Modifier = Modifier
) {
    val inspections by viewModel.inspections.collectAsState()

    val totalInspections = inspections.size
    val totalAnimals = inspections.sumOf { it.horseCount }
    val totalMistreated = inspections.sumOf { it.mistreatedHorseCount }

    val bySpecies = inspections.groupBy { it.speciesDisplay }
    val byRegion = inspections.groupBy { it.administrativeRegion }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Estatísticas Operacionais",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "Panorama das vistorias e fauna fiscalizada no DF",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Big Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00684A))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total Vistorias", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalInspections", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Animais Avaliados", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalAnimals", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDC2626))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Maus-Tratos", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalMistreated", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Distribution by Species
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = Color(0xFF00684A), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Distribuição por Espécie", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (bySpecies.isEmpty()) {
                    Text("Nenhum dado cadastrado.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    bySpecies.forEach { (spec, list) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(spec, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("${list.size} vistoria(s)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF00684A))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Distribution by RA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Regiões Administrativas com Ocorrências", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (byRegion.isEmpty()) {
                    Text("Nenhum dado cadastrado.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    byRegion.forEach { (ra, list) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ra, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("${list.size} ocorrência(s)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0284C7))
                        }
                    }
                }
            }
        }
    }
}
