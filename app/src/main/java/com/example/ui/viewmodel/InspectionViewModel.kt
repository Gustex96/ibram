package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.DfConstants
import com.example.data.model.HorseInspection
import com.example.data.model.TutorEntry
import com.example.data.repository.InspectionRepository
import com.example.ui.components.AnimalSpecies
import com.example.ui.theme.VisualComfortMode
import com.example.util.AppVersionInfo
import com.example.util.CpfValidator
import com.example.util.LocationHelper
import com.example.util.PdfReportGenerator
import com.example.util.UpdateCheckResult
import com.example.util.UpdateChecker
import com.example.util.WatermarkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

data class InspectionDraftState(
    val photoUri: Uri? = null,
    val watermarkedPhotoPath: String? = null,
    val photoUris: List<Uri> = emptyList(),
    val watermarkedPhotoPaths: List<String> = emptyList(),
    val latitude: Double = DfConstants.DEFAULT_DF_LATITUDE,
    val longitude: Double = DfConstants.DEFAULT_DF_LONGITUDE,
    val captureTimestamp: Long = System.currentTimeMillis(),
    val administrativeRegion: String = "Plano Piloto (RA I)",
    val quadra: String = "",
    val conjunto: String = "",
    val numero: String = "",
    val species: String = "Cavalo",
    val auditorName: String = "",
    val horseDescription: String = "",
    val horseCount: Int = 1,
    val mistreatedHorseCount: Int = 0,
    val mistreatmentIndicators: List<String> = emptyList(),
    val adequateIndicators: List<String> = emptyList(),
    val requiresSeagriApprehension: Boolean = false,
    val seagriNotes: String = "",
    val requiresPmdfSupport: Boolean = false,
    val pmdfNotes: String = "",
    val requiresOtherAgencySupport: Boolean = false,
    val otherAgencyName: String = "",
    val otherAgencyNotes: String = "",
    val riskLevel: Int = 3,
    val safetyRiskAssessment: String = "",
    val imageNotes: String = "",
    val tutors: List<TutorEntry> = listOf(TutorEntry("", "")),
    val isWatermarking: Boolean = false,
    val isSaving: Boolean = false,
    val protocolNumber: String = generateProtocol(),
    val ppbeaDiagnosis: com.example.data.model.PpbeaDiagnosis? = null,
    val individualEvaluations: List<com.example.data.model.IndividualAnimalEvaluation> = emptyList(),
) {
    val isDog: Boolean
        get() = DfConstants.isDogSpecies(species)

    val speciesDisplay: String
        get() = if (isDog) "Cachorro (Canino)" else "Cavalo"

    val speciesEmoji: String
        get() = if (isDog) "🐕" else "🐎"
    val tutorName: String
        get() = tutors.firstOrNull()?.name ?: ""

    val tutorCpf: String
        get() = tutors.firstOrNull()?.cpf ?: ""
    val allPhotoPaths: List<String>
        get() {
            val list = mutableListOf<String>()
            watermarkedPhotoPaths.forEach { if (it.isNotBlank() && !list.contains(it)) list.add(it) }
            if (!watermarkedPhotoPath.isNullOrBlank() && !list.contains(watermarkedPhotoPath)) {
                list.add(0, watermarkedPhotoPath)
            }
            return list
        }

    companion object {
        fun generateProtocol(): String {
            val randomNum = Random.nextInt(1000, 9999)
            return "DF-MAUS-TRATOS-2026-$randomNum"
        }
    }
}

class InspectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InspectionRepository
    private val locationHelper: LocationHelper

    // Identificação do Auditor Fiscal (persistida localmente em SharedPreferences)
    var auditorIdentification by mutableStateOf("")
        private set

    // Espécie selecionada no painel principal ("Cavalo" ou "Cachorro")
    var selectedSpecies by mutableStateOf(AnimalSpecies.HORSE)

    val draft = MutableStateFlow(InspectionDraftState())

    init {
        val db = AppDatabase.getDatabase(application)
        repository = InspectionRepository(db.horseInspectionDao())
        locationHelper = LocationHelper(application)

        val levoPrefs = application.getSharedPreferences("levo_auditor_prefs", Context.MODE_PRIVATE)
        val oldPrefs = application.getSharedPreferences("equidf_auditor_prefs", Context.MODE_PRIVATE)
        auditorIdentification = levoPrefs.getString("key_auditor_id", null)
            ?: oldPrefs.getString("key_auditor_id", "") ?: ""
        if (auditorIdentification.isNotBlank()) {
            draft.value = draft.value.copy(auditorName = auditorIdentification)
        }
    }

    fun setAuditorId(id: String) {
        val clean = id.trim()
        auditorIdentification = clean
        val prefs = getApplication<Application>().getSharedPreferences("levo_auditor_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("key_auditor_id", clean).apply()
        draft.value = draft.value.copy(auditorName = clean)
    }

    var customSpeciesName by mutableStateOf<String?>(null)
        private set

    fun selectSpecies(species: AnimalSpecies) {
        selectedSpecies = species
        if (species != AnimalSpecies.OUTROS) {
            customSpeciesName = null
        }
        val targetName = customSpeciesName ?: species.displayName
        selectedSpeciesFilter.value = targetName
        draft.value = draft.value.copy(species = targetName)
    }

    fun setCustomSpecies(name: String) {
        val clean = name.trim()
        if (clean.isNotBlank()) {
            customSpeciesName = clean
            selectedSpecies = AnimalSpecies.OUTROS
            selectedSpeciesFilter.value = clean
            draft.value = draft.value.copy(species = clean)
        }
    }

    fun setSpeciesFilter(filterName: String?) {
        selectedSpeciesFilter.value = filterName
    }

    val allInspections: StateFlow<List<HorseInspection>> = repository.allInspections
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var activeScreenName by mutableStateOf("MAIN")
    var activeDetailId by mutableStateOf<Long?>(null)

    // Visual Comfort Mode (Claro / Sépia para sensibilidade dos olhos / Escuro)
    var visualComfortMode by mutableStateOf(VisualComfortMode.LIGHT)

    fun setVisualComfort(mode: VisualComfortMode) {
        visualComfortMode = mode
    }

    fun toggleVisualComfort() {
        visualComfortMode = when (visualComfortMode) {
            VisualComfortMode.LIGHT -> VisualComfortMode.EYE_CARE
            VisualComfortMode.EYE_CARE -> VisualComfortMode.DARK
            VisualComfortMode.DARK -> VisualComfortMode.LIGHT
        }
    }

    fun navigateTo(screen: String, detailId: Long? = null) {
        if (screen == "NEW_INSPECTION") {
            val targetName = if (selectedSpecies == AnimalSpecies.OUTROS && !customSpeciesName.isNullOrBlank()) {
                customSpeciesName!!
            } else {
                selectedSpecies.displayName
            }
            draft.value = draft.value.copy(
                species = targetName,
                auditorName = auditorIdentification.ifBlank { draft.value.auditorName }
            )
        }
        activeScreenName = screen
        activeDetailId = detailId
    }

    // Gerenciamento de Atualização do App (version.json exclusivo na raiz do GitHub)
    var availableUpdate by mutableStateOf<AppVersionInfo?>(null)
        private set
    var isCheckingUpdate by mutableStateOf(value = false)
        private set
    var updateCheckResult by mutableStateOf<UpdateCheckResult?>(null)
        private set
    var showAppUpdateDialog by mutableStateOf(false)
        private set
    var showUpdateBanner by mutableStateOf(true)
        private set
    var showUpdateFeedbackDialog by mutableStateOf(false)

    fun openUpdateDialog() {
        showAppUpdateDialog = true
    }

    fun dismissUpdateDialog() {
        showAppUpdateDialog = false
    }

    fun dismissUpdateBanner() {
        showUpdateBanner = false
    }

    fun dismissUpdateFeedbackDialog() {
        showUpdateFeedbackDialog = false
    }

    fun checkForUpdates(isManualCheck: Boolean = false, customUrl: String? = null) {
        viewModelScope.launch {
            isCheckingUpdate = true
            try {
                val result = UpdateChecker.checkUpdateWithResult(getApplication(), customUrl)
                updateCheckResult = result
                when (result) {
                    is UpdateCheckResult.UpdateAvailable -> {
                        availableUpdate = result.info
                        showAppUpdateDialog = true
                        showUpdateBanner = true
                    }
                    is UpdateCheckResult.AlreadyUpToDate -> {
                        if (isManualCheck) {
                            showUpdateFeedbackDialog = true
                        }
                    }
                    is UpdateCheckResult.Error -> {
                        if (isManualCheck) {
                            showUpdateFeedbackDialog = true
                        }
                    }
                }
            } finally {
                isCheckingUpdate = false
            }
        }
    }

    fun saveCustomUpdateUrl(url: String) {
        UpdateChecker.setVersionUrl(getApplication(), url)
        checkForUpdates(isManualCheck = true, customUrl = url)
    }

    fun resetUpdateUrlToDefault() {
        UpdateChecker.resetVersionUrl(getApplication())
        checkForUpdates(isManualCheck = true)
    }

    fun simulateUpdateCheck() {
        val sample = UpdateChecker.getSampleVersionInfo(getApplication())
        availableUpdate = sample
        showAppUpdateDialog = true
        showUpdateBanner = true
        showUpdateFeedbackDialog = false
    }

    val searchQuery = MutableStateFlow("")
    val selectedRiskFilter = MutableStateFlow<Int?>(null) // null = all
    val selectedRaFilter = MutableStateFlow<String?>(null)
    val selectedSpeciesFilter = MutableStateFlow<String?>("Cavalo")

    // PDF Report Generator Hub Filters
    val reportStartDate = MutableStateFlow<Long?>(null)
    val reportEndDate = MutableStateFlow<Long?>(null)
    val reportSelectedRa = MutableStateFlow<String?>(null)

    val filteredInspections: StateFlow<List<HorseInspection>> = combine(
        allInspections,
        searchQuery,
        selectedRiskFilter,
        selectedRaFilter,
        selectedSpeciesFilter
    ) { list, query, risk, ra, speciesFilter ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.administrativeRegion.contains(query, ignoreCase = true) ||
                item.tutorName.contains(query, ignoreCase = true) ||
                item.protocolNumber.contains(query, ignoreCase = true) ||
                item.imageNotes.contains(query, ignoreCase = true) ||
                item.horseDescription.contains(query, ignoreCase = true) ||
                item.safetyRiskAssessment.contains(query, ignoreCase = true) ||
                item.tutorCpf.contains(query, ignoreCase = true) ||
                item.species.contains(query, ignoreCase = true) ||
                item.auditorName.contains(query, ignoreCase = true)

            val matchesRisk = (risk == null) || (item.riskLevel == risk)
            val matchesRa = ra == null || item.administrativeRegion == ra

            val matchesSpecies = if (speciesFilter == null || speciesFilter == "Todos") {
                true
            } else if (DfConstants.isDogSpecies(speciesFilter)) {
                item.isDog
            } else {
                !item.isDog
            }

            matchesQuery && matchesRisk && matchesRa && matchesSpecies
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered list for PDF Export
    val reportPreviewInspections: StateFlow<List<HorseInspection>> = combine(
        allInspections,
        reportStartDate,
        reportEndDate,
        reportSelectedRa
    ) { list, start, end, ra ->
        list.filter { item ->
            val matchesStart = start == null || item.captureTimestamp >= start
            // end date should include full day up to 23:59:59
            val matchesEnd = end == null || item.captureTimestamp <= (end + 86400000L - 1L)
            val matchesRa = ra == null || ra == "Todas as RAs" || item.administrativeRegion == ra
            matchesStart && matchesEnd && matchesRa
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateCoordinates(latitude: Double, longitude: Double) {
        val detectedRa = DfConstants.detectClosestRa(latitude, longitude)
        draft.value = draft.value.copy(
            latitude = latitude,
            longitude = longitude,
            administrativeRegion = detectedRa
        )
        reapplyWatermarkIfPossible()
    }

    fun fetchCurrentLocation(hasPermission: Boolean) {
        viewModelScope.launch {
            val coords = locationHelper.getCurrentCoordinates(hasPermission)
            updateCoordinates(coords.latitude, coords.longitude)
        }
    }

    fun addDraftPhotoUri(
        uri: Uri,
        exactLatitude: Double? = null,
        exactLongitude: Double? = null,
        exactTimestampMillis: Long? = null,
        exactRa: String? = null,
        exemplarIndex: Int? = null,
        photoTag: String? = null,
        orientationDegrees: Int = 0
    ) {
        val currentDraft = draft.value
        val captureLat = exactLatitude ?: currentDraft.latitude
        val captureLng = exactLongitude ?: currentDraft.longitude
        val captureTime = exactTimestampMillis ?: System.currentTimeMillis()
        val captureRa = exactRa ?: if (exactLatitude != null && exactLongitude != null) {
            DfConstants.detectClosestRa(exactLatitude, exactLongitude)
        } else {
            currentDraft.administrativeRegion
        }
        val orderIndex = exemplarIndex ?: (currentDraft.allPhotoPaths.size + 1)

        if (exactLatitude != null && exactLongitude != null) {
            draft.value = currentDraft.copy(
                latitude = exactLatitude,
                longitude = exactLongitude,
                captureTimestamp = captureTime,
                administrativeRegion = captureRa,
                isWatermarking = true
            )
        } else {
            draft.value = currentDraft.copy(isWatermarking = true)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val watermarked = WatermarkUtils.applyWatermarkAndSave(
                context = getApplication(),
                inputUri = uri,
                latitude = captureLat,
                longitude = captureLng,
                administrativeRegion = captureRa,
                protocol = currentDraft.protocolNumber,
                captureTimeMillis = captureTime,
                exemplarIndex = orderIndex,
                photoTag = photoTag,
                deviceOrientationDegrees = orientationDegrees
            )

            withContext(Dispatchers.Main) {
                if (watermarked != null) {
                    val updatedList = draft.value.watermarkedPhotoPaths.toMutableList()
                    if (!updatedList.contains(watermarked.absolutePath)) {
                        updatedList.add(watermarked.absolutePath)
                    }
                    val updatedUris = draft.value.photoUris.toMutableList()
                    if (!updatedUris.contains(uri)) {
                        updatedUris.add(uri)
                    }
                    draft.value = draft.value.copy(
                        photoUri = updatedUris.firstOrNull(),
                        photoUris = updatedUris,
                        watermarkedPhotoPath = updatedList.firstOrNull(),
                        watermarkedPhotoPaths = updatedList,
                        isWatermarking = false
                    )
                } else {
                    draft.value = draft.value.copy(isWatermarking = false)
                }
            }
        }
    }

    fun addDraftPhotoUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val currentDraft = draft.value
        draft.value = currentDraft.copy(isWatermarking = true)
        val initialOrder = currentDraft.allPhotoPaths.size

        viewModelScope.launch(Dispatchers.IO) {
            val newWatermarkedPaths = mutableListOf<String>()
            uris.forEachIndexed { idx, uri ->
                val captureTime = System.currentTimeMillis()
                val watermarked = WatermarkUtils.applyWatermarkAndSave(
                    context = getApplication(),
                    inputUri = uri,
                    latitude = currentDraft.latitude,
                    longitude = currentDraft.longitude,
                    administrativeRegion = currentDraft.administrativeRegion,
                    protocol = currentDraft.protocolNumber,
                    captureTimeMillis = captureTime,
                    exemplarIndex = initialOrder + idx + 1
                )
                if (watermarked != null) {
                    newWatermarkedPaths.add(watermarked.absolutePath)
                }
            }

            withContext(Dispatchers.Main) {
                val updatedPaths = draft.value.watermarkedPhotoPaths.toMutableList()
                newWatermarkedPaths.forEach { if (!updatedPaths.contains(it)) updatedPaths.add(it) }

                val updatedUris = draft.value.photoUris.toMutableList()
                uris.forEach { if (!updatedUris.contains(it)) updatedUris.add(it) }

                draft.value = draft.value.copy(
                    photoUri = updatedUris.firstOrNull(),
                    photoUris = updatedUris,
                    watermarkedPhotoPath = updatedPaths.firstOrNull(),
                    watermarkedPhotoPaths = updatedPaths,
                    isWatermarking = false
                )
            }
        }
    }

    fun removeDraftPhoto(path: String) {
        val updatedPaths = draft.value.watermarkedPhotoPaths.filter { it != path }
        draft.value = draft.value.copy(
            watermarkedPhotoPath = updatedPaths.firstOrNull(),
            watermarkedPhotoPaths = updatedPaths
        )
    }

    fun setDraftPhotoUri(uri: Uri) {
        addDraftPhotoUri(uri)
    }

    fun setDraftCapturedBitmap(bitmap: Bitmap) {
        val currentDraft = draft.value
        val captureTime = System.currentTimeMillis()
        draft.value = currentDraft.copy(
            captureTimestamp = captureTime,
            isWatermarking = true
        )

        viewModelScope.launch(Dispatchers.IO) {
            val watermarked = WatermarkUtils.applyWatermarkToBitmap(
                context = getApplication(),
                source = bitmap,
                latitude = currentDraft.latitude,
                longitude = currentDraft.longitude,
                administrativeRegion = currentDraft.administrativeRegion,
                protocol = currentDraft.protocolNumber,
                captureTimeMillis = captureTime
            )

            withContext(Dispatchers.Main) {
                if (watermarked != null) {
                    val updatedList = draft.value.watermarkedPhotoPaths.toMutableList()
                    if (!updatedList.contains(watermarked.absolutePath)) {
                        updatedList.add(watermarked.absolutePath)
                    }
                    draft.value = draft.value.copy(
                        watermarkedPhotoPath = updatedList.firstOrNull(),
                        watermarkedPhotoPaths = updatedList,
                        isWatermarking = false
                    )
                } else {
                    draft.value = draft.value.copy(isWatermarking = false)
                }
            }
        }
    }

    fun updateAdministrativeRegion(ra: String) {
        draft.value = draft.value.copy(administrativeRegion = ra)
        reapplyWatermarkIfPossible()
    }

    fun updateQuadra(quadra: String) {
        draft.value = draft.value.copy(quadra = quadra)
    }

    fun updateConjunto(conjunto: String) {
        draft.value = draft.value.copy(conjunto = conjunto)
    }

    fun updateNumero(numero: String) {
        draft.value = draft.value.copy(numero = numero)
    }

    fun updateHorseCount(count: Int) {
        if (count >= 1) {
            val clampedMistreated = draft.value.mistreatedHorseCount.coerceAtMost(count)
            draft.value = draft.value.copy(
                horseCount = count,
                mistreatedHorseCount = clampedMistreated
            )
        }
    }

    fun updateMistreatedHorseCount(count: Int) {
        val validCount = count.coerceIn(0, draft.value.horseCount)
        val indicators = if (validCount == 0) emptyList() else draft.value.mistreatmentIndicators
        draft.value = draft.value.copy(
            mistreatedHorseCount = validCount,
            mistreatmentIndicators = indicators
        )
    }

    fun toggleMistreatmentIndicator(indicator: String) {
        val current = draft.value.mistreatmentIndicators.toMutableList()
        if (current.contains(indicator)) {
            current.remove(indicator)
        } else {
            current.add(indicator)
        }
        draft.value = draft.value.copy(mistreatmentIndicators = current)
    }

    fun setMistreatmentIndicators(indicators: List<String>) {
        draft.value = draft.value.copy(mistreatmentIndicators = indicators)
    }

    fun toggleAdequateIndicator(indicator: String) {
        val current = draft.value.adequateIndicators.toMutableList()
        if (current.contains(indicator)) {
            current.remove(indicator)
        } else {
            current.add(indicator)
        }
        draft.value = draft.value.copy(adequateIndicators = current)
    }

    fun setAdequateIndicators(indicators: List<String>) {
        draft.value = draft.value.copy(adequateIndicators = indicators)
    }

    fun clearAllMistreatmentAlerts() {
        draft.value = draft.value.copy(mistreatmentIndicators = emptyList())
    }

    fun clearAllAdequateIndicators() {
        draft.value = draft.value.copy(adequateIndicators = emptyList())
    }

    fun setPpbeaDiagnosis(diagnosis: com.example.data.model.PpbeaDiagnosis) {
        val newMistreatments = draft.value.mistreatmentIndicators.toMutableList()
        val newAdequates = draft.value.adequateIndicators.toMutableList()

        // Sincroniza indicadores derivados dos critérios respondidos no ANEXO D
        diagnosis.answersMap.forEach { (criterionId, isYes) ->
            val criterion = com.example.data.model.PpbeaCriteriaRepository.criteria.find { it.id == criterionId }
            if (criterion != null) {
                val isRuim = if (criterion.simIsRuim) isYes else !isYes
                val tag = "[${criterion.domain.title}] ${criterion.question.replace("?", "")}"
                if (isRuim) {
                    if (!newMistreatments.contains(tag)) newMistreatments.add(tag)
                    newAdequates.remove(tag)
                } else {
                    if (!newAdequates.contains(tag)) newAdequates.add(tag)
                    newMistreatments.remove(tag)
                }
            }
        }

        val updatedMistreatedCount = if (diagnosis.ocorrenciaMausTratos == com.example.data.model.EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS && draft.value.mistreatedHorseCount == 0) {
            draft.value.horseCount
        } else {
            draft.value.mistreatedHorseCount
        }

        draft.value = draft.value.copy(
            ppbeaDiagnosis = diagnosis,
            mistreatmentIndicators = newMistreatments,
            adequateIndicators = newAdequates,
            mistreatedHorseCount = updatedMistreatedCount
        )
    }

    fun clearPpbeaDiagnosis() {
        draft.value = draft.value.copy(ppbeaDiagnosis = null)
    }

    fun updateIndividualEvaluations(list: List<com.example.data.model.IndividualAnimalEvaluation>) {
        draft.value = draft.value.copy(individualEvaluations = list)
    }

    fun updateRequiresSeagriApprehension(required: Boolean) {
        draft.value = draft.value.copy(requiresSeagriApprehension = required)
    }

    fun updateSeagriNotes(notes: String) {
        draft.value = draft.value.copy(seagriNotes = notes)
    }

    fun updateRequiresPmdfSupport(required: Boolean) {
        draft.value = draft.value.copy(requiresPmdfSupport = required)
    }

    fun updatePmdfNotes(notes: String) {
        draft.value = draft.value.copy(pmdfNotes = notes)
    }

    fun updateRequiresOtherAgencySupport(required: Boolean) {
        draft.value = draft.value.copy(requiresOtherAgencySupport = required)
    }

    fun updateOtherAgencyName(name: String) {
        draft.value = draft.value.copy(otherAgencyName = name)
    }

    fun updateOtherAgencyNotes(notes: String) {
        draft.value = draft.value.copy(otherAgencyNotes = notes)
    }

    fun updateHorseDescription(desc: String) {
        draft.value = draft.value.copy(horseDescription = desc)
    }

    fun updateSafetyRiskAssessment(assessment: String) {
        draft.value = draft.value.copy(safetyRiskAssessment = assessment)
    }

    fun updateRiskLevel(risk: Int) {
        if (risk in 1..5) {
            draft.value = draft.value.copy(riskLevel = risk)
        }
    }

    fun updateImageNotes(notes: String) {
        draft.value = draft.value.copy(imageNotes = notes)
    }

    fun addTutor() {
        val list = draft.value.tutors.toMutableList()
        list.add(TutorEntry("", ""))
        draft.value = draft.value.copy(tutors = list)
    }

    fun removeTutor(index: Int) {
        val list = draft.value.tutors.toMutableList()
        if (list.size > 1 && index in list.indices) {
            list.removeAt(index)
            draft.value = draft.value.copy(tutors = list)
        } else if (list.size == 1 && index == 0) {
            draft.value = draft.value.copy(tutors = listOf(TutorEntry("", "")))
        }
    }

    fun updateTutor(index: Int, name: String, rawCpf: String) {
        val list = draft.value.tutors.toMutableList()
        if (index in list.indices) {
            val formatted = CpfValidator.format(rawCpf)
            list[index] = TutorEntry(name, formatted)
            draft.value = draft.value.copy(tutors = list)
        }
    }

    fun updateTutorName(name: String) {
        updateTutor(0, name, draft.value.tutors.firstOrNull()?.cpf ?: "")
    }

    fun updateTutorCpf(rawCpf: String) {
        updateTutor(0, draft.value.tutors.firstOrNull()?.name ?: "", rawCpf)
    }

    fun updateDraftSpecies(speciesName: String) {
        val clean = speciesName.trim()
        val mapped = if (DfConstants.isDogSpecies(clean)) AnimalSpecies.DOG else AnimalSpecies.HORSE
        selectedSpecies = mapped
        selectedSpeciesFilter.value = clean
        draft.value = draft.value.copy(
            species = clean,
            mistreatmentIndicators = emptyList(),
            adequateIndicators = emptyList()
        )
    }

    fun updateDraftAuditorName(name: String) {
        draft.value = draft.value.copy(auditorName = name)
    }

    private fun reapplyWatermarkIfPossible() {
        val current = draft.value
        val uri = current.photoUri ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val watermarked = WatermarkUtils.applyWatermarkAndSave(
                context = getApplication(),
                inputUri = uri,
                latitude = current.latitude,
                longitude = current.longitude,
                administrativeRegion = current.administrativeRegion,
                protocol = current.protocolNumber,
                captureTimeMillis = current.captureTimestamp
            )
            withContext(Dispatchers.Main) {
                draft.value = draft.value.copy(
                    watermarkedPhotoPath = watermarked?.absolutePath
                )
            }
        }
    }

    fun saveDraft(
        onSuccess: (HorseInspection) -> Unit,
        onError: (String) -> Unit
    ) {
        val current = draft.value

        if (current.watermarkedPhotoPath == null) {
            generateFallbackFieldPhotoAndSave(onSuccess, onError)
            return
        }

        viewModelScope.launch {
            try {
                draft.value = draft.value.copy(isSaving = true)

                val riskInfo = DfConstants.RISK_LEVELS.find { it.level == current.riskLevel }
                val justification = riskInfo?.summary ?: "Risco nível ${current.riskLevel}"
                val safetyAssessment = current.safetyRiskAssessment.ifBlank { justification }

                val allPhotos = current.allPhotoPaths
                val primaryPhoto = allPhotos.firstOrNull() ?: current.watermarkedPhotoPath ?: ""
                val additionalPhotos = if (allPhotos.size > 1) allPhotos.drop(1).joinToString(";;") else ""

                val validTutors = current.tutors.filter { it.name.isNotBlank() || it.cpf.isNotBlank() }
                val primTutor = validTutors.firstOrNull() ?: current.tutors.firstOrNull() ?: TutorEntry("", "")
                val addTutors = if (validTutors.size > 1) {
                    validTutors.drop(1).joinToString(";;") { "${it.name}|${it.cpf}" }
                } else ""

                val savedSpecies = current.species.ifBlank { selectedSpecies.displayName }
                val savedAuditor = current.auditorName.ifBlank { auditorIdentification }

                val inspection = HorseInspection(
                    protocolNumber = current.protocolNumber,
                    photoPath = primaryPhoto,
                    additionalPhotoPaths = additionalPhotos,
                    latitude = current.latitude,
                    longitude = current.longitude,
                    captureTimestamp = current.captureTimestamp,
                    administrativeRegion = current.administrativeRegion,
                    quadra = current.quadra,
                    conjunto = current.conjunto,
                    numero = current.numero,
                    species = savedSpecies,
                    auditorName = savedAuditor,
                    horseDescription = current.horseDescription,
                    horseCount = current.horseCount,
                    mistreatedHorseCount = current.mistreatedHorseCount,
                    mistreatmentIndicators = current.mistreatmentIndicators.joinToString(";;"),
                    adequateIndicators = current.adequateIndicators.joinToString(";;"),
                    requiresSeagriApprehension = current.requiresSeagriApprehension,
                    seagriNotes = current.seagriNotes,
                    requiresPmdfSupport = current.requiresPmdfSupport,
                    pmdfNotes = current.pmdfNotes,
                    requiresOtherAgencySupport = current.requiresOtherAgencySupport,
                    otherAgencyName = current.otherAgencyName,
                    otherAgencyNotes = current.otherAgencyNotes,
                    riskLevel = current.riskLevel,
                    riskJustification = justification,
                    safetyRiskAssessment = safetyAssessment,
                    imageNotes = current.imageNotes,
                    ppbeaAnswersData = com.example.data.model.PpbeaCriteriaRepository.mapToAnswersString(current.ppbeaDiagnosis?.answersMap ?: emptyMap()),
                    individualEvaluationsData = com.example.data.model.IndividualAnimalSerializer.serializeList(current.individualEvaluations),
                    tutorName = primTutor.name,
                    tutorCpf = primTutor.cpf,
                    additionalTutors = addTutors
                )

                val insertedId = repository.insertInspection(inspection)
                val savedInspection = inspection.copy(id = insertedId)

                resetDraft()
                onSuccess(savedInspection)
            } catch (e: Exception) {
                draft.value = draft.value.copy(isSaving = false)
                onError("Erro ao salvar fiscalização: ${e.message}")
            }
        }
    }

    private fun generateFallbackFieldPhotoAndSave(
        onSuccess: (HorseInspection) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = draft.value
            val width = 1280
            val height = 960
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val paint = Paint().apply {
                color = Color.rgb(30, 58, 47)
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            val textPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                textSize = 36f
                isAntiAlias = true
            }
            canvas.drawText("LEVANTAMENTO DE CAMPO • APOIO À FISCALIZAÇÃO", 80f, 300f, textPaint)
            textPaint.textSize = 28f
            val speciesText = if (current.isDog) "Área urbana / Bem-estar canino (Cães)" else "Área de pastoreio / Contenção de equinos"
            canvas.drawText(speciesText, 80f, 360f, textPaint)
            canvas.drawText("RA: ${current.administrativeRegion}", 80f, 420f, textPaint)
            val countLabel = if (current.isDog) "Cães contabilizados: ${current.horseCount}" else "Cavalos contabilizados: ${current.horseCount}"
            canvas.drawText(countLabel, 80f, 480f, textPaint)

            val watermarked = WatermarkUtils.applyWatermarkToBitmap(
                context = getApplication(),
                source = bitmap,
                latitude = current.latitude,
                longitude = current.longitude,
                administrativeRegion = current.administrativeRegion,
                protocol = current.protocolNumber,
                captureTimeMillis = current.captureTimestamp
            )

            withContext(Dispatchers.Main) {
                if (watermarked != null) {
                    val riskLevels = DfConstants.getRiskLevels(current.species)
                    val riskInfo = riskLevels.find { it.level == current.riskLevel }
                    val justification = riskInfo?.summary ?: "Risco nível ${current.riskLevel}"
                    val safetyAssessment = current.safetyRiskAssessment.ifBlank { justification }

                    val validTutors = current.tutors.filter { it.name.isNotBlank() || it.cpf.isNotBlank() }
                    val primTutor = validTutors.firstOrNull() ?: current.tutors.firstOrNull() ?: TutorEntry("", "")
                    val addTutors = if (validTutors.size > 1) {
                        validTutors.drop(1).joinToString(";;") { "${it.name}|${it.cpf}" }
                    } else ""

                    val savedSpecies = current.species.ifBlank { selectedSpecies.displayName }
                    val savedAuditor = current.auditorName.ifBlank { auditorIdentification }

                    val inspection = HorseInspection(
                        protocolNumber = current.protocolNumber,
                        photoPath = watermarked.absolutePath,
                        latitude = current.latitude,
                        longitude = current.longitude,
                        captureTimestamp = current.captureTimestamp,
                        administrativeRegion = current.administrativeRegion,
                        quadra = current.quadra,
                        conjunto = current.conjunto,
                        numero = current.numero,
                        species = savedSpecies,
                        auditorName = savedAuditor,
                        horseDescription = current.horseDescription,
                        horseCount = current.horseCount,
                        mistreatedHorseCount = current.mistreatedHorseCount,
                        mistreatmentIndicators = current.mistreatmentIndicators.joinToString(";;"),
                        adequateIndicators = current.adequateIndicators.joinToString(";;"),
                        requiresSeagriApprehension = current.requiresSeagriApprehension,
                        seagriNotes = current.seagriNotes,
                        requiresPmdfSupport = current.requiresPmdfSupport,
                        pmdfNotes = current.pmdfNotes,
                        requiresOtherAgencySupport = current.requiresOtherAgencySupport,
                        otherAgencyName = current.otherAgencyName,
                        otherAgencyNotes = current.otherAgencyNotes,
                        riskLevel = current.riskLevel,
                        riskJustification = justification,
                        safetyRiskAssessment = safetyAssessment,
                        imageNotes = current.imageNotes,
                        ppbeaAnswersData = com.example.data.model.PpbeaCriteriaRepository.mapToAnswersString(current.ppbeaDiagnosis?.answersMap ?: emptyMap()),
                        individualEvaluationsData = com.example.data.model.IndividualAnimalSerializer.serializeList(current.individualEvaluations),
                        tutorName = primTutor.name,
                        tutorCpf = primTutor.cpf,
                        additionalTutors = addTutors
                    )

                    viewModelScope.launch {
                        val insertedId = repository.insertInspection(inspection)
                        val saved = inspection.copy(id = insertedId)
                        resetDraft()
                        onSuccess(saved)
                    }
                } else {
                    onError("Por favor, capture ou selecione uma foto antes de salvar.")
                }
            }
        }
    }

    fun updateInspection(
        original: HorseInspection,
        newNotes: String,
        newRa: String,
        newQuadra: String = original.quadra,
        newConjunto: String = original.conjunto,
        newNumero: String = original.numero,
        newSpecies: String = original.species,
        newAuditorName: String = original.auditorName,
        newHorseDescription: String = original.horseDescription,
        newHorseCount: Int,
        newMistreatedHorseCount: Int = original.mistreatedHorseCount,
        newMistreatmentIndicators: String = original.mistreatmentIndicators,
        newAdequateIndicators: String = original.adequateIndicators,
        newRequiresSeagriApprehension: Boolean = original.requiresSeagriApprehension,
        newSeagriNotes: String = original.seagriNotes,
        newRequiresPmdfSupport: Boolean = original.requiresPmdfSupport,
        newPmdfNotes: String = original.pmdfNotes,
        newRequiresOtherAgencySupport: Boolean = original.requiresOtherAgencySupport,
        newOtherAgencyName: String = original.otherAgencyName,
        newOtherAgencyNotes: String = original.otherAgencyNotes,
        newRiskLevel: Int = original.riskLevel,
        newSafetyRiskAssessment: String = original.safetyRiskAssessment,
        newTutorName: String,
        newTutorCpf: String,
        newAdditionalTutors: String = original.additionalTutors,
        newPpbeaAnswersData: String = original.ppbeaAnswersData,
        newPhotoPaths: List<String> = original.allPhotoPaths,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val riskLevels = DfConstants.getRiskLevels(newSpecies)
            val riskInfo = riskLevels.find { it.level == newRiskLevel }
            val justification = riskInfo?.summary ?: "Risco nível $newRiskLevel"
            val safetyAssessment = newSafetyRiskAssessment.ifBlank { justification }
            val clampedMistreated = newMistreatedHorseCount.coerceIn(0, newHorseCount)
            val indicatorsToSave = if (clampedMistreated == 0) "" else newMistreatmentIndicators

            val finalPhotos = if (newPhotoPaths.isNotEmpty()) newPhotoPaths else listOf(original.photoPath)
            val primaryPhoto = finalPhotos.first()
            val additionalPhotosString = if (finalPhotos.size > 1) finalPhotos.drop(1).joinToString(";;") else ""

            val updated = original.copy(
                photoPath = primaryPhoto,
                additionalPhotoPaths = additionalPhotosString,
                imageNotes = newNotes,
                administrativeRegion = newRa,
                quadra = newQuadra,
                conjunto = newConjunto,
                numero = newNumero,
                species = newSpecies,
                auditorName = newAuditorName,
                horseDescription = newHorseDescription,
                horseCount = newHorseCount,
                mistreatedHorseCount = clampedMistreated,
                mistreatmentIndicators = indicatorsToSave,
                adequateIndicators = newAdequateIndicators,
                requiresSeagriApprehension = newRequiresSeagriApprehension,
                seagriNotes = newSeagriNotes,
                requiresPmdfSupport = newRequiresPmdfSupport,
                pmdfNotes = newPmdfNotes,
                requiresOtherAgencySupport = newRequiresOtherAgencySupport,
                otherAgencyName = newOtherAgencyName,
                otherAgencyNotes = newOtherAgencyNotes,
                riskLevel = newRiskLevel,
                riskJustification = justification,
                safetyRiskAssessment = safetyAssessment,
                tutorName = newTutorName,
                tutorCpf = CpfValidator.format(newTutorCpf),
                additionalTutors = newAdditionalTutors,
                ppbeaAnswersData = newPpbeaAnswersData
            )

            repository.updateInspection(updated)
            onSuccess()
        }
    }

    fun resetDraft(species: String? = null) {
        val targetSpecies = species ?: selectedSpecies.displayName
        draft.value = InspectionDraftState(
            species = targetSpecies,
            auditorName = auditorIdentification
        )
    }

    fun deleteInspection(inspection: HorseInspection) {
        viewModelScope.launch {
            repository.deleteInspection(inspection)
            inspection.allPhotoPaths.forEach { path ->
                try {
                    val file = File(path)
                    if (file.exists()) file.delete()
                } catch (_: Exception) {}
            }
        }
    }

    var pendingPdfAction by mutableStateOf<(() -> Unit)?>(null)
        private set

    var showPdfDisclaimerDialog by mutableStateOf(false)
        private set

    fun openPdfDisclaimerDialog(onConfirmed: (() -> Unit)? = null) {
        pendingPdfAction = onConfirmed
        showPdfDisclaimerDialog = true
    }

    fun dismissPdfDisclaimer() {
        showPdfDisclaimerDialog = false
        pendingPdfAction = null
    }

    fun confirmPdfDisclaimer() {
        showPdfDisclaimerDialog = false
        val action = pendingPdfAction
        pendingPdfAction = null
        action?.invoke()
    }

    fun exportSinglePdf(inspection: HorseInspection) {
        pendingPdfAction = {
            PdfReportGenerator.generateAndShareSinglePdf(getApplication(), inspection)
        }
    }

    fun exportMultipleConsolidatedPdf(inspections: List<HorseInspection>) {
        pendingPdfAction = {
            PdfReportGenerator.generateAndShareConsolidatedPdf(
                context = getApplication(),
                inspections = inspections,
                startDateMillis = null,
                endDateMillis = null,
                selectedRa = null
            )
        }
    }

    fun exportMultipleIndividualPdfs(inspections: List<HorseInspection>) {
        pendingPdfAction = {
            PdfReportGenerator.generateAndShareMultipleIndividualPdfs(
                context = getApplication(),
                inspections = inspections
            )
        }
    }

    fun exportInspectionsCsv(inspections: List<HorseInspection>) {
        val file = com.example.util.CsvExportUtils.exportInspectionsToCsv(getApplication(), inspections)
        if (file != null) {
            com.example.util.CsvExportUtils.shareCsvFile(getApplication(), file)
        }
    }

    fun exportBackupToUri(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = allInspections.value
            val result = com.example.util.DatabaseBackupManager.exportToZipUri(getApplication(), uri, currentList)
            val (success, msg) = if (result.isSuccess) {
                val backup = result.getOrNull()
                val recCount = backup?.recordsCount ?: 0
                val photoCount = backup?.photosCount ?: 0
                Pair(true, "Cópia completa salva com sucesso! ($recCount relatórios e $photoCount fotos arquivadas)")
            } else {
                Pair(false, "Erro ao salvar cópia completa: ${result.exceptionOrNull()?.message}")
            }
            withContext(Dispatchers.Main) {
                onResult(success, msg)
            }
        }
    }

    fun importBackupFromUri(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            val result = com.example.util.DatabaseBackupManager.importFromBackupUri(getApplication(), uri, db.horseInspectionDao())
            val (success, msg) = if (result.isSuccess) {
                val backup = result.getOrNull()
                val recCount = backup?.recordsCount ?: 0
                val photoCount = backup?.photosCount ?: 0
                Pair(true, "Backup restaurado com sucesso! ($recCount relatórios e $photoCount fotos recuperadas no aparelho)")
            } else {
                Pair(false, "Erro ao restaurar backup: ${result.exceptionOrNull()?.message}")
            }
            withContext(Dispatchers.Main) {
                onResult(success, msg)
            }
        }
    }

    fun deleteAllLocalDataAndRoom(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            val result = com.example.util.DatabaseBackupManager.deleteAllLocalDataAndRoom(
                context = getApplication(),
                dao = db.horseInspectionDao()
            )
            val (success, msg) = if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                Pair(true, "Limpeza concluída! $count registros do Room e todos os arquivos locais de fotos foram apagados.")
            } else {
                Pair(false, "Erro ao apagar dados do Room: ${result.exceptionOrNull()?.message}")
            }
            withContext(Dispatchers.Main) {
                onResult(success, msg)
            }
        }
    }

    fun exportConsolidatedPdf() {
        val inspections = reportPreviewInspections.value
        val start = reportStartDate.value
        val end = reportEndDate.value
        val ra = reportSelectedRa.value
        pendingPdfAction = {
            PdfReportGenerator.generateAndShareConsolidatedPdf(getApplication(), inspections, start, end, ra)
        }
    }
}
