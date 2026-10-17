package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.R
import com.example.data.model.HorseInspection
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private fun decodeOptimizedBitmap(filePath: String, targetMaxDimension: Int = 900): Bitmap? {
        val file = File(filePath)
        if (!file.exists() || (file.length() == 0L)) return null

        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            val w = options.outWidth
            val h = options.outHeight
            if (w <= 0 || h <= 0) return null

            var sampleSize = 1
            val maxDim = maxOf(w, h)
            while (maxDim / (sampleSize * 2) >= targetMaxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val decoded = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return null

            val currentMax = maxOf(decoded.width, decoded.height)
            if (currentMax > targetMaxDimension) {
                val scale = targetMaxDimension.toFloat() / currentMax
                val targetW = (decoded.width * scale).toInt().coerceAtLeast(1)
                val targetH = (decoded.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
                if (scaled != decoded) {
                    decoded.recycle()
                }
                scaled
            } else {
                decoded
            }
        } catch (_: Exception) {
            null
        }
    }

    fun generateSingleInspectionPdf(context: Context, inspection: HorseInspection): File? {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val contentWidth = pageWidth - (margin * 2)

        val allPhotos = inspection.allPhotoPaths
        val photoChunks = allPhotos.chunked(2)
        val totalPages = 1 + photoChunks.size

        // PAGE 1: DADOS TÉCNICOS & LEVANTAMENTO OPERACIONAL
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawColor(Color.WHITE)

        // Top decorative bar
        val greenBarPaint = Paint().apply { color = Color.rgb(0, 104, 74); style = Paint.Style.FILL }
        val blueBarPaint = Paint().apply { color = Color.rgb(0, 100, 149); style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 12f, greenBarPaint)
        canvas.drawRect(0f, 12f, pageWidth.toFloat(), 16f, blueBarPaint)

        var y = 32f

        // Header Title
        val headerTitlePaint = Paint().apply {
            color = Color.rgb(0, 104, 74)
            textSize = 12.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("RELATÓRIO TÉCNICO DE FISCALIZAÇÃO", margin, y + 14f, headerTitlePaint)

        val headerSubPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("LEVANTAMENTO OPERACIONAL E BEM-ESTAR ANIMAL • DISTRITO FEDERAL", margin, y + 27f, headerSubPaint)

        val headerSmallPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("Protocolo: ${inspection.protocolNumber}  |  Região Administrativa: ${inspection.administrativeRegion}", margin, y + 40f, headerSmallPaint)

        y += 56f

        val labelPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        fun drawSectionHeader(c: android.graphics.Canvas, title: String, startY: Float): Float {
            val bgPaint = Paint().apply { color = Color.rgb(241, 245, 249); style = Paint.Style.FILL }
            val borderP = Paint().apply { color = Color.rgb(203, 213, 225); style = Paint.Style.STROKE; strokeWidth = 1f }
            val rect = RectF(margin, startY, margin + contentWidth, startY + 18f)
            c.drawRect(rect, bgPaint)
            c.drawRect(rect, borderP)

            val textP = Paint().apply {
                color = Color.rgb(0, 104, 74)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            c.drawText(title, margin + 8f, startY + 12.5f, textP)
            return startY + 26f
        }

        val col1X = margin + 8f
        val col2X = margin + (contentWidth / 2f)

        // SECTION 1: DADOS GERAIS
        y = drawSectionHeader(canvas, "1. DADOS CADASTRAIS DO LEVANTAMENTO", y)
        canvas.drawText("PROTOCOLO DA AÇÃO:", col1X, y, labelPaint)
        canvas.drawText(inspection.protocolNumber, col1X, y + 11f, valuePaint)
        canvas.drawText("DATA E HORA DO REGISTRO:", col2X, y, labelPaint)
        val dateText = if (inspection.formattedCaptureDate.isNotBlank()) inspection.formattedCaptureDate else SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date(inspection.captureTimestamp))
        canvas.drawText(dateText, col2X, y + 11f, valuePaint)
        y += 28f

        canvas.drawText("AUDITOR / AGENTE FISCAL:", col1X, y, labelPaint)
        val audText = if (inspection.auditorName.isNotBlank()) "${inspection.auditorName} (${inspection.auditorAgency})" else "Agente Fiscal DF Legal"
        canvas.drawText(audText, col1X, y + 11f, valuePaint)
        canvas.drawText("COORDENADAS GEOGRÁFICAS (GPS):", col2X, y, labelPaint)
        canvas.drawText(String.format(Locale.US, "%.6f°, %.6f° (SIRGAS 2000)", inspection.latitude, inspection.longitude), col2X, y + 11f, valuePaint)
        y += 30f

        // SECTION 2: IDENTIFICAÇÃO DA ESPÉCIE
        y = drawSectionHeader(canvas, "2. IDENTIFICAÇÃO E CLASSIFICAÇÃO DA ESPÉCIE", y)
        canvas.drawText("ESPÉCIE / CATEGORIA AVALIADA:", col1X, y, labelPaint)
        val speciesInfoText = "${inspection.speciesEmoji} ${inspection.speciesDisplay}"
        canvas.drawText(speciesInfoText, col1X, y + 11f, valuePaint)

        canvas.drawText("QUANTIDADE DE ANIMAIS:", col2X, y, labelPaint)
        val countInfoText = "${inspection.horseCount} animal(is) vistoriado(s) • ${inspection.mistreatedHorseCount} com indícios de maus-tratos"
        canvas.drawText(countInfoText, col2X, y + 11f, valuePaint)
        y += 30f

        // SECTION 3: AVALIAÇÃO DE BEM-ESTAR E MAUS-TRATOS
        y = drawSectionHeader(canvas, "3. AVALIAÇÃO ZOOTÉCNICA E BEM-ESTAR (RES. CFMV Nº 1.236/2018)", y)
        canvas.drawText("ESCORE DE CONDIÇÃO CORPORAL (ECC):", col1X, y, labelPaint)
        canvas.drawText(inspection.bodyConditionScore, col1X, y + 11f, valuePaint)
        canvas.drawText("ESTADO DE HIDRATAÇÃO:", col2X, y, labelPaint)
        canvas.drawText(inspection.hydrationStatus, col2X, y + 11f, valuePaint)
        y += 26f

        canvas.drawText("POSTURA E COMPORTAMENTO:", col1X, y, labelPaint)
        canvas.drawText(inspection.postureBehavior, col1X, y + 11f, valuePaint)
        canvas.drawText("CASCOS / DENTIÇÃO / LOCOMOÇÃO:", col2X, y, labelPaint)
        canvas.drawText(inspection.hoofDentalStatus, col2X, y + 11f, valuePaint)
        y += 28f

        if (inspection.mistreatmentList.isNotEmpty()) {
            val alertPaint = Paint().apply { color = Color.rgb(185, 28, 28); textSize = 8.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
            canvas.drawText("SINAIS DE MAUS-TRATOS CONSTATADOS:", col1X, y, alertPaint)
            y += 12f
            for (ind in inspection.mistreatmentList) {
                canvas.drawText("• $ind", col1X + 4f, y, valuePaint)
                y += 12f
            }
            y += 4f
        }

        // SECTION 4: TUTORES / RESPONSÁVEIS
        val tutors = inspection.allTutors
        y = drawSectionHeader(canvas, "4. IDENTIFICAÇÃO DOS TUTORES E RESPONSÁVEIS (${tutors.size})", y)
        if (tutors.isEmpty()) {
            canvas.drawText("Nenhum tutor ou responsável identificado no local da vistoria.", col1X, y, valuePaint)
            y += 16f
        } else {
            for ((tIdx, tutor) in tutors.withIndex()) {
                val tName = tutor.name.ifBlank { "Não informado" }
                val tCpf = if (tutor.cpf.isNotBlank()) CpfValidator.formatCpfOrCnpj(tutor.cpf) else "Não informado"
                val docLabel = if (CpfValidator.isCnpj(tutor.cpf)) "CNPJ" else "CPF"
                canvas.drawText("TUTOR #${tIdx + 1}: $tName", col1X, y, valuePaint)
                canvas.drawText("$docLabel: $tCpf", col2X, y, valuePaint)
                y += 14f
            }
        }
        y += 4f

        // SECTION 5: NOTAS DE CAMPO
        if (inspection.imageNotes.isNotBlank() || inspection.horseDescription.isNotBlank()) {
            y = drawSectionHeader(canvas, "5. NOTAS DE CAMPO E OBSERVAÇÕES", y)
            val notes = listOf(inspection.horseDescription, inspection.imageNotes).filter { it.isNotBlank() }.joinToString(" • ")
            canvas.drawText(notes, col1X, y, valuePaint)
            y += 16f
        }

        // SECTION 6: RESUMO FOTOGRÁFICO
        y = drawSectionHeader(canvas, "6. REGISTRO FOTOGRÁFICO GEORREFERENCIADO (${allPhotos.size} FOTO(S))", y)
        if (allPhotos.isEmpty()) {
            canvas.drawText("[Nenhuma fotografia anexada nesta ocorrência]", col1X, y, valuePaint)
            y += 16f
        } else if (allPhotos.size == 1) {
            val photoNote = "✓ 1 fotografia georreferenciada anexada na Folha 2 em alta resolução (proporção ajustada)."
            canvas.drawText(photoNote, col1X, y, valuePaint)
            y += 16f
        } else {
            val pageRange = if (photoChunks.size == 1) "na Folha 2" else "nas Folhas 2 a $totalPages"
            val photoNote = "✓ ${allPhotos.size} fotografias georreferenciadas enfileiradas com espaçamento $pageRange (otimização de páginas e proporção preservada)."
            canvas.drawText(photoNote, col1X, y, valuePaint)
            y += 16f
        }

        // FOOTER PAGE 1
        val footerY = pageHeight - 48f
        val linePaint = Paint().apply { color = Color.rgb(203, 213, 225); strokeWidth = 1f }
        canvas.drawLine(margin, footerY, margin + contentWidth, footerY, linePaint)

        val footerTextPaint = Paint().apply { color = Color.rgb(148, 163, 184); textSize = 7.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); isAntiAlias = true }
        val sdfGen = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
        val genText = "Folha 1 de $totalPages | Levantamento expedido em ${sdfGen.format(Date())} | Fiscalização DF"
        canvas.drawText(genText, margin, footerY + 10f, footerTextPaint)
        val legalNote = "Documento técnico para apoio às ações de fiscalização e proteção da fauna do Distrito Federal."
        canvas.drawText(legalNote, margin, footerY + 20f, footerTextPaint)

        val signLineX = margin + contentWidth - 210f
        canvas.drawLine(signLineX, footerY + 20f, margin + contentWidth, footerY + 20f, linePaint)
        val auditorSignLabel = if (inspection.auditorName.isNotBlank()) inspection.auditorName else "Auditor Fiscal de Campo"
        val auditorSubLabel = "Auditor Fiscal / Agente Responsável"
        canvas.drawText(auditorSignLabel, signLineX + 8f, footerY + 30f, footerTextPaint)
        canvas.drawText(auditorSubLabel, signLineX + 8f, footerY + 40f, footerTextPaint)

        document.finishPage(page)

        // PHOTO PAGES (ENFILEIRADAS COM ESPAÇAMENTO: 2 FOTOS POR PÁGINA)
        val borderPaint = Paint().apply { color = Color.rgb(203, 213, 225); style = Paint.Style.STROKE; strokeWidth = 1f }
        val bgFramePaint = Paint().apply { color = Color.rgb(248, 250, 252); style = Paint.Style.FILL }
        val captionTitlePaint = Paint().apply { color = Color.rgb(15, 23, 42); textSize = 8.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        val captionSubPaint = Paint().apply { color = Color.rgb(71, 85, 105); textSize = 7.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); isAntiAlias = true }

        if (photoChunks.isNotEmpty()) {
            photoChunks.forEachIndexed { chunkIndex, chunk ->
                val pageNumber = chunkIndex + 2
                val photoPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val photoPage = document.startPage(photoPageInfo)
                val pCanvas = photoPage.canvas
                pCanvas.drawColor(Color.WHITE)

                // Header bars
                pCanvas.drawRect(0f, 0f, pageWidth.toFloat(), 12f, greenBarPaint)
                pCanvas.drawRect(0f, 12f, pageWidth.toFloat(), 16f, blueBarPaint)

                var py = 32f
                pCanvas.drawText("ANEXO FOTOGRÁFICO GEORREFERENCIADO • LEVANTAMENTO OPERACIONAL", margin, py, headerSubPaint)
                py += 15f
                val startIdx = (chunkIndex * 2) + 1
                val endIdx = startIdx + chunk.size - 1
                val rangeStr = if (startIdx == endIdx) "Foto $startIdx de ${allPhotos.size}" else "Fotos $startIdx a $endIdx de ${allPhotos.size}"
                pCanvas.drawText("Levantamento: ${inspection.protocolNumber} | RA: ${inspection.administrativeRegion} | $rangeStr", margin, py, headerSmallPaint)
                py += 20f

                val bgSectionP = Paint().apply { color = Color.rgb(241, 245, 249); style = Paint.Style.FILL }
                val sectionRect = RectF(margin, py, margin + contentWidth, py + 18f)
                pCanvas.drawRect(sectionRect, bgSectionP)
                pCanvas.drawRect(sectionRect, borderPaint)
                val sectionTextP = Paint().apply { color = Color.rgb(0, 104, 74); textSize = 9f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
                pCanvas.drawText("REGISTRO FOTOGRÁFICO EM FILA (PROPORÇÃO AJUSTADA COM ALTA DEFINIÇÃO)", margin + 8f, py + 12.5f, sectionTextP)
                py += 26f

                val availableContentHeight = footerY - py - 12f

                if (chunk.size == 2) {
                    val gap = 16f
                    val itemTotalHeight = (availableContentHeight - gap) / 2f

                    chunk.forEachIndexed { itemIdx, photoPath ->
                        val globalPhotoIndex = (chunkIndex * 2) + itemIdx
                        val itemStartY = py + (itemIdx * (itemTotalHeight + gap))

                        pCanvas.drawText("📸 FOTO ${globalPhotoIndex + 1} DE ${allPhotos.size} • REGISTRO FISCAL GEORREFERENCIADO", margin, itemStartY + 10f, captionTitlePaint)

                        val boxTop = itemStartY + 16f
                        val boxHeight = itemTotalHeight - 44f
                        val boxWidth = contentWidth
                        val boxRect = RectF(margin, boxTop, margin + boxWidth, boxTop + boxHeight)

                        pCanvas.drawRoundRect(boxRect, 4f, 4f, bgFramePaint)

                        val optimizedBitmap = decodeOptimizedBitmap(photoPath, targetMaxDimension = 900)
                        if (optimizedBitmap != null) {
                            val bRatio = optimizedBitmap.width.toFloat() / optimizedBitmap.height.toFloat()
                            var dw = boxWidth - 12f
                            var dh = dw / bRatio
                            if (dh > boxHeight - 8f) {
                                dh = boxHeight - 8f
                                dw = dh * bRatio
                            }
                            val drawX = margin + ((boxWidth - dw) / 2f)
                            val drawY = boxTop + ((boxHeight - dh) / 2f)
                            val drawRect = RectF(drawX, drawY, drawX + dw, drawY + dh)

                            pCanvas.drawBitmap(optimizedBitmap, null, drawRect, null)
                            pCanvas.drawRoundRect(boxRect, 4f, 4f, borderPaint)
                        } else {
                            pCanvas.drawRoundRect(boxRect, 4f, 4f, borderPaint)
                            pCanvas.drawText("[Arquivo de fotografia não disponível no armazenamento]", margin + 16f, boxTop + (boxHeight / 2f), captionSubPaint)
                        }

                        val captionY = boxTop + boxHeight + 11f
                        pCanvas.drawText("📍 Coordenadas: ${String.format(Locale.US, "%.6f°, %.6f°", inspection.latitude, inspection.longitude)}  |  🏛️ Região Administrativa: ${inspection.administrativeRegion}", margin, captionY, captionTitlePaint)
                        pCanvas.drawText("🕒 Data/Hora: ${dateText}  |  Registro Georreferenciado • Preservação da Cadeia de Custódia", margin, captionY + 11f, captionSubPaint)
                    }
                } else {
                    val photoPath = chunk[0]
                    val globalPhotoIndex = (chunkIndex * 2)

                    pCanvas.drawText("📸 FOTO ${globalPhotoIndex + 1} DE ${allPhotos.size} • REGISTRO FISCAL GEORREFERENCIADO", margin, py + 12f, captionTitlePaint)

                    val boxTop = py + 20f
                    val boxHeight = if (allPhotos.size == 1) 450f else 320f
                    val boxWidth = contentWidth
                    val boxRect = RectF(margin, boxTop, margin + boxWidth, boxTop + boxHeight)

                    pCanvas.drawRoundRect(boxRect, 4f, 4f, bgFramePaint)

                    val optimizedBitmap = decodeOptimizedBitmap(photoPath, targetMaxDimension = 900)
                    if (optimizedBitmap != null) {
                        val bRatio = optimizedBitmap.width.toFloat() / optimizedBitmap.height.toFloat()
                        var dw = boxWidth - 16f
                        var dh = dw / bRatio
                        if (dh > boxHeight - 12f) {
                            dh = boxHeight - 12f
                            dw = dh * bRatio
                        }
                        val drawX = margin + ((boxWidth - dw) / 2f)
                        val drawY = boxTop + ((boxHeight - dh) / 2f)
                        val drawRect = RectF(drawX, drawY, drawX + dw, drawY + dh)

                        pCanvas.drawBitmap(optimizedBitmap, null, drawRect, null)
                        pCanvas.drawRoundRect(boxRect, 4f, 4f, borderPaint)
                    } else {
                        pCanvas.drawRoundRect(boxRect, 4f, 4f, borderPaint)
                        pCanvas.drawText("[Arquivo de fotografia não disponível no armazenamento]", margin + 16f, boxTop + (boxHeight / 2f), captionSubPaint)
                    }

                    val captionY = boxTop + boxHeight + 13f
                    pCanvas.drawText("📍 Coordenadas: ${String.format(Locale.US, "%.6f°, %.6f°", inspection.latitude, inspection.longitude)}  |  🏛️ Região Administrativa: ${inspection.administrativeRegion}", margin, captionY, captionTitlePaint)
                    pCanvas.drawText("🕒 Data/Hora: ${dateText}  |  Registro Georreferenciado • Preservação da Cadeia de Custódia", margin, captionY + 12f, captionSubPaint)
                }

                pCanvas.drawLine(margin, footerY, margin + contentWidth, footerY, linePaint)
                val pGenText = "Folha $pageNumber de $totalPages | Anexo Fotográfico • Fiscalização DF"
                pCanvas.drawText(pGenText, margin, footerY + 12f, footerTextPaint)

                document.finishPage(photoPage)
            }
        }

        val reportsDir = File(context.cacheDir, "reports").apply {
            if (!exists()) mkdirs()
        }
        val outputFile = File(reportsDir, "Levantamento_${inspection.protocolNumber}.pdf")

        return try {
            FileOutputStream(outputFile).use { out ->
                document.writeTo(out)
            }
            document.close()
            outputFile
        } catch (_: Exception) {
            document.close()
            null
        }
    }
}
