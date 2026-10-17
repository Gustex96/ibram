package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AnimalSpecies
import com.example.data.model.AuditorProfile
import com.example.data.model.HorseInspection
import com.example.data.model.Tutor
import com.example.util.DatabaseBackupManager
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InspectionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.inspectionDao()

    val inspections: StateFlow<List<HorseInspection>> = dao.getAllInspections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Species selected initially
    private val _selectedSpecies = MutableStateFlow(AnimalSpecies.CAVALO)
    val selectedSpecies: StateFlow<AnimalSpecies> = _selectedSpecies.asStateFlow()

    private val _customSpeciesName = MutableStateFlow("")
    val customSpeciesName: StateFlow<String> = _customSpeciesName.asStateFlow()

    // Auditor Profile
    private val _auditorProfile = MutableStateFlow(
        AuditorProfile(
            name = "Auditor Fiscal de Campo",
            registrationNumber = "DFL-2024",
            agency = "DF Legal",
            administrativeRegion = "Plano Piloto"
        )
    )
    val auditorProfile: StateFlow<AuditorProfile> = _auditorProfile.asStateFlow()

    // Draft Inspection State
    private val _draftSpecies = MutableStateFlow(AnimalSpecies.CAVALO)
    val draftSpecies: StateFlow<AnimalSpecies> = _draftSpecies.asStateFlow()

    private val _draftCustomSpecies = MutableStateFlow("")
    val draftCustomSpecies: StateFlow<String> = _draftCustomSpecies.asStateFlow()

    val draftAnimalCount = MutableStateFlow(1)
    val draftMistreatedCount = MutableStateFlow(0)
    val draftAdministrativeRegion = MutableStateFlow("Plano Piloto")
    val draftQuadra = MutableStateFlow("")
    val draftConjunto = MutableStateFlow("")
    val draftNumero = MutableStateFlow("")
    val draftLatitude = MutableStateFlow(-15.7975)
    val draftLongitude = MutableStateFlow(-47.8919)
    val draftPhotos = MutableStateFlow<List<String>>(emptyList())
    val draftNotes = MutableStateFlow("")
    val draftDescription = MutableStateFlow("")
    val draftOtherAgencies = MutableStateFlow("")
    val draftTutors = MutableStateFlow<List<Tutor>>(emptyList())
    val draftMistreatments = MutableStateFlow<Set<String>>(emptySet())
    val draftAdequates = MutableStateFlow<Set<String>>(emptySet())
    val draftBodyCondition = MutableStateFlow("3 - Ideal (Nutrição Adequada)")
    val draftHydration = MutableStateFlow("Normal (Turgor cutâneo < 2s)")
    val draftPosture = MutableStateFlow("Alerta e Ativo")
    val draftHoofDental = MutableStateFlow("Cascos íntegros / sem lesões")

    val speciesDisplay: String
        get() {
            return if (_selectedSpecies.value == AnimalSpecies.OUTROS && _customSpeciesName.value.isNotBlank()) {
                _customSpeciesName.value
            } else {
                _selectedSpecies.value.title
            }
        }

    fun selectSpecies(species: AnimalSpecies, customName: String = "") {
        _selectedSpecies.value = species
        _customSpeciesName.value = customName
        _draftSpecies.value = species
        _draftCustomSpecies.value = customName
    }

    fun updateAuditorProfile(profile: AuditorProfile) {
        _auditorProfile.value = profile
    }

    fun addDraftPhoto(path: String) {
        val current = draftPhotos.value.toMutableList()
        if (!current.contains(path)) {
            current.add(path)
            draftPhotos.value = current
        }
    }

    fun removeDraftPhoto(path: String) {
        val current = draftPhotos.value.toMutableList()
        current.remove(path)
        draftPhotos.value = current
    }

    fun addTutor(name: String, cpf: String) {
        if (name.isNotBlank() || cpf.isNotBlank()) {
            val current = draftTutors.value.toMutableList()
            current.add(Tutor(name.trim(), cpf.trim()))
            draftTutors.value = current
        }
    }

    fun removeTutor(index: Int) {
        val current = draftTutors.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            draftTutors.value = current
        }
    }

    fun toggleMistreatment(indicator: String) {
        val current = draftMistreatments.value.toMutableSet()
        if (current.contains(indicator)) current.remove(indicator) else current.add(indicator)
        draftMistreatments.value = current
    }

    fun toggleAdequate(indicator: String) {
        val current = draftAdequates.value.toMutableSet()
        if (current.contains(indicator)) current.remove(indicator) else current.add(indicator)
        draftAdequates.value = current
    }

    fun saveCurrentInspection(onSuccess: (HorseInspection) -> Unit) {
        viewModelScope.launch {
            val profile = _auditorProfile.value
            val photos = draftPhotos.value
            val mainPhoto = photos.firstOrNull() ?: ""
            val extras = if (photos.size > 1) photos.drop(1).joinToString(";;;") else ""

            val tutorsStr = draftTutors.value.joinToString("###") { "${it.name}|||${it.cpf}" }
            val mistreatStr = draftMistreatments.value.joinToString(";;;")
            val adequateStr = draftAdequates.value.joinToString(";;;")

            val now = System.currentTimeMillis()
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
            val formattedDate = sdf.format(Date(now))

            val insp = HorseInspection(
                protocolNumber = HorseInspection.generateProtocol(),
                species = _draftSpecies.value.title,
                customSpeciesName = _draftCustomSpecies.value,
                speciesEmoji = _draftSpecies.value.emoji,
                horseCount = draftAnimalCount.value,
                mistreatedHorseCount = draftMistreatedCount.value,
                auditorName = profile.name,
                auditorBadge = profile.registrationNumber,
                auditorAgency = profile.agency,
                administrativeRegion = draftAdministrativeRegion.value,
                quadra = draftQuadra.value,
                conjunto = draftConjunto.value,
                numero = draftNumero.value,
                latitude = draftLatitude.value,
                longitude = draftLongitude.value,
                captureTimestamp = now,
                formattedCaptureDate = formattedDate,
                photoPath = mainPhoto,
                additionalPhotoPathsJson = extras,
                imageNotes = draftNotes.value,
                horseDescription = draftDescription.value,
                otherAgencyNotes = draftOtherAgencies.value,
                tutorsJson = tutorsStr,
                mistreatmentIndicatorsJson = mistreatStr,
                adequateWelfareIndicatorsJson = adequateStr,
                bodyConditionScore = draftBodyCondition.value,
                hydrationStatus = draftHydration.value,
                postureBehavior = draftPosture.value,
                hoofDentalStatus = draftHoofDental.value,
                createdAt = now
            )

            val id = dao.insertInspection(insp)
            val saved = insp.copy(id = id)

            // Reset draft
            draftPhotos.value = emptyList()
            draftNotes.value = ""
            draftDescription.value = ""
            draftOtherAgencies.value = ""
            draftTutors.value = emptyList()
            draftMistreatments.value = emptySet()
            draftAdequates.value = emptySet()
            draftQuadra.value = ""
            draftConjunto.value = ""
            draftNumero.value = ""

            onSuccess(saved)
        }
    }

    fun deleteInspection(inspection: HorseInspection) {
        viewModelScope.launch {
            dao.deleteInspection(inspection)
        }
    }

    fun generatePdf(context: Context, inspection: HorseInspection): File? {
        return PdfReportGenerator.generateSingleInspectionPdf(context, inspection)
    }

    suspend fun exportBackup(context: Context): File {
        return DatabaseBackupManager.exportBackup(context, inspections.value)
    }

    suspend fun importBackup(context: Context, uri: Uri): Int {
        return DatabaseBackupManager.importBackup(context, uri)
    }
}
