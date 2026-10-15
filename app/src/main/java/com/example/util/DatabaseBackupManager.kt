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
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupResult(
    val recordsCount: Int,
    val photosCount: Int
)

object DatabaseBackupManager {

    private const val TAG = "DatabaseBackup"

    fun generateBackupFilename(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "backup_completo_fiscalizacoes_df_${sdf.format(Date())}.zip"
    }

    /**
     * Exporta um arquivo ZIP completo contendo os relatórios (inspections.json)
     * e todas as fotografias com marca d'água das ocorrências na pasta /photos.
     */
    suspend fun exportToZipUri(
        context: Context,
        uri: Uri,
        inspections: List<HorseInspection>
    ): Result<BackupResult> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("version", 2)
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
                    put("requiresOtherAgencySupport", item.requiresOtherAgencySupport)
                    put("otherAgencyName", item.otherAgencyName)
                    put("otherAgencyNotes", item.otherAgencyNotes)
                    put("riskLevel", item.riskLevel)
                    put("riskJustification", item.riskJustification)
                    put("safetyRiskAssessment", item.safetyRiskAssessment)
                    put("imageNotes", item.imageNotes)
                    put("tutorName", item.tutorName)
                    put("tutorCpf", item.tutorCpf)
                    put("additionalTutors", item.additionalTutors)
                    put("species", item.species)
                    put("auditorName", item.auditorName)
                    put("inspectionTeam", item.inspectionTeam)
                    put("createdAt", item.createdAt)
                }
                array.put(obj)
            }
            root.put("inspections", array)

            var exportedPhotosCount = 0
            val processedFileNames = HashSet<String>()

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zipOut ->
                    // 1. Grava o inspections.json dentro do ZIP
                    zipOut.putNextEntry(ZipEntry("inspections.json"))
                    val jsonBytes = root.toString(2).toByteArray(Charsets.UTF_8)
                    zipOut.write(jsonBytes)
                    zipOut.closeEntry()

                    // 2. Empacota todas as fotografias locais na pasta photos/ dentro do ZIP
                    for (item in inspections) {
                        // Foto principal
                        if (item.photoPath.isNotBlank()) {
                            val file = File(item.photoPath)
                            if (file.exists() && file.isFile && !processedFileNames.contains(file.name)) {
                                processedFileNames.add(file.name)
                                zipOut.putNextEntry(ZipEntry("photos/${file.name}"))
                                file.inputStream().use { input ->
                                    input.copyTo(zipOut)
                                }
                                zipOut.closeEntry()
                                exportedPhotosCount++
                            }
                        }
                        // Fotos adicionais
                        if (item.additionalPhotoPaths.isNotBlank()) {
                            for (path in item.additionalPhotoPaths.split(";;")) {
                                if (path.isNotBlank()) {
                                    val file = File(path)
                                    if (file.exists() && file.isFile && !processedFileNames.contains(file.name)) {
                                        processedFileNames.add(file.name)
                                        zipOut.putNextEntry(ZipEntry("photos/${file.name}"))
                                        file.inputStream().use { input ->
                                            input.copyTo(zipOut)
                                        }
                                        zipOut.closeEntry()
                                        exportedPhotosCount++
                                    }
                                }
                            }
                        }
                    }
                    zipOut.finish()
                }
            } ?: return@withContext Result.failure(Exception("Não foi possível abrir o local selecionado para gravação do backup."))

            Log.d(TAG, "Backup ZIP exportado com sucesso: ${inspections.size} relatórios e $exportedPhotosCount fotos arquivadas.")
            Result.success(BackupResult(recordsCount = inspections.size, photosCount = exportedPhotosCount))
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao exportar backup completo: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Importa um backup completo (.zip com fotos e dados) ou legado (.json com dados).
     * Recupera as fotos para o armazenamento local e as registra na Galeria do celular.
     */
    suspend fun importFromBackupUri(
        context: Context,
        uri: Uri,
        dao: HorseInspectionDao
    ): Result<BackupResult> = withContext(Dispatchers.IO) {
        try {
            val rawInputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Não foi possível ler o arquivo selecionado."))

            val bis = BufferedInputStream(rawInputStream)
            bis.mark(16)
            val headerBytes = ByteArray(4)
            val readCount = bis.read(headerBytes, 0, 4)
            bis.reset()

            // Assinatura ZIP: PK (0x50, 0x4B)
            val isZip = readCount >= 4 &&
                headerBytes[0] == 0x50.toByte() &&
                headerBytes[1] == 0x4B.toByte() &&
                (headerBytes[2] == 0x03.toByte() || headerBytes[2] == 0x05.toByte() || headerBytes[2] == 0x07.toByte())

            if (isZip) {
                val photosDir = File(context.filesDir, "photos").apply {
                    if (!exists()) mkdirs()
                }
                val extractedPhotos = mutableMapOf<String, String>()
                var inspectionsJsonText: String? = null
                var restoredPhotosCount = 0

                ZipInputStream(bis).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val entryName = entry.name
                        if (!entry.isDirectory) {
                            if (entryName == "inspections.json" || entryName.endsWith(".json")) {
                                inspectionsJsonText = zis.bufferedReader(Charsets.UTF_8).readText()
                            } else if (entryName.startsWith("photos/") || entryName.endsWith(".jpg") || entryName.endsWith(".jpeg") || entryName.endsWith(".png")) {
                                val simpleName = File(entryName).name
                                val targetFile = File(photosDir, simpleName)
                                FileOutputStream(targetFile).use { out ->
                                    zis.copyTo(out)
                                }
                                extractedPhotos[simpleName] = targetFile.absolutePath
                                restoredPhotosCount++

                                // Disponibiliza também na Galeria de Fotos do celular
                                WatermarkUtils.saveToGallery(
                                    context = context,
                                    imageFile = targetFile,
                                    title = "Foto Recuperada - $simpleName",
                                    description = "Foto restaurada de cópia de segurança"
                                )
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }

                if (inspectionsJsonText.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Arquivo de backup inválido: dados de relatórios não encontrados dentro do arquivo ZIP."))
                }

                val list = parseInspectionsJson(inspectionsJsonText!!, extractedPhotos, photosDir)
                if (list.isNotEmpty()) {
                    dao.insertAll(list)
                }

                Log.d(TAG, "Backup ZIP importado com sucesso: ${list.size} vistorias e $restoredPhotosCount fotos restauradas.")
                Result.success(BackupResult(recordsCount = list.size, photosCount = restoredPhotosCount))
            } else {
                // Legado: Arquivo JSON simples
                val jsonText = bis.bufferedReader(Charsets.UTF_8).readText().trim()
                val list = parseInspectionsJson(jsonText, emptyMap(), null)
                if (list.isNotEmpty()) {
                    dao.insertAll(list)
                }
                Log.d(TAG, "Backup JSON legado importado: ${list.size} registros.")
                Result.success(BackupResult(recordsCount = list.size, photosCount = 0))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao importar backup: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun parseInspectionsJson(
        text: String,
        extractedPhotos: Map<String, String>,
        photosDir: File?
    ): List<HorseInspection> {
        val array = if (text.startsWith("[")) {
            JSONArray(text)
        } else {
            val root = JSONObject(text)
            root.optJSONArray("inspections") ?: root.optJSONArray("records") ?: JSONArray()
        }

        val list = mutableListOf<HorseInspection>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            // Remapeia a foto principal se ela tiver sido restaurada
            val rawPhotoPath = obj.optString("photoPath", "")
            val resolvedPhotoPath = if (rawPhotoPath.isNotBlank()) {
                val simpleName = File(rawPhotoPath).name
                extractedPhotos[simpleName]
                    ?: if (photosDir != null && File(photosDir, simpleName).exists()) File(photosDir, simpleName).absolutePath
                    else rawPhotoPath
            } else ""

            // Remapeia fotos adicionais
            val rawAdditionalPaths = obj.optString("additionalPhotoPaths", "")
            val resolvedAdditionalPaths = if (rawAdditionalPaths.isNotBlank()) {
                rawAdditionalPaths.split(";;").map { path ->
                    if (path.isNotBlank()) {
                        val simpleName = File(path).name
                        extractedPhotos[simpleName]
                            ?: if (photosDir != null && File(photosDir, simpleName).exists()) File(photosDir, simpleName).absolutePath
                            else path
                    } else ""
                }.filter { it.isNotBlank() }.joinToString(";;")
            } else ""

            val inspection = HorseInspection(
                protocolNumber = obj.optString("protocolNumber", "DF-REC-${System.currentTimeMillis()}"),
                photoPath = resolvedPhotoPath,
                additionalPhotoPaths = resolvedAdditionalPaths,
                latitude = obj.optDouble("latitude", com.example.data.model.DfConstants.DEFAULT_DF_LATITUDE),
                longitude = obj.optDouble("longitude", com.example.data.model.DfConstants.DEFAULT_DF_LONGITUDE),
                captureTimestamp = obj.optLong("captureTimestamp", System.currentTimeMillis()),
                administrativeRegion = obj.optString("administrativeRegion", "Plano Piloto (RA I)"),
                quadra = obj.optString("quadra", ""),
                conjunto = obj.optString("conjunto", ""),
                numero = obj.optString("numero", ""),
                species = obj.optString("species", "Cavalo"),
                auditorName = obj.optString("auditorName", ""),
                horseDescription = obj.optString("horseDescription", ""),
                horseCount = obj.optInt("horseCount", 1),
                mistreatedHorseCount = obj.optInt("mistreatedHorseCount", 0),
                mistreatmentIndicators = obj.optString("mistreatmentIndicators", ""),
                adequateIndicators = obj.optString("adequateIndicators", ""),
                requiresSeagriApprehension = obj.optBoolean("requiresSeagriApprehension", false),
                seagriNotes = obj.optString("seagriNotes", ""),
                requiresPmdfSupport = obj.optBoolean("requiresPmdfSupport", false),
                pmdfNotes = obj.optString("pmdfNotes", ""),
                requiresOtherAgencySupport = obj.optBoolean("requiresOtherAgencySupport", false),
                otherAgencyName = obj.optString("otherAgencyName", ""),
                otherAgencyNotes = obj.optString("otherAgencyNotes", ""),
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
        return list
    }

    suspend fun exportToJsonUri(
        context: Context,
        uri: Uri,
        inspections: List<HorseInspection>
    ): Result<Int> = exportToZipUri(context, uri, inspections).map { it.recordsCount }

    suspend fun importFromJsonUri(
        context: Context,
        uri: Uri,
        dao: HorseInspectionDao
    ): Result<Int> = importFromBackupUri(context, uri, dao).map { it.recordsCount }

    /**
     * Exclui permanentemente todos os registros da tabela Room e deleta
     * todos os arquivos de fotos armazenados localmente no dispositivo.
     */
    suspend fun deleteAllLocalDataAndRoom(
        context: Context,
        dao: HorseInspectionDao
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // 1. Obter todos os registros atuais para identificar e remover arquivos de fotos vinculados
            val allRecords = dao.getAllInspectionsList()
            var deletedFilesCount = 0

            for (record in allRecords) {
                // Deletar foto principal
                if (record.photoPath.isNotBlank()) {
                    try {
                        val file = File(record.photoPath)
                        if (file.exists() && file.delete()) {
                            deletedFilesCount++
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Falha ao deletar foto principal: ${record.photoPath}", e)
                    }
                }
                // Deletar fotos adicionais
                if (record.additionalPhotoPaths.isNotBlank()) {
                    for (path in record.additionalPhotoPaths.split(";;")) {
                        if (path.isNotBlank()) {
                            try {
                                val file = File(path)
                                if (file.exists() && file.delete()) {
                                    deletedFilesCount++
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Falha ao deletar foto adicional: $path", e)
                            }
                        }
                    }
                }
            }

            // 2. Limpar diretórios locais de armazenamento de fotos, relatórios e exports temporários
            val directoriesToClean = listOf(
                File(context.filesDir, "photos"),
                File(context.cacheDir, "photos"),
                File(context.cacheDir, "camera_raw"),
                File(context.cacheDir, "reports"),
                File(context.cacheDir, "exports")
            )

            for (dir in directoriesToClean) {
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.forEach { f ->
                        try {
                            if (f.isFile && f.delete()) {
                                deletedFilesCount++
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Falha ao limpar arquivo local: ${f.absolutePath}", e)
                        }
                    }
                }
            }

            // 3. Deletar todos os registros da tabela Room
            val deletedRecords = dao.deleteAllInspections()

            Log.d(TAG, "Limpeza do Room concluída: $deletedRecords registros apagados e $deletedFilesCount arquivos locais deletados.")
            Result.success(deletedRecords)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar dados locais e do Room: ${e.message}", e)
            Result.failure(e)
        }
    }
}
