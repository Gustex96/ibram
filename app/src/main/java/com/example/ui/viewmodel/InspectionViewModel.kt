package com.example.ui.viewmodel

import android.app.Application
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
import com.example.ui.theme.VisualComfortMode
import com.example.util.AppVersionInfo
import com.example.util.CpfValidator
import com.example.util.LocationHelper
import com.example.util.PdfReportGenerator
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
    val horseDescription: String = "",
    val horseCount: Int = 1,
    val mistreatedHorseCount: Int = 0,
    val mistreatmentIndicators: List<String> = emptyList(),
    val adequateIndicators: List<String> = emptyList(),
    val requiresSeagriApprehension: Boolean = false,
    val seagriNotes: String = "",
    val requiresPmdfSupport: Boolean = false,
    val pmdfNotes: String = "",
    val riskLevel: Int = 3,
    val safetyRiskAssessment: String = "",
    val imageNotes: String = "",
    val tutors: List<TutorEntry> = listOf(TutorEntry("", "")),
    val isWatermarking: Boolean = false,
    val isSaving: Boolean = false,
    val protocolNumber: String = generateProtocol()
) {
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

    init {
        val db = AppDatabase.getDatabase(application)
        repository = InspectionRepository(db.horseInspectionDao())
        locationHelper = LocationHelper(application)
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
        activeScreenName = screen
        activeDetailId = detailId
    }

    // Gerenciamento de Atualização do App (GitHub raw version.json)
    var availableUpdate by mutableStateOf<AppVersionInfo?>(null)
        private set
    var isCheckingUpdate by mutableStateOf(false)
        private set

    fun dismissUpdateDialog() {
        availableUpdate = null
    }

    fun checkForUpdates(customUrl: String? = null) {
        viewModelScope.launch {
            isCheckingUpdate = true
            try {
                val update = UpdateChecker.checkForUpdate(getApplication(), customUrl)
                availableUpdate = update
            } finally {
                isCheckingUpdate = false
            }
        }
    }

    val searchQuery = MutableStateFlow("")
    val selectedRiskFilter = MutableStateFlow<Int?>(null) // null = all
    val selectedRaFilter = MutableStateFlow<String?>(null)

    // PDF Report Generator Hub Filters
    val reportStartDate = MutableStateFlow<Long?>(null)
    val reportEndDate = MutableStateFlow<Long?>(null)
    val reportSelectedRa = MutableStateFlow<String?>(null)

    val filteredInspections: StateFlow<List<HorseInspection>> = combine(
        allInspections,
        searchQuery,
        selectedRiskFilter,
        selectedRaFilter
    ) { list, query, risk, ra ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.administrativeRegion.contains(query, ignoreCase = true) ||
                item.tutorName.contains(query, ignoreCase = true) ||
                item.protocolNumber.contains(query, ignoreCase = true) ||
                item.imageNotes.contains(query, ignoreCase = true) ||
                item.horseDescription.contains(query, ignoreCase = true) ||
                item.safetyRiskAssessment.contains(query, ignoreCase = true) ||
                item.tutorCpf.contains(query, ignoreCase = true)

            val matchesRisk = risk == null || item.riskLevel == risk
            val matchesRa = ra == null || item.administrativeRegion == ra

            matchesQuery && matchesRisk && matchesRa
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

    val draft = MutableStateFlow(InspectionDraftState())

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
        exemplarIndex: Int? = null
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
                exemplarIndex = orderIndex
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
                    horseDescription = current.horseDescription,
                    horseCount = current.horseCount,
                    mistreatedHorseCount = current.mistreatedHorseCount,
                    mistreatmentIndicators = current.mistreatmentIndicators.joinToString(";;"),
                    adequateIndicators = current.adequateIndicators.joinToString(";;"),
                    requiresSeagriApprehension = current.requiresSeagriApprehension,
                    seagriNotes = current.seagriNotes,
                    requiresPmdfSupport = current.requiresPmdfSupport,
                    pmdfNotes = current.pmdfNotes,
                    riskLevel = current.riskLevel,
                    riskJustification = justification,
                    safetyRiskAssessment = safetyAssessment,
                    imageNotes = current.imageNotes,
                    tutorName = primTutor.name,
                    tutorCpf = primTutor.cpf,
                    additionalTutors = addTutors
                )

                val insertedId = repository.insertInspection(inspection)
                val savedInspection = inspection.copy(id = insertedId)

                // Sincronização automática em segundo plano com o Firestore
                viewModelScope.launch(Dispatchers.IO) {
                    com.example.util.FirestoreSyncManager.syncInspection(getApplication(), savedInspection)
                }

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
            canvas.drawText("BRASÍLIA AMBIENTAL • LEVANTAMENTO OPERACIONAL", 80f, 300f, textPaint)
            textPaint.textSize = 28f
            canvas.drawText("Área de pastoreio / Contenção de equinos", 80f, 360f, textPaint)
            canvas.drawText("RA: ${current.administrativeRegion}", 80f, 420f, textPaint)
            canvas.drawText("Cavalos contabilizados: ${current.horseCount}", 80f, 480f, textPaint)

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
                    val riskInfo = DfConstants.RISK_LEVELS.find { it.level == current.riskLevel }
                    val justification = riskInfo?.summary ?: "Risco nível ${current.riskLevel}"
                    val safetyAssessment = current.safetyRiskAssessment.ifBlank { justification }

                    val validTutors = current.tutors.filter { it.name.isNotBlank() || it.cpf.isNotBlank() }
                    val primTutor = validTutors.firstOrNull() ?: current.tutors.firstOrNull() ?: TutorEntry("", "")
                    val addTutors = if (validTutors.size > 1) {
                        validTutors.drop(1).joinToString(";;") { "${it.name}|${it.cpf}" }
                    } else ""

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
                        horseDescription = current.horseDescription,
                        horseCount = current.horseCount,
                        mistreatedHorseCount = current.mistreatedHorseCount,
                        mistreatmentIndicators = current.mistreatmentIndicators.joinToString(";;"),
                        adequateIndicators = current.adequateIndicators.joinToString(";;"),
                        requiresSeagriApprehension = current.requiresSeagriApprehension,
                        seagriNotes = current.seagriNotes,
                        requiresPmdfSupport = current.requiresPmdfSupport,
                        pmdfNotes = current.pmdfNotes,
                        riskLevel = current.riskLevel,
                        riskJustification = justification,
                        safetyRiskAssessment = safetyAssessment,
                        imageNotes = current.imageNotes,
                        tutorName = primTutor.name,
                        tutorCpf = primTutor.cpf,
                        additionalTutors = addTutors
                    )

                    viewModelScope.launch {
                        val insertedId = repository.insertInspection(inspection)
                        val saved = inspection.copy(id = insertedId)
                        viewModelScope.launch(Dispatchers.IO) {
                            com.example.util.FirestoreSyncManager.syncInspection(getApplication(), saved)
                        }
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
        newHorseDescription: String = original.horseDescription,
        newHorseCount: Int,
        newMistreatedHorseCount: Int = original.mistreatedHorseCount,
        newMistreatmentIndicators: String = original.mistreatmentIndicators,
        newAdequateIndicators: String = original.adequateIndicators,
        newRequiresSeagriApprehension: Boolean = original.requiresSeagriApprehension,
        newSeagriNotes: String = original.seagriNotes,
        newRequiresPmdfSupport: Boolean = original.requiresPmdfSupport,
        newPmdfNotes: String = original.pmdfNotes,
        newRiskLevel: Int = original.riskLevel,
        newSafetyRiskAssessment: String = original.safetyRiskAssessment,
        newTutorName: String,
        newTutorCpf: String,
        newAdditionalTutors: String = original.additionalTutors,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val riskInfo = DfConstants.RISK_LEVELS.find { it.level == newRiskLevel }
            val justification = riskInfo?.summary ?: "Risco nível $newRiskLevel"
            val safetyAssessment = newSafetyRiskAssessment.ifBlank { justification }
            val clampedMistreated = newMistreatedHorseCount.coerceIn(0, newHorseCount)
            val indicatorsToSave = if (clampedMistreated == 0) "" else newMistreatmentIndicators

            val updated = original.copy(
                imageNotes = newNotes,
                administrativeRegion = newRa,
                quadra = newQuadra,
                conjunto = newConjunto,
                numero = newNumero,
                horseDescription = newHorseDescription,
                horseCount = newHorseCount,
                mistreatedHorseCount = clampedMistreated,
                mistreatmentIndicators = indicatorsToSave,
                adequateIndicators = newAdequateIndicators,
                requiresSeagriApprehension = newRequiresSeagriApprehension,
                seagriNotes = newSeagriNotes,
                requiresPmdfSupport = newRequiresPmdfSupport,
                pmdfNotes = newPmdfNotes,
                riskLevel = newRiskLevel,
                riskJustification = justification,
                safetyRiskAssessment = safetyAssessment,
                tutorName = newTutorName,
                tutorCpf = CpfValidator.format(newTutorCpf),
                additionalTutors = newAdditionalTutors
            )

            repository.updateInspection(updated)
            viewModelScope.launch(Dispatchers.IO) {
                com.example.util.FirestoreSyncManager.syncInspection(getApplication(), updated)
            }
            onSuccess()
        }
    }

    fun resetDraft() {
        draft.value = InspectionDraftState()
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

    fun exportSinglePdf(inspection: HorseInspection) {
        PdfReportGenerator.generateAndShareSinglePdf(getApplication(), inspection)
    }

    fun exportMultipleConsolidatedPdf(inspections: List<HorseInspection>) {
        PdfReportGenerator.generateAndShareConsolidatedPdf(
            context = getApplication(),
            inspections = inspections,
            startDateMillis = null,
            endDateMillis = null,
            selectedRa = null
        )
    }

    fun exportMultipleIndividualPdfs(inspections: List<HorseInspection>) {
        PdfReportGenerator.generateAndShareMultipleIndividualPdfs(
            context = getApplication(),
            inspections = inspections
        )
    }

    fun exportInspectionsCsv(inspections: List<HorseInspection>) {
        val file = com.example.util.CsvExportUtils.exportInspectionsToCsv(getApplication(), inspections)
        if (file != null) {
            com.example.util.CsvExportUtils.shareCsvFile(getApplication(), file)
        }
    }

    val isSyncingCloud = MutableStateFlow(false)

    fun syncAllToCloud(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            isSyncingCloud.value = true
            val currentList = allInspections.value
            if (currentList.isEmpty()) {
                isSyncingCloud.value = false
                withContext(Dispatchers.Main) {
                    onComplete(false, "Nenhum relatório local disponível para envio ao Firebase.")
                }
                return@launch
            }

            val result = com.example.util.FirestoreSyncManager.syncAll(getApplication(), currentList)
            isSyncingCloud.value = false

            val (success, msg) = if (result.successCount > 0 && result.failCount == 0) {
                Pair(true, "${result.successCount} relatório(s) sincronizado(s) com sucesso no Firebase Firestore!")
            } else if (result.successCount > 0) {
                Pair(true, "${result.successCount} sincronizados, ${result.failCount} falharam. Detalhe: ${result.errorMessage ?: "Erro desconhecido"}")
            } else {
                val reason = result.errorMessage ?: "Verifique a conexão ou as Regras de Segurança do Firestore."
                Pair(false, "Falha ao enviar para o Firebase: $reason")
            }

            withContext(Dispatchers.Main) {
                onComplete(success, msg)
            }
        }
    }

    fun restoreAllFromCloud(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            isSyncingCloud.value = true
            val db = AppDatabase.getDatabase(getApplication())
            val result = com.example.util.FirestoreSyncManager.fetchAndRestoreAll(getApplication(), db.horseInspectionDao())
            isSyncingCloud.value = false

            val (success, msg) = if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                Pair(true, "$count relatório(s) recuperado(s) com sucesso da nuvem Firebase!")
            } else {
                val reason = result.exceptionOrNull()?.message ?: "Erro desconhecido ao recuperar da nuvem."
                Pair(false, "Falha ao recuperar do Firebase: $reason")
            }

            withContext(Dispatchers.Main) {
                onComplete(success, msg)
            }
        }
    }

    fun exportBackupToUri(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = allInspections.value
            val result = com.example.util.DatabaseBackupManager.exportToJsonUri(getApplication(), uri, currentList)
            val (success, msg) = if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                Pair(true, "Cópia do banco de dados salva com sucesso! ($count registros salvos no arquivo)")
            } else {
                Pair(false, "Erro ao salvar cópia do banco de dados: ${result.exceptionOrNull()?.message}")
            }
            withContext(Dispatchers.Main) {
                onResult(success, msg)
            }
        }
    }

    fun importBackupFromUri(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            val result = com.example.util.DatabaseBackupManager.importFromJsonUri(getApplication(), uri, db.horseInspectionDao())
            val (success, msg) = if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                Pair(true, "$count registros restaurados do arquivo selecionado!")
            } else {
                Pair(false, "Erro ao restaurar banco de dados: ${result.exceptionOrNull()?.message}")
            }
            withContext(Dispatchers.Main) {
                onResult(success, msg)
            }
        }
    }

    suspend fun restoreFromCloud(): Pair<Boolean, String> {
        val db = AppDatabase.getDatabase(getApplication())
        val result = com.example.util.FirestoreSyncManager.fetchAndRestoreAll(getApplication(), db.horseInspectionDao())
        return if (result.isSuccess) {
            val count = result.getOrNull() ?: 0
            Pair(true, "$count registro(s) recuperado(s) com sucesso da nuvem!")
        } else {
            Pair(false, result.exceptionOrNull()?.message ?: "Erro ao recuperar da nuvem.")
        }
    }

    fun exportConsolidatedPdf() {
        val inspections = reportPreviewInspections.value
        val start = reportStartDate.value
        val end = reportEndDate.value
        val ra = reportSelectedRa.value
        PdfReportGenerator.generateAndShareConsolidatedPdf(getApplication(), inspections, start, end, ra)
    }
}
