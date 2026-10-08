package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class VisualComfortMode(
    val title: String,
    val subtitle: String
) {
    LIGHT(
        title = "Modo Diurno (Claro)",
        subtitle = "Padrão nítido para operações diurnas com luz solar"
    ),
    EYE_CARE(
        title = "Conforto Ocular (Sépia)",
        subtitle = "Filtro âmbar quente para sensibilidade à luz e descanso visual"
    ),
    DARK(
        title = "Modo Noturno (Escuro)",
        subtitle = "Baixo brilho e fundo escuro para baixa iluminação"
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = GreenOnPrimaryDark,
    primaryContainer = Color(0xFF0D4430),
    onPrimaryContainer = Color(0xFF9CF2C8),
    secondary = AmberSecondaryDark,
    onSecondary = Color(0xFF422E00),
    secondaryContainer = Color(0xFF5C4200),
    onSecondaryContainer = Color(0xFFFFDEA3),
    tertiary = BlueTertiaryDark,
    background = Color(0xFF111714),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF161E1A),
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = Color(0xFF202A25),
    onSurfaceVariant = Color(0xFFBFC9C2),
    outlineVariant = Color(0xFF324039)
)

private val EyeCareColorScheme = lightColorScheme(
    primary = EyeCarePrimary,
    onPrimary = EyeCareOnPrimary,
    primaryContainer = EyeCarePrimaryContainer,
    onPrimaryContainer = EyeCareOnPrimaryContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = Color(0xFFEDE0C8),
    onSecondaryContainer = Color(0xFF322308),
    tertiary = Color(0xFF2B5B6D),
    onTertiary = Color.White,
    background = EyeCareBackground,
    onBackground = EyeCareOnSurface,
    surface = EyeCareSurface,
    onSurface = EyeCareOnSurface,
    surfaceVariant = EyeCareSurfaceVariant,
    onSurfaceVariant = EyeCareOnSurfaceVariant,
    outlineVariant = EyeCareOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenOnPrimaryContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberSecondaryContainer,
    onSecondaryContainer = AmberOnSecondaryContainer,
    tertiary = BlueTertiary,
    onTertiary = BlueOnTertiary,
    tertiaryContainer = BlueTertiaryContainer,
    onTertiaryContainer = BlueOnTertiaryContainer,
    background = Color(0xFFF8FAF9),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFF1F5F3),
    onSurfaceVariant = Color(0xFF404944),
    outlineVariant = Color(0xFFDCE3DF)
)

@Composable
fun MyApplicationTheme(
    visualMode: VisualComfortMode = VisualComfortMode.LIGHT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (visualMode) {
        VisualComfortMode.LIGHT -> LightColorScheme
        VisualComfortMode.EYE_CARE -> EyeCareColorScheme
        VisualComfortMode.DARK -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
