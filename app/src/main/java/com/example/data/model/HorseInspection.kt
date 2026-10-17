package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "inspections")
data class HorseInspection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val protocolNumber: String = "",
    val species: String = "Cavalo",
    val customSpeciesName: String = "",
    val speciesEmoji: String = "🐴",
    val horseCount: Int = 1,
    val mistreatedHorseCount: Int = 0,
    val auditorName: String = "",
    val auditorBadge: String = "",
    val auditorAgency: String = "DF Legal",
    val administrativeRegion: String = "Plano Piloto",
    val quadra: String = "",
    val conjunto: String = "",
    val numero: String = "",
    val latitude: Double = -15.7975,
    val longitude: Double = -47.8919,
    val captureTimestamp: Long = System.currentTimeMillis(),
    val formattedCaptureDate: String = "",
    val photoPath: String = "",
    val additionalPhotoPathsJson: String = "",
    val imageNotes: String = "",
    val horseDescription: String = "",
    val otherAgencyNotes: String = "",
    val tutorsJson: String = "",
    val mistreatmentIndicatorsJson: String = "",
    val adequateWelfareIndicatorsJson: String = "",
    val bodyConditionScore: String = "3 - Ideal",
    val hydrationStatus: String = "Normal",
    val postureBehavior: String = "Alerta e Responsivo",
    val hoofDentalStatus: String = "Sem alterações evidentes",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isHorse: Boolean
        get() = species.equals("Cavalo", ignoreCase = true) || species.contains("Equin", ignoreCase = true)

    val isDog: Boolean
        get() = species.equals("Cachorro", ignoreCase = true) || species.contains("Canin", ignoreCase = true)

    val isCat: Boolean
        get() = species.equals("Gato", ignoreCase = true) || species.contains("Felin", ignoreCase = true)

    val isPoultry: Boolean
        get() = species.equals("Galináceos", ignoreCase = true) || species.contains("Ave", ignoreCase = true)

    val speciesDisplay: String
        get() {
            return when {
                isHorse -> "Cavalo"
                isDog -> "Cachorro"
                isCat -> "Gato"
                isPoultry -> "Galináceos"
                customSpeciesName.isNotBlank() -> customSpeciesName
                else -> species
            }
        }

    val allPhotoPaths: List<String>
        get() {
            val list = mutableListOf<String>()
            if (photoPath.isNotBlank()) list.add(photoPath)
            if (additionalPhotoPathsJson.isNotBlank()) {
                val extras = additionalPhotoPathsJson.split(";;;").filter { it.isNotBlank() }
                for (p in extras) {
                    if (!list.contains(p)) list.add(p)
                }
            }
            return list
        }

    val mistreatmentList: List<String>
        get() = if (mistreatmentIndicatorsJson.isBlank()) emptyList() else mistreatmentIndicatorsJson.split(";;;").filter { it.isNotBlank() }

    val adequateList: List<String>
        get() = if (adequateWelfareIndicatorsJson.isBlank()) emptyList() else adequateWelfareIndicatorsJson.split(";;;").filter { it.isNotBlank() }

    val allTutors: List<Tutor>
        get() {
            if (tutorsJson.isBlank()) return emptyList()
            return tutorsJson.split("###").mapNotNull { token ->
                val parts = token.split("|||")
                if (parts.isNotEmpty()) {
                    Tutor(parts[0], parts.getOrElse(1) { "" })
                } else null
            }
        }

    companion object {
        fun generateProtocol(): String {
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            return "VIST-${sdf.format(Date())}"
        }
    }
}
