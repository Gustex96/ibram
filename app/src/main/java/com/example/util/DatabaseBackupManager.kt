package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.db.HorseInspectionDao
import com.example.data.model.HorseInspection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DatabaseBackupManager {

    private const val TAG = "DatabaseBackup"

    fun generateBackupFilename(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "backup_fiscalizacoes_df_${sdf.format(Date())}.json"
    }

    suspend fun exportToJsonUri(
        context: Context,
        uri: Uri,
        inspections: List<HorseInspection>
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("appName", "Levantamento Operacional DF")
            root.put("exportedAt", System.currentTimeMillis())
            root.put("recordCount", inspections.size)

            val array = JSONArray()
            for (item in inspections) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("protocolNumber", item.protocolNumber)
                    put("photoPath", item.photoPath)
                    put("additionalPhotoPaths", item.additionalPhotoPaths)
                    put("latitude", item.latitude)
                    put("longitude", item.longitude)
                    put("captureTimestamp", item.captureTimestamp)
                    put("administrativeRegion", item.administrativeRegion)
                    put("quadra", item.quadra)
                    put("conjunto", item.conjunto)
                    put("numero", item.numero)
                    put("horseDescription", item.horseDescription)
                    put("horseCount", item.horseCount)
                    put("mistreatedHorseCount", item.mistreatedHorseCount)
                    put("mistreatmentIndicators", item.mistreatmentIndicators)
                    put("adequateIndicators", item.adequateIndicators)
                    put("requiresSeagriApprehension", item.requiresSeagriApprehension)
                    put("seagriNotes", item.seagriNotes)
                    put("requiresPmdfSupport", item.requiresPmdfSupport)
                    put("pmdfNotes", item.pmdfNotes)
                    put("riskLevel", item.riskLevel)
                    put("riskJustification", item.riskJustification)
                    put("safetyRiskAssessment", item.safetyRiskAssessment)
                    put("imageNotes", item.imageNotes)
                    put("tutorName", item.tutorName)
                    put("tutorCpf", item.tutorCpf)
                    put("additionalTutors", item.additionalTutors)
                    put("inspectionTeam", item.inspectionTeam)
                    put("createdAt", item.createdAt)
                }
                array.put(obj)
            }
            root.put("inspections", array)

            val jsonString = root.toString(2)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                    writer.flush()
                }
            } ?: return@withContext Result.failure(Exception("Não foi possível abrir o local selecionado para gravação."))

            Log.d(TAG, "Backup exportado com sucesso: ${inspections.size} registros salvos no URI: $uri")
            Result.success(inspections.size)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao exportar backup: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun importFromJsonUri(
        context: Context,
        uri: Uri,
        dao: HorseInspectionDao
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val content = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        content.append(line)
                        line = reader.readLine()
                    }
                }
            } ?: return@withContext Result.failure(Exception("Não foi possível ler o arquivo selecionado."))

            val text = content.toString().trim()
            val array = if (text.startsWith("[")) {
                JSONArray(text)
            } else {
                val root = JSONObject(text)
                root.optJSONArray("inspections") ?: root.optJSONArray("records") ?: JSONArray()
            }

            val list = mutableListOf<HorseInspection>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val inspection = HorseInspection(
                    protocolNumber = obj.optString("protocolNumber", "DF-REC-${System.currentTimeMillis()}"),
                    photoPath = obj.optString("photoPath", ""),
                    additionalPhotoPaths = obj.optString("additionalPhotoPaths", ""),
                    latitude = obj.optDouble("latitude", com.example.data.model.DfConstants.DEFAULT_DF_LATITUDE),
                    longitude = obj.optDouble("longitude", com.example.data.model.DfConstants.DEFAULT_DF_LONGITUDE),
                    captureTimestamp = obj.optLong("captureTimestamp", System.currentTimeMillis()),
                    administrativeRegion = obj.optString("administrativeRegion", "Plano Piloto (RA I)"),
                    quadra = obj.optString("quadra", ""),
                    conjunto = obj.optString("conjunto", ""),
                    numero = obj.optString("numero", ""),
                    horseDescription = obj.optString("horseDescription", ""),
                    horseCount = obj.optInt("horseCount", 1),
                    mistreatedHorseCount = obj.optInt("mistreatedHorseCount", 0),
                    mistreatmentIndicators = obj.optString("mistreatmentIndicators", ""),
                    adequateIndicators = obj.optString("adequateIndicators", ""),
                    requiresSeagriApprehension = obj.optBoolean("requiresSeagriApprehension", false),
                    seagriNotes = obj.optString("seagriNotes", ""),
                    requiresPmdfSupport = obj.optBoolean("requiresPmdfSupport", false),
                    pmdfNotes = obj.optString("pmdfNotes", ""),
                    riskLevel = obj.optInt("riskLevel", 1),
                    riskJustification = obj.optString("riskJustification", ""),
                    safetyRiskAssessment = obj.optString("safetyRiskAssessment", ""),
                    imageNotes = obj.optString("imageNotes", ""),
                    tutorName = obj.optString("tutorName", ""),
                    tutorCpf = obj.optString("tutorCpf", ""),
                    additionalTutors = obj.optString("additionalTutors", ""),
                    inspectionTeam = obj.optString("inspectionTeam", "Equipe de Apoio em Fiscalização"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
                list.add(inspection)
            }

            if (list.isNotEmpty()) {
                dao.insertAll(list)
            }

            Log.d(TAG, "Backup importado com sucesso: ${list.size} registros restaurados.")
            Result.success(list.size)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao importar backup: ${e.message}", e)
            Result.failure(e)
        }
    }
}
