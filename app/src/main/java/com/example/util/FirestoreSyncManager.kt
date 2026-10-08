package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.model.HorseInspection
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirestoreSyncManager {

    private const val TAG = "FirestoreSync"
    private const val COLLECTION_NAME = "inspections"

    data class SyncBatchResult(
        val successCount: Int,
        val failCount: Int,
        val errorMessage: String? = null
    )

    fun isFirebaseInitialized(context: Context): Boolean {
        return try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isEmpty()) {
                val app = FirebaseApp.initializeApp(context)
                app != null
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar Firebase: ${e.message}", e)
            false
        }
    }

    suspend fun syncInspection(context: Context, inspection: HorseInspection): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isFirebaseInitialized(context)) {
                return@withContext Result.failure(
                    Exception("Firebase não inicializado. Verifique o arquivo google-services.json.")
                )
            }
            val firestore = FirebaseFirestore.getInstance()
            val docId = inspection.protocolNumber.replace("/", "-").trim().ifBlank { "insp_${inspection.id}" }

            val data = hashMapOf<String, Any?>(
                "id" to inspection.id,
                "protocolNumber" to inspection.protocolNumber,
                "latitude" to inspection.latitude,
                "longitude" to inspection.longitude,
                "captureTimestamp" to inspection.captureTimestamp,
                "administrativeRegion" to inspection.administrativeRegion,
                "quadra" to inspection.quadra,
                "conjunto" to inspection.conjunto,
                "numero" to inspection.numero,
                "horseDescription" to inspection.horseDescription,
                "horseCount" to inspection.horseCount,
                "mistreatedHorseCount" to inspection.mistreatedHorseCount,
                "mistreatmentIndicators" to inspection.mistreatmentIndicators,
                "adequateIndicators" to inspection.adequateIndicators,
                "requiresSeagriApprehension" to inspection.requiresSeagriApprehension,
                "seagriNotes" to inspection.seagriNotes,
                "requiresPmdfSupport" to inspection.requiresPmdfSupport,
                "pmdfNotes" to inspection.pmdfNotes,
                "riskLevel" to inspection.riskLevel,
                "riskJustification" to inspection.riskJustification,
                "safetyRiskAssessment" to inspection.safetyRiskAssessment,
                "imageNotes" to inspection.imageNotes,
                "tutorName" to inspection.tutorName,
                "tutorCpf" to inspection.tutorCpf,
                "additionalTutors" to inspection.additionalTutors,
                "inspectionTeam" to inspection.teamDisplay,
                "allPhotoPathsCount" to inspection.allPhotoPaths.size,
                "createdAt" to inspection.createdAt,
                "syncedAt" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_NAME)
                .document(docId)
                .set(data, SetOptions.merge())
                .await()

            Log.d(TAG, "Registro sincronizado com sucesso: $docId")
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            val userMsg = when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "Permissão negada (PERMISSION_DENIED). Verifique as Regras de Segurança no console Firebase (Firestore Database > Regras)."
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    "Serviço Firebase indisponível ou sem conexão com a internet."
                FirebaseFirestoreException.Code.NOT_FOUND ->
                    "Banco de dados Firestore não encontrado no projeto."
                else -> "Erro no Firestore (${e.code}): ${e.message}"
            }
            Log.e(TAG, "Erro Firestore ao sincronizar: $userMsg", e)
            Result.failure(Exception(userMsg, e))
        } catch (e: Exception) {
            Log.e(TAG, "Erro na sincronização Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun syncAll(context: Context, inspections: List<HorseInspection>): SyncBatchResult = withContext(Dispatchers.IO) {
        if (!isFirebaseInitialized(context)) {
            return@withContext SyncBatchResult(
                0,
                inspections.size,
                "Firebase não inicializado. Verifique o arquivo google-services.json."
            )
        }
        if (inspections.isEmpty()) {
            return@withContext SyncBatchResult(0, 0, "Nenhum relatório local disponível para envio.")
        }
        var successCount = 0
        var failCount = 0
        var firstError: String? = null
        for (item in inspections) {
            val res = syncInspection(context, item)
            if (res.isSuccess) {
                successCount++
            } else {
                failCount++
                if (firstError == null) {
                    firstError = res.exceptionOrNull()?.message
                }
            }
        }
        SyncBatchResult(successCount, failCount, firstError)
    }

    /**
     * Recupera todos os registros salvos na coleção do Firestore e os restaura no banco de dados local.
     */
    suspend fun fetchAndRestoreAll(context: Context, dao: com.example.data.db.HorseInspectionDao): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (!isFirebaseInitialized(context)) {
                return@withContext Result.failure(Exception("Firebase não configurado (adicione o arquivo google-services.json para conectar a nuvem)."))
            }
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection(COLLECTION_NAME).get().await()
            var count = 0
            for (doc in snapshot.documents) {
                val protocol = doc.getString("protocolNumber") ?: doc.id
                val lat = doc.getDouble("latitude") ?: com.example.data.model.DfConstants.DEFAULT_DF_LATITUDE
                val lon = doc.getDouble("longitude") ?: com.example.data.model.DfConstants.DEFAULT_DF_LONGITUDE
                val time = doc.getLong("captureTimestamp") ?: System.currentTimeMillis()
                val ra = doc.getString("administrativeRegion") ?: "Plano Piloto (RA I)"
                val quadra = doc.getString("quadra") ?: ""
                val conjunto = doc.getString("conjunto") ?: ""
                val numero = doc.getString("numero") ?: ""
                val desc = doc.getString("horseDescription") ?: ""
                val countHorses = doc.getLong("horseCount")?.toInt() ?: 1
                val mistreatedCount = doc.getLong("mistreatedHorseCount")?.toInt() ?: 0
                val mistreatInd = doc.getString("mistreatmentIndicators") ?: ""
                val adeqInd = doc.getString("adequateIndicators") ?: ""
                val seagri = doc.getBoolean("requiresSeagriApprehension") ?: false
                val seagriNotes = doc.getString("seagriNotes") ?: ""
                val pmdf = doc.getBoolean("requiresPmdfSupport") ?: false
                val pmdfNotes = doc.getString("pmdfNotes") ?: ""
                val risk = doc.getLong("riskLevel")?.toInt() ?: 1
                val riskJust = doc.getString("riskJustification") ?: ""
                val safetyRisk = doc.getString("safetyRiskAssessment") ?: ""
                val imgNotes = doc.getString("imageNotes") ?: ""
                val tutor = doc.getString("tutorName") ?: ""
                val cpf = doc.getString("tutorCpf") ?: ""
                val addTutors = doc.getString("additionalTutors") ?: ""
                val team = doc.getString("inspectionTeam") ?: "Brasília Ambiental / Fiscalização DF"
                val created = doc.getLong("createdAt") ?: time

                val restoredInspection = HorseInspection(
                    protocolNumber = protocol,
                    photoPath = "",
                    latitude = lat,
                    longitude = lon,
                    captureTimestamp = time,
                    administrativeRegion = ra,
                    quadra = quadra,
                    conjunto = conjunto,
                    numero = numero,
                    horseDescription = desc,
                    horseCount = countHorses,
                    mistreatedHorseCount = mistreatedCount,
                    mistreatmentIndicators = mistreatInd,
                    adequateIndicators = adeqInd,
                    requiresSeagriApprehension = seagri,
                    seagriNotes = seagriNotes,
                    requiresPmdfSupport = pmdf,
                    pmdfNotes = pmdfNotes,
                    riskLevel = risk,
                    riskJustification = riskJust,
                    safetyRiskAssessment = safetyRisk,
                    imageNotes = imgNotes,
                    tutorName = tutor,
                    tutorCpf = cpf,
                    additionalTutors = addTutors,
                    inspectionTeam = team,
                    createdAt = created
                )
                dao.insertInspection(restoredInspection)
                count++
            }
            Result.success(count)
        } catch (e: FirebaseFirestoreException) {
            val userMsg = when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "Permissão negada (PERMISSION_DENIED). Verifique as Regras de Segurança no console Firebase."
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    "Serviço Firebase indisponível ou sem conexão com a internet."
                else -> "Erro no Firestore (${e.code}): ${e.message}"
            }
            Log.e(TAG, "Erro ao recuperar registros: $userMsg", e)
            Result.failure(Exception(userMsg, e))
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao recuperar registros da nuvem: ${e.message}", e)
            Result.failure(e)
        }
    }
}
