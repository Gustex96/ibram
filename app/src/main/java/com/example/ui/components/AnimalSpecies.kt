package com.example.ui.components

import androidx.annotation.DrawableRes
import com.example.R

/**
 * Representa as espécies contempladas pelo aplicativo para o levantamento de bem-estar animal:
 * Cachorro, Cavalo, Gato, Galináceos e Outros (personalizado).
 */
enum class AnimalSpecies(
    val id: String,
    val displayName: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val realImageRes: Int,
    val emoji: String,
    val isCurrentlyActive: Boolean,
    val badgeText: String?
) {
    CAT(
        id = "cat",
        displayName = "Gato",
        subtitle = "Felinos e felídeos",
        iconRes = R.drawable.ic_species_cat,
        realImageRes = R.drawable.real_species_cat,
        emoji = "🐱",
        isCurrentlyActive = true,
        badgeText = "Ativo"
    ),
    DOG(
        id = "dog",
        displayName = "Cachorro",
        subtitle = "Cães e caninos",
        iconRes = R.drawable.ic_species_dog,
        realImageRes = R.drawable.real_species_dog,
        emoji = "🐕",
        isCurrentlyActive = true,
        badgeText = "Ativo"
    ),
    HORSE(
        id = "horse",
        displayName = "Cavalo",
        subtitle = "Equinos & Carroças",
        iconRes = R.drawable.ic_species_horse,
        realImageRes = R.drawable.real_species_horse,
        emoji = "🐎",
        isCurrentlyActive = true,
        badgeText = "Ativo"
    ),
    POULTRY(
        id = "poultry",
        displayName = "Galináceos",
        subtitle = "Aves, Galos & Galinhas",
        iconRes = R.drawable.ic_species_rooster,
        realImageRes = R.drawable.real_species_rooster,
        emoji = "🐓",
        isCurrentlyActive = true,
        badgeText = "Ativo"
    ),
    OUTROS(
        id = "others",
        displayName = "Outros",
        subtitle = "Espécies Diversas",
        iconRes = R.drawable.app_protection_icon,
        realImageRes = R.drawable.real_species_others,
        emoji = "🐾",
        isCurrentlyActive = true,
        badgeText = "Personalizar"
    )
}
