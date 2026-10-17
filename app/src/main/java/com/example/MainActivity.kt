package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.data.model.HorseInspection
import com.example.ui.screens.AuditorProfileScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.InspectionDetailScreen
import com.example.ui.screens.NewInspectionScreen
import com.example.ui.screens.SpeciesSelectionScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.viewmodel.InspectionViewModel

sealed class AppScreen {
    object SpeciesSelection : AppScreen()
    object AuditorProfile : AppScreen()
    object NewInspection : AppScreen()
    data class InspectionDetail(val inspection: HorseInspection) : AppScreen()
    object History : AppScreen()
    object Statistics : AppScreen()
}

class MainActivity : ComponentActivity() {
    private val viewModel: InspectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FiscalizacaoDFTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: InspectionViewModel) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.SpeciesSelection) }
    var selectedTab by remember { mutableStateOf(0) }

    // Handle back button smoothly
    BackHandler(enabled = currentScreen !is AppScreen.SpeciesSelection) {
        when (currentScreen) {
            is AppScreen.AuditorProfile -> currentScreen = AppScreen.SpeciesSelection
            is AppScreen.NewInspection -> currentScreen = AppScreen.AuditorProfile
            is AppScreen.InspectionDetail -> currentScreen = AppScreen.History
            is AppScreen.History, is AppScreen.Statistics -> {
                selectedTab = 0
                currentScreen = AppScreen.SpeciesSelection
            }
            else -> currentScreen = AppScreen.SpeciesSelection
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0 && currentScreen is AppScreen.SpeciesSelection,
                    onClick = {
                        selectedTab = 0
                        currentScreen = AppScreen.SpeciesSelection
                    },
                    icon = { Icon(Icons.Default.Pets, contentDescription = "Espécies") },
                    label = { Text("Início", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00684A),
                        selectedTextColor = Color(0xFF00684A),
                        indicatorColor = Color(0xFFE8F5E9)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1 && currentScreen is AppScreen.NewInspection,
                    onClick = {
                        selectedTab = 1
                        currentScreen = AppScreen.NewInspection
                    },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Nova Vistoria") },
                    label = { Text("Vistoria", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00684A),
                        selectedTextColor = Color(0xFF00684A),
                        indicatorColor = Color(0xFFE8F5E9)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2 && (currentScreen is AppScreen.History || currentScreen is AppScreen.InspectionDetail),
                    onClick = {
                        selectedTab = 2
                        currentScreen = AppScreen.History
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = "Histórico") },
                    label = { Text("Histórico", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00684A),
                        selectedTextColor = Color(0xFF00684A),
                        indicatorColor = Color(0xFFE8F5E9)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3 && currentScreen is AppScreen.Statistics,
                    onClick = {
                        selectedTab = 3
                        currentScreen = AppScreen.Statistics
                    },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Estatísticas") },
                    label = { Text("Dados", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00684A),
                        selectedTextColor = Color(0xFF00684A),
                        indicatorColor = Color(0xFFE8F5E9)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 4 && currentScreen is AppScreen.AuditorProfile,
                    onClick = {
                        selectedTab = 4
                        currentScreen = AppScreen.AuditorProfile
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil", maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00684A),
                        selectedTextColor = Color(0xFF00684A),
                        indicatorColor = Color(0xFFE8F5E9)
                    )
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val screen = currentScreen) {
                is AppScreen.SpeciesSelection -> {
                    SpeciesSelectionScreen(
                        viewModel = viewModel,
                        onSpeciesSelectedAndNavigateToProfile = {
                            currentScreen = AppScreen.AuditorProfile
                        }
                    )
                }

                is AppScreen.AuditorProfile -> {
                    AuditorProfileScreen(
                        viewModel = viewModel,
                        onContinueToInspection = {
                            currentScreen = AppScreen.NewInspection
                        },
                        onChangeSpecies = {
                            currentScreen = AppScreen.SpeciesSelection
                        }
                    )
                }

                is AppScreen.NewInspection -> {
                    NewInspectionScreen(
                        viewModel = viewModel,
                        onInspectionSaved = { saved ->
                            currentScreen = AppScreen.InspectionDetail(saved)
                        }
                    )
                }

                is AppScreen.InspectionDetail -> {
                    InspectionDetailScreen(
                        inspection = screen.inspection,
                        viewModel = viewModel,
                        onBack = {
                            currentScreen = AppScreen.History
                        }
                    )
                }

                is AppScreen.History -> {
                    HistoryScreen(
                        viewModel = viewModel,
                        onInspectionClick = { insp ->
                            currentScreen = AppScreen.InspectionDetail(insp)
                        }
                    )
                }

                is AppScreen.Statistics -> {
                    StatisticsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun FiscalizacaoDFTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = Color(0xFF00684A),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE8F5E9),
        onPrimaryContainer = Color(0xFF004D36),
        secondary = Color(0xFF006495),
        background = Color(0xFFF8FAFC),
        surface = Color.White,
        onSurface = Color(0xFF0F172A)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
