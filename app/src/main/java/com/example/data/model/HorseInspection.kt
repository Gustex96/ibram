package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "horse_inspections")
data class HorseInspection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val protocolNumber: String,
    val photoPath: String,
    // Caminhos de fotografias adicionais anexadas/tiradas separadas por ;;
    val additionalPhotoPaths: String = "",
    // Geolocalização (latitude / longitude)
    val latitude: Double,
    val longitude: Double,
    val captureTimestamp: Long = System.currentTimeMillis(),
    val administrativeRegion: String,
    // Endereço da Ação Fiscal: RA, Quadra, Conjunto e Número
    val quadra: String = "",
    val conjunto: String = "",
    val numero: String = "",
    // Descrição do cavalo (pelagem, porte, ferimentos, características físicas)
    val horseDescription: String = "",
    val horseCount: Int = 1,
    // Quantidade de cavalos que apresentam possíveis sinais de maus-tratos
    val mistreatedHorseCount: Int = 0,
    // Indicadores técnicos de maus-tratos (Resolução CFMV 1.236/2018 e CRMV-MS) separados por ;;
    val mistreatmentIndicators: String = "",
    // Indicadores técnicos de condições adequadas / bem-estar assegurado separados por ;;
    val adequateIndicators: String = "",
    // Providências Operacionais: Apreensão pela SEAGRI-DF
    val requiresSeagriApprehension: Boolean = false,
    val seagriNotes: String = "",
    // Providências Operacionais: Apoio da Polícia Militar (PMDF)
    val requiresPmdfSupport: Boolean = false,
    val pmdfNotes: String = "",
    // Avaliação de riscos de fiscalização ambiental (Escala 1 a 5 e parâmetros de fiscalização ambiental)
    val riskLevel: Int = 1, // 1 to 5
    val riskJustification: String = "",
    val safetyRiskAssessment: String = "",
    val imageNotes: String = "",
    val tutorName: String = "",
    val tutorCpf: String = "",
    // Tutores e CPFs adicionais no formato Nome|CPF separados por ;;
    val additionalTutors: String = "",
    // Equipe responsável pela diligência
    val inspectionTeam: String = "Equipe de Apoio em Fiscalização",
    val createdAt: Long = System.currentTimeMillis()
) {
    val teamDisplay: String
        get() = if (inspectionTeam.isNotBlank()) inspectionTeam else "Equipe de Apoio em Fiscalização"

    val formattedAddress: String
        get() {
            val parts = mutableListOf<String>()
            if (administrativeRegion.isNotBlank()) parts.add(administrativeRegion)
            if (quadra.isNotBlank()) parts.add("Q. $quadra")
            if (conjunto.isNotBlank()) parts.add("Conj. $conjunto")
            if (numero.isNotBlank()) parts.add("Nº $numero")
            return if (parts.isNotEmpty()) parts.joinToString(", ") else administrativeRegion
        }

    val residentialAddress: String
        get() {
            val parts = mutableListOf<String>()
            if (quadra.isNotBlank()) parts.add("Quadra $quadra")
            if (conjunto.isNotBlank()) parts.add("Conjunto $conjunto")
            if (numero.isNotBlank()) parts.add("Nº $numero")
            return if (parts.isNotEmpty()) parts.joinToString(", ") else "Não informado / Sem complemento"
        }

    val hasResidentialAddress: Boolean
        get() = quadra.isNotBlank() || conjunto.isNotBlank() || numero.isNotBlank()

    val formattedCaptureDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
            return sdf.format(Date(captureTimestamp))
        }

    val formattedShortDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            return sdf.format(Date(captureTimestamp))
        }

    val riskDescription: String
        get() = when (riskLevel) {
            1 -> "1 - Muito Baixo (Área rural cercada, sem tráfego próximo)"
            2 -> "2 - Baixo (Área semiurbana, baixa velocidade, animais contidos)"
            3 -> "3 - Moderado (Próximo a vias coletoras, cercamento frágil)"
            4 -> "4 - Alto (Margens de rodovias do DF - EPTG/EPIA/Estrutural)"
            5 -> "5 - Crítico / Iminente (Animais na pista / risco fatal iminente)"
            else -> "Risco $riskLevel"
        }

    val riskBadgeTitle: String
        get() = when (riskLevel) {
            1 -> "Muito Baixo"
            2 -> "Baixo"
            3 -> "Moderado"
            4 -> "Alto"
            5 -> "Crítico"
            else -> "Nível $riskLevel"
        }

    val hasMistreatmentSigns: Boolean
        get() = mistreatedHorseCount > 0

    val mistreatmentList: List<String>
        get() = if (mistreatmentIndicators.isBlank()) emptyList() else mistreatmentIndicators.split(";;").map { it.trim() }.filter { it.isNotEmpty() }

    val adequateList: List<String>
        get() = if (adequateIndicators.isBlank()) emptyList() else adequateIndicators.split(";;").map { it.trim() }.filter { it.isNotEmpty() }

    val allPhotoPaths: List<String>
        get() {
            val list = mutableListOf<String>()
            if (photoPath.isNotBlank()) list.add(photoPath)
            if (additionalPhotoPaths.isNotBlank()) {
                list.addAll(additionalPhotoPaths.split(";;").map { it.trim() }.filter { it.isNotEmpty() })
            }
            return list
        }

    val allTutors: List<TutorEntry>
        get() {
            val list = mutableListOf<TutorEntry>()
            if (tutorName.isNotBlank() || tutorCpf.isNotBlank()) {
                list.add(TutorEntry(tutorName.trim(), tutorCpf.trim()))
            }
            if (additionalTutors.isNotBlank()) {
                additionalTutors.split(";;").forEach { item ->
                    val parts = item.split("|")
                    val n = parts.getOrNull(0)?.trim() ?: ""
                    val c = parts.getOrNull(1)?.trim() ?: ""
                    if (n.isNotEmpty() || c.isNotEmpty()) {
                        list.add(TutorEntry(n, c))
                    }
                }
            }
            return list
        }

    val tutorsSummary: String
        get() {
            val tutors = allTutors
            if (tutors.isEmpty()) return "Não informado"
            return tutors.joinToString(" • ") { t ->
                if (t.cpf.isNotBlank()) "${t.name} (${t.cpf})" else t.name
            }
        }
}

data class TutorEntry(
    val name: String = "",
    val cpf: String = ""
)
