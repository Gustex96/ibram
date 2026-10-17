package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.db.AppDatabase
import com.example.data.model.HorseInspection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object DatabaseBackupManager {

    suspend fun exportBackup(context: Context, inspections: List<HorseInspection>): File = withContext(Dispatchers.IO) {
        val backupDir = File(context.cacheDir, "backups").apply { if (!exists()) mkdirs() }
        val zipFile = File(backupDir, "Backup_Fiscalizacao_DF_${System.currentTimeMillis()}.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // Write JSON
            val jsonArray = JSONArray()
            val photosToPack = mutableSetOf<String>()

            for (insp in inspections) {
                val obj = JSONObject().apply {
                    put("id", insp.id)
                    put("protocolNumber", insp.protocolNumber)
                    put("species", insp.species)
                    put("customSpeciesName", insp.customSpeciesName)
                    put("speciesEmoji", insp.speciesEmoji)
                    put("horseCount", insp.horseCount)
                    put("mistreatedHorseCount", insp.mistreatedHorseCount)
                    put("auditorName", insp.auditorName)
                    put("auditorBadge", insp.auditorBadge)
                    put("auditorAgency", insp.auditorAgency)
                    put("administrativeRegion", insp.administrativeRegion)
                    put("quadra", insp.quadra)
                    put("conjunto", insp.conjunto)
                    put("numero", insp.numero)
                    put("latitude", insp.latitude)
                    put("longitude", insp.longitude)
                    put("captureTimestamp", insp.captureTimestamp)
                    put("formattedCaptureDate", insp.formattedCaptureDate)
                    put("photoPath", insp.photoPath)
                    put("additionalPhotoPathsJson", insp.additionalPhotoPathsJson)
                    put("imageNotes", insp.imageNotes)
                    put("horseDescription", insp.horseDescription)
                    put("otherAgencyNotes", insp.otherAgencyNotes)
                    put("tutorsJson", insp.tutorsJson)
                    put("mistreatmentIndicatorsJson", insp.mistreatmentIndicatorsJson)
                    put("adequateWelfareIndicatorsJson", insp.adequateWelfareIndicatorsJson)
                    put("bodyConditionScore", insp.bodyConditionScore)
                    put("hydrationStatus", insp.hydrationStatus)
                    put("postureBehavior", insp.postureBehavior)
                    put("hoofDentalStatus", insp.hoofDentalStatus)
                    put("createdAt", insp.createdAt)
                }
                jsonArray.put(obj)
                for (p in insp.allPhotoPaths) {
                    if (p.isNotBlank()) photosToPack.add(p)
                }
            }

            zos.putNextEntry(ZipEntry("inspections.json"))
            zos.write(jsonArray.toString(2).toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // Write photos
            for (photoPath in photosToPack) {
                val photoFile = File(photoPath)
                if (photoFile.exists() && photoFile.length() > 0) {
                    zos.putNextEntry(ZipEntry("photos/${photoFile.name}"))
                    FileInputStream(photoFile).use { fis ->
                        fis.copyTo(zos)
                    }
                    zos.closeEntry()
                }
            }
        }
        zipFile
    }

    suspend fun importBackup(context: Context, backupUri: Uri): Int = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val photosDir = File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }
        var importedCount = 0

        context.contentResolver.openInputStream(backupUri)?.use { rawIn ->
            ZipInputStream(rawIn).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                var jsonContent: String? = null
                val restoredPhotoMap = mutableMapOf<String, String>()

                while (entry != null) {
                    if (entry.name == "inspections.json") {
                        jsonContent = zis.bufferedReader(Charsets.UTF_8).readText()
                    } else if (entry.name.startsWith("photos/")) {
                        val fileName = File(entry.name).name
                        val targetFile = File(photosDir, fileName)
                        FileOutputStream(targetFile).use { fos ->
                            zis.copyTo(fos)
                        }
                        restoredPhotoMap[fileName] = targetFile.absolutePath
                        WatermarkUtils.saveToGallery(context, targetFile, "Fiscalização DF Restaurada")
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }

                if (jsonContent != null) {
                    val array = JSONArray(jsonContent)
                    val list = mutableListOf<HorseInspection>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        var pPath = obj.optString("photoPath", "")
                        if (pPath.isNotBlank()) {
                            val fName = File(pPath).name
                            restoredPhotoMap[fName]?.let { pPath = it }
                        }

                        val rawExtras = obj.optString("additionalPhotoPathsJson", "")
                        val updatedExtras = if (rawExtras.isNotBlank()) {
                            rawExtras.split(";;;").joinToString(";;;") { oldP ->
                                val fName = File(oldP).name
                                restoredPhotoMap[fName] ?: oldP
                            }
                        } else ""

                        val insp = HorseInspection(
                            protocolNumber = obj.optString("protocolNumber", HorseInspection.generateProtocol()),
                            species = obj.optString("species", "Cavalo"),
                            customSpeciesName = obj.optString("customSpeciesName", ""),
                            speciesEmoji = obj.optString("speciesEmoji", "🐴"),
                            horseCount = obj.optInt("horseCount", 1),
                            mistreatedHorseCount = obj.optInt("mistreatedHorseCount", 0),
                            auditorName = obj.optString("auditorName", ""),
                            auditorBadge = obj.optString("auditorBadge", ""),
                            auditorAgency = obj.optString("auditorAgency", "DF Legal"),
                            administrativeRegion = obj.optString("administrativeRegion", "Plano Piloto"),
                            quadra = obj.optString("quadra", ""),
                            conjunto = obj.optString("conjunto", ""),
                            numero = obj.optString("numero", ""),
                            latitude = obj.optDouble("latitude", -15.7975),
                            longitude = obj.optDouble("longitude", -47.8919),
                            captureTimestamp = obj.optLong("captureTimestamp", System.currentTimeMillis()),
                            formattedCaptureDate = obj.optString("formattedCaptureDate", ""),
                            photoPath = pPath,
                            additionalPhotoPathsJson = updatedExtras,
                            imageNotes = obj.optString("imageNotes", ""),
                            horseDescription = obj.optString("horseDescription", ""),
                            otherAgencyNotes = obj.optString("otherAgencyNotes", ""),
                            tutorsJson = obj.optString("tutorsJson", ""),
                            mistreatmentIndicatorsJson = obj.optString("mistreatmentIndicatorsJson", ""),
                            adequateWelfareIndicatorsJson = obj.optString("adequateWelfareIndicatorsJson", ""),
                            bodyConditionScore = obj.optString("bodyConditionScore", "3 - Ideal"),
                            hydrationStatus = obj.optString("hydrationStatus", "Normal"),
                            postureBehavior = obj.optString("postureBehavior", "Alerta e Responsivo"),
                            hoofDentalStatus = obj.optString("hoofDentalStatus", "Sem alterações evidentes"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                        list.add(insp)
                    }
                    db.inspectionDao().insertAll(list)
                    importedCount = list.size
                }
            }
        }
        importedCount
    }
}
