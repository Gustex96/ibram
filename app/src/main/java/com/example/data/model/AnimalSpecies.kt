package com.example.data.model

enum class AnimalSpecies(
    val title: String,
    val emoji: String,
    val description: String
) {
    GATO("Gato", "🐱", "Felinos domésticos ou comunitários"),
    CACHORRO("Cachorro", "🐶", "Caninos domésticos ou errantes"),
    CAVALO("Cavalo", "🐴", "Equinos de tração, montaria ou pastoreio"),
    GALINACEOS("Galináceos", "🐔", "Aves, galinhas, galos e criações"),
    OUTROS("Outros", "🐾", "Outras espécies (informar manualmente)");

    companion object {
        fun fromString(value: String): AnimalSpecies {
            return entries.firstOrNull { it.title.equals(value, ignoreCase = true) } ?: OUTROS
        }
    }
}
