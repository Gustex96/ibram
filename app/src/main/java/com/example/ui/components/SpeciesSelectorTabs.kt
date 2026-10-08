package com.example.ui.components

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Representa as espécies contempladas pelo aplicativo e futuros módulos individualizados de
 * avaliação de bem-estar animal.
 */
enum class AnimalSpecies(
    val id: String,
    val displayName: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int,
    val emoji: String,
    val isCurrentlyActive: Boolean,
    val badgeText: String?
) {
    DOG(
        id = "dog",
        displayName = "Cachorro",
        subtitle = "Cães e filhotes",
        iconRes = R.drawable.ic_species_dog,
        emoji = "🐕",
        isCurrentlyActive = false,
        badgeText = "Em breve"
    ),
    CAT(
        id = "cat",
        displayName = "Gato",
        subtitle = "Felinos domésticos",
        iconRes = R.drawable.ic_species_cat,
        emoji = "🐈",
        isCurrentlyActive = false,
        badgeText = "Em breve"
    ),
    HORSE(
        id = "horse",
        displayName = "Cavalo",
        subtitle = "Equinos & Carroças",
        iconRes = R.drawable.ic_species_horse,
        emoji = "🐎",
        isCurrentlyActive = true,
        badgeText = "Ativo"
    ),
    ROOSTER(
        id = "rooster",
        displayName = "Galo",
        subtitle = "Galos & Aves",
        iconRes = R.drawable.ic_species_rooster,
        emoji = "🐓",
        isCurrentlyActive = false,
        badgeText = "Em breve"
    )
}

/**
 * Seletor horizontal com ícones e identificadores das espécies: Cachorro, Gato, Cavalo e Galo.
 * Permite ao auditor alternar ou visualizar as categorias que serão futuramente individualizadas
 * para o levantamento de bem-estar animal por espécie.
 */
@Composable
fun SpeciesSelectorTabs(
    selectedSpecies: AnimalSpecies,
    onSpeciesSelected: (AnimalSpecies) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Cabeçalho da seção de espécies
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ESPÉCIE DO LEVANTAMENTO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
            Text(
                text = "Módulos individualizados",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Barra com os cards das 4 espécies (Cachorro, Gato, Cavalo, Galo)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimalSpecies.entries.forEach { species ->
                val isSelected = species == selectedSpecies

                SpeciesCard(
                    species = species,
                    isSelected = isSelected,
                    onClick = {
                        onSpeciesSelected(species)
                        if (!species.isCurrentlyActive) {
                            Toast.makeText(
                                context,
                                "Módulo de ${species.displayName} em fase de homologação (PPBEA/CRMV). Atualmente o módulo ativo é Cavalo (Equinos).",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SpeciesCard(
    species: AnimalSpecies,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = if (isSelected) {
        primaryColor.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    val borderColor = if (isSelected) {
        primaryColor
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    val iconTint = if (isSelected) {
        primaryColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val textColor = if (isSelected) {
        primaryColor
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 1.8.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("species_tab_${species.id}"),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Container do Ícone com indicador de seleção e Badge
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) primaryColor.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = species.emoji,
                        fontSize = 19.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Indicador pequeno de status (Cavalo ativo / Outros em breve)
                if (species.isCurrentlyActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A))
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Nome da Espécie
            Text(
                text = species.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            // Mini Badge
            Text(
                text = if (species.isCurrentlyActive) "Ativo" else "Em breve",
                fontSize = 9.sp,
                fontWeight = if (species.isCurrentlyActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (species.isCurrentlyActive) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
