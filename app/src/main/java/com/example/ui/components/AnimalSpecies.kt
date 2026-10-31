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

/**
 * Converte a espécie textual/status em um valor do enum AnimalSpecies
 * para abertura assertiva dos guias técnicos e do Anexo F.
 */
fun resolveAnimalSpecies(species: String?, isDog: Boolean = false): AnimalSpecies {
    val norm = species?.trim()?.lowercase() ?: ""
    return when {
        norm.contains("gato") || norm.contains("felin") || norm == "cat" -> AnimalSpecies.CAT
        isDog || norm.contains("cão") || norm.contains("cao") || norm.contains("cachorro") || norm.contains("canin") || norm == "dog" -> AnimalSpecies.DOG
        norm.contains("gal") || norm.contains("ave") || norm.contains("poultry") -> AnimalSpecies.POULTRY
        norm.contains("cavalo") || norm.contains("equin") || norm.contains("equid") || norm == "horse" -> AnimalSpecies.HORSE
        norm.isNotBlank() && !norm.contains("cavalo") && !norm.contains("horse") -> AnimalSpecies.OUTROS
        else -> if (isDog) AnimalSpecies.DOG else AnimalSpecies.HORSE
    }
}

