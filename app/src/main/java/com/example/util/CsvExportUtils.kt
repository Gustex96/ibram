package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.HorseInspection
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportUtils {

    fun exportInspectionsToCsv(context: Context, inspections: List<HorseInspection>): File? {
        if (inspections.isEmpty()) {
            Toast.makeText(context, "Nenhum registro para exportar em CSV.", Toast.LENGTH_SHORT).show()
            return null
        }

        return try {
            val exportDir = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val csvFile = File(exportDir, "Levantamento_Operacional_$timestamp.csv")

            FileOutputStream(csvFile).use { fos ->
                // Write UTF-8 BOM so Microsoft Excel correctly displays accents and special characters
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    // Header line (using semicolon delimiter which is the standard for Brazilian Excel)
                    writer.append("Protocolo;")
                    writer.append("Data e Hora;")
                    writer.append("Região Administrativa (RA);")
                    writer.append("Quadra;")
                    writer.append("Conjunto;")
                    writer.append("Número;")
                    writer.append("Endereço Completo;")
                    writer.append("Equipe Fiscalizatória;")
                    writer.append("Latitude;")
                    writer.append("Longitude;")
                    writer.append("Total de Animais;")
                    writer.append("Animais com Maus-Tratos;")
                    writer.append("Indicadores de Maus-Tratos;")
                    writer.append("Indicadores de Bem-Estar;")
                    writer.append("Apreensão SEAGRI;")
                    writer.append("Obs SEAGRI;")
                    writer.append("Apoio PMDF;")
                    writer.append("Obs PMDF;")
                    writer.append("Tutores e CPFs;")
                    writer.append("Descrição dos Animais;")
                    writer.append("Notas de Campo;")
                    writer.append("Qtd Fotos\n")

                    // Data rows
                    for (item in inspections) {
                        writer.append(escapeCsv(item.protocolNumber)).append(";")
                        writer.append(escapeCsv(item.formattedCaptureDate)).append(";")
                        writer.append(escapeCsv(item.administrativeRegion)).append(";")
                        writer.append(escapeCsv(item.quadra)).append(";")
                        writer.append(escapeCsv(item.conjunto)).append(";")
                        writer.append(escapeCsv(item.numero)).append(";")
                        writer.append(escapeCsv(item.formattedAddress)).append(";")
                        writer.append(escapeCsv(item.teamDisplay)).append(";")
                        writer.append(String.format(Locale.US, "%.6f", item.latitude)).append(";")
                        writer.append(String.format(Locale.US, "%.6f", item.longitude)).append(";")
                        writer.append(item.horseCount.toString()).append(";")
                        writer.append(item.mistreatedHorseCount.toString()).append(";")
                        writer.append(escapeCsv(item.mistreatmentIndicators.replace(";;", " | "))).append(";")
                        writer.append(escapeCsv(item.adequateIndicators.replace(";;", " | "))).append(";")
                        writer.append(if (item.requiresSeagriApprehension) "SIM" else "NÃO").append(";")
                        writer.append(escapeCsv(item.seagriNotes)).append(";")
                        writer.append(if (item.requiresPmdfSupport) "SIM" else "NÃO").append(";")
                        writer.append(escapeCsv(item.pmdfNotes)).append(";")
                        writer.append(escapeCsv(item.tutorsSummary)).append(";")
                        writer.append(escapeCsv(item.horseDescription)).append(";")
                        writer.append(escapeCsv(item.imageNotes)).append(";")
                        writer.append(item.allPhotoPaths.size.toString()).append("\n")
                    }
                    writer.flush()
                }
            }
            csvFile
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao gerar arquivo CSV: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    fun shareCsvFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Exportação de Registros - Levantamento Operacional")
                putExtra(Intent.EXTRA_TEXT, "Segue em anexo a planilha de registros do Levantamento Operacional (Relatório de Apoio).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Exportar / Compartilhar Planilha CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao compartilhar planilha: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun escapeCsv(text: String): String {
        val sanitized = text.replace("\"", "\"\"")
        return "\"$sanitized\""
    }
}
