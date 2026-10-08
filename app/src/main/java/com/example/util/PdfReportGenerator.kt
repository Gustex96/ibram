package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.DfConstants
import com.example.data.model.HorseInspection
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    /**
     * Decodes and scales an image efficiently to target resolution (~1200px max dimension),
     * preventing multi-megabyte uncompressed bitmaps inside the PDF while preserving
     * razor-sharp 300 DPI print quality in 500x500 point boxes.
     */
    private fun decodeOptimizedBitmap(filePath: String, targetMaxDimension: Int = 1200): Bitmap? {
        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) return null

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
                inPreferredConfig = Bitmap.Config.RGB_565 // Drastically reduces memory and PDF size
            }
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
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

        // PAGE 1: DADOS TÉCNICOS & BOLETIM OFICIAL
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

        // Logo Brasília Ambiental
        val logoWidth = 48f
        val logoHeight = 48f
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_brasilia_ambiental)
            if (logoBitmap != null) {
                val logoRect = RectF(margin, y, margin + logoWidth, y + logoHeight)
                canvas.drawBitmap(logoBitmap, null, logoRect, null)
            }
        } catch (_: Exception) {}

        // Header text
        val textStartX = margin + logoWidth + 12f
        val headerTitlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("GOVERNO DO DISTRITO FEDERAL", textStartX, y + 13f, headerTitlePaint)

        val headerSubPaint = Paint().apply {
            color = Color.rgb(0, 104, 74)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("INSTITUTO BRASÍLIA AMBIENTAL • FISCALIZAÇÃO AMBIENTAL", textStartX, y + 27f, headerSubPaint)

        val headerSmallPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("LEVANTAMENTO OPERACIONAL E MONITORAMENTO DE CAMPO", textStartX, y + 39f, headerSmallPaint)

        y += 52f

        // Document Badge - LEVANTAMENTO E NÚMERO DO PROTOCOLO
        val badgeBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        val badgeRect = RectF(margin, y, margin + contentWidth, y + 24f)
        canvas.drawRoundRect(badgeRect, 5f, 5f, badgeBgPaint)

        val docNamePaint = Paint().apply {
            color = Color.rgb(0, 104, 74)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("LEVANTAMENTO E NÚMERO DO PROTOCOLO", margin + 10f, y + 16f, docNamePaint)

        val protocolPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        val protoText = "Nº ${inspection.protocolNumber}"
        val protoWidth = protocolPaint.measureText(protoText)
        canvas.drawText(protoText, margin + contentWidth - protoWidth - 10f, y + 16f, protocolPaint)

        y += 32f

        val labelPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val col1X = margin + 8f
        val col2X = margin + (contentWidth / 2f) + 8f

        // SECTION 1: LOCALIZAÇÃO, ENDEREÇO DA AÇÃO FISCAL & GEORREFERENCIAMENTO
        y = drawSectionHeader(canvas, "1. DADOS DE LOCALIZAÇÃO, ENDEREÇO DA AÇÃO FISCAL & GPS", margin, y, contentWidth)

        canvas.drawText("REGIÃO ADMINISTRATIVA (RA):", col1X, y, labelPaint)
        canvas.drawText(inspection.administrativeRegion, col1X, y + 11f, valuePaint)

        canvas.drawText("DATA E HORA DA CAPTURA:", col2X, y, labelPaint)
        canvas.drawText(inspection.formattedCaptureDate, col2X, y + 11f, valuePaint)
        y += 26f

        canvas.drawText("ENDEREÇO DA AÇÃO FISCAL:", col1X, y, labelPaint)
        val fullAddrText = buildString {
            append("RA: ${inspection.administrativeRegion}")
            if (inspection.quadra.isNotBlank()) append(" | Quadra: ${inspection.quadra}")
            if (inspection.conjunto.isNotBlank()) append(" | Conjunto: ${inspection.conjunto}")
            if (inspection.numero.isNotBlank()) append(" | Nº: ${inspection.numero}")
        }
        canvas.drawText(fullAddrText, col1X, y + 11f, valuePaint)

        canvas.drawText("ÓRGÃO FISCALIZADOR:", col2X, y, labelPaint)
        canvas.drawText("Brasília Ambiental / Fiscalização Ambiental DF", col2X, y + 11f, valuePaint)
        y += 26f

        canvas.drawText("COORDENADAS GEOGRÁFICAS (GPS):", col1X, y, labelPaint)
        val coordsText = String.format(Locale.US, "Latitude: %.6f° | Longitude: %.6f° (DF)", inspection.latitude, inspection.longitude)
        canvas.drawText(coordsText, col1X, y + 11f, valuePaint)
        y += 28f

        // SECTION 2: QUANTIFICAÇÃO DOS ANIMAIS E CONSTATAÇÕES DE CAMPO (SEM NÍVEIS DE RISCO)
        y = drawSectionHeader(canvas, "2. QUANTIFICAÇÃO DOS ANIMAIS E CONSTATAÇÕES DE CAMPO", margin, y, contentWidth)

        canvas.drawText("QUANTIDADE TOTAL DE ANIMAIS:", col1X, y, labelPaint)
        canvas.drawText("${inspection.horseCount} animal(is)", col1X, y + 11f, valuePaint)

        canvas.drawText("SINAIS DE MAUS-TRATOS:", col2X, y, labelPaint)
        val countText = if (inspection.mistreatedHorseCount > 0) {
            "${inspection.mistreatedHorseCount} animal(is) com sinais observados"
        } else {
            "Nenhum sinal aparente identificado"
        }
        val countValPaint = Paint().apply {
            color = if (inspection.mistreatedHorseCount > 0) Color.rgb(198, 40, 40) else Color.rgb(46, 125, 50)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(countText, col2X, y + 11f, countValPaint)
        y += 25f

        if (inspection.horseDescription.isNotBlank()) {
            canvas.drawText("DESCRIÇÃO DO(S) ANIMAL(IS):", col1X, y, labelPaint)
            y += 10f
            y = drawWrappedText(canvas, inspection.horseDescription, col1X, y, contentWidth - 16f, valuePaint, 11f)
            y += 4f
        }

        if (inspection.mistreatmentList.isNotEmpty()) {
            val alertLabelPaint = Paint().apply {
                color = Color.rgb(198, 40, 40)
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("CONSTATAÇÕES DA AUDITORIA FISCAL DE MAUS-TRATOS (RES. CFMV Nº 1.236/2018):", col1X, y, alertLabelPaint)
            y += 10f
            val alertValPaint = Paint().apply {
                color = Color.rgb(198, 40, 40)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            for (ind in inspection.mistreatmentList) {
                y = drawWrappedText(canvas, "• $ind", col1X + 4f, y, contentWidth - 20f, alertValPaint, 11f)
            }
            y += 4f
        }

        if (inspection.adequateList.isNotEmpty()) {
            val goodLabelPaint = Paint().apply {
                color = Color.rgb(46, 125, 50)
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("CONDIÇÕES ADEQUADAS DE BEM-ESTAR CONSTATADAS (RES. CFMV Nº 1.236/2018):", col1X, y, goodLabelPaint)
            y += 10f
            val goodValPaint = Paint().apply {
                color = Color.rgb(46, 125, 50)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            for (ind in inspection.adequateList) {
                y = drawWrappedText(canvas, "✓ $ind", col1X + 4f, y, contentWidth - 20f, goodValPaint, 11f)
            }
            y += 4f
        }

        // SECTION 3: PROVIDÊNCIAS OPERACIONAIS
        y = drawSectionHeader(canvas, "3. PROVIDÊNCIAS OPERACIONAIS (SEAGRI & PMDF)", margin, y, contentWidth)

        canvas.drawText("APREENSÃO / TRANSPORTE (SEAGRI-DF):", col1X, y, labelPaint)
        val seagriStatus = if (inspection.requiresSeagriApprehension) "SIM (SOLICITADO)" else "NÃO"
        val seagriPaint = if (inspection.requiresSeagriApprehension) {
            Paint().apply { color = Color.rgb(230, 81, 0); textSize = 9f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        } else valuePaint
        canvas.drawText(seagriStatus, col1X, y + 11f, seagriPaint)

        canvas.drawText("APOIO POLÍCIA MILITAR (PMDF / BPMA):", col2X, y, labelPaint)
        val pmdfStatus = if (inspection.requiresPmdfSupport) "SIM (SOLICITADO)" else "NÃO"
        val pmdfPaint = if (inspection.requiresPmdfSupport) {
            Paint().apply { color = Color.rgb(21, 101, 192); textSize = 9f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        } else valuePaint
        canvas.drawText(pmdfStatus, col2X, y + 11f, pmdfPaint)
        y += 24f

        if (inspection.requiresSeagriApprehension && inspection.seagriNotes.isNotBlank()) {
            canvas.drawText("OBSERVAÇÕES DA APREENSÃO:", col1X, y, labelPaint)
            y += 10f
            y = drawWrappedText(canvas, inspection.seagriNotes, col1X, y, contentWidth - 16f, valuePaint, 11f)
            y += 4f
        }
        if (inspection.requiresPmdfSupport && inspection.pmdfNotes.isNotBlank()) {
            canvas.drawText("JUSTIFICATIVA APOIO PMDF:", col1X, y, labelPaint)
            y += 10f
            y = drawWrappedText(canvas, inspection.pmdfNotes, col1X, y, contentWidth - 16f, valuePaint, 11f)
            y += 4f
        }

        // SECTION 4: TUTORES / RESPONSÁVEIS
        val tutors = inspection.allTutors
        y = drawSectionHeader(canvas, "4. IDENTIFICAÇÃO DOS TUTORES E RESPONSÁVEIS (${tutors.size})", margin, y, contentWidth)
        if (tutors.isEmpty()) {
            canvas.drawText("Nenhum tutor ou responsável identificado no local da vistoria.", col1X, y, valuePaint)
            y += 16f
        } else {
            for ((tIdx, tutor) in tutors.withIndex()) {
                val tName = if (tutor.name.isNotBlank()) tutor.name else "Não informado"
                val tCpf = if (tutor.cpf.isNotBlank()) tutor.cpf else "Não informado"
                val docLabel = if (CpfValidator.isCnpj(tutor.cpf)) "CNPJ" else "CPF"
                canvas.drawText("TUTOR #${tIdx + 1}: $tName", col1X, y, valuePaint)
                canvas.drawText("$docLabel: $tCpf", col2X, y, valuePaint)
                y += 14f
            }
        }
        y += 4f

        // SECTION 5: NOTAS DE CAMPO
        if (inspection.imageNotes.isNotBlank()) {
            y = drawSectionHeader(canvas, "5. NOTAS DE CAMPO E OBSERVAÇÕES FINAIS", margin, y, contentWidth)
            y = drawWrappedText(canvas, inspection.imageNotes, col1X, y, contentWidth - 16f, valuePaint, 11f)
            y += 6f
        }

        // SECTION 6: RESUMO FOTOGRÁFICO
        y = drawSectionHeader(canvas, "6. REGISTRO FOTOGRÁFICO GEORREFERENCIADO (${allPhotos.size} FOTO(S))", margin, y, contentWidth)
        if (allPhotos.isEmpty()) {
            canvas.drawText("[Nenhuma fotografia anexada nesta ocorrência]", col1X, y, valuePaint)
            y += 16f
        } else {
            val photoNote = if (allPhotos.size == 1) {
                "✓ 1 fotografia georreferenciada anexada na Folha 2 em alta resolução (proporção 500x500)."
            } else {
                "✓ ${allPhotos.size} fotografias georreferenciadas anexadas nas Folhas seguintes em alta resolução (proporção 500x500)."
            }
            canvas.drawText(photoNote, col1X, y, valuePaint)
            y += 16f
        }

        // FOOTER PAGE 1
        val footerY = pageHeight - 48f
        val linePaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 1f
        }
        canvas.drawLine(margin, footerY, margin + contentWidth, footerY, linePaint)

        val footerTextPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val sdfGen = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
        val genText = "Levantamento expedido em ${sdfGen.format(Date())} | DIFIS-IV • Brasília Ambiental"
        canvas.drawText(genText, margin, footerY + 10f, footerTextPaint)
        val legalNote = "Nota: Subsidia a DIFIS-IV; não substitui o Relatório de Auditoria e Fiscalização (RAF/IBRAM)."
        canvas.drawText(legalNote, margin, footerY + 20f, footerTextPaint)

        val signLineX = margin + contentWidth - 180f
        canvas.drawLine(signLineX, footerY + 22f, margin + contentWidth, footerY + 22f, linePaint)
        canvas.drawText("Assinatura do Agente / Responsável", signLineX + 15f, footerY + 32f, footerTextPaint)

        document.finishPage(page)

        // PAGE 2 (AND BEYOND): FOTOGRAFIAS EM PROPORÇÃO 500x500
        val borderPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val captionTitlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val captionSubPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        if (allPhotos.isNotEmpty()) {
            allPhotos.forEachIndexed { photoIndex, photoPath ->
                val photoPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, photoIndex + 2).create()
                val photoPage = document.startPage(photoPageInfo)
                val pCanvas = photoPage.canvas
                pCanvas.drawColor(Color.WHITE)

                // Header bars
                pCanvas.drawRect(0f, 0f, pageWidth.toFloat(), 12f, greenBarPaint)
                pCanvas.drawRect(0f, 12f, pageWidth.toFloat(), 16f, blueBarPaint)

                var py = 32f
                pCanvas.drawText("ANEXO FOTOGRÁFICO GEORREFERENCIADO • LEVANTAMENTO OPERACIONAL", margin, py, headerSubPaint)
                py += 15f
                pCanvas.drawText("Protocolo: ${inspection.protocolNumber} | RA: ${inspection.administrativeRegion} | Foto ${photoIndex + 1} de ${allPhotos.size}", margin, py, headerSmallPaint)
                py += 20f

                py = drawSectionHeader(pCanvas, "REGISTRO FOTOGRÁFICO FISCAL EM ALTA DEFINIÇÃO (PROPORÇÃO 500x500)", margin, py, contentWidth)

                // 500 x 500 Photo Box (Centered on A4 width 595: (595 - 500) / 2 = 47.5f)
                val targetBoxSize = 500f
                val photoX = (pageWidth - targetBoxSize) / 2f
                val photoRect = RectF(photoX, py, photoX + targetBoxSize, py + targetBoxSize)

                val optimizedBitmap = decodeOptimizedBitmap(photoPath, targetMaxDimension = 1200)
                if (optimizedBitmap != null) {
                    // Draw clean dark background for photo frame
                    val bgFramePaint = Paint().apply { color = Color.rgb(248, 250, 252); style = Paint.Style.FILL }
                    pCanvas.drawRect(photoRect, bgFramePaint)

                    // Draw image scaled nicely into the 500x500 box preserving aspect ratio or fill
                    val bRatio = optimizedBitmap.width.toFloat() / optimizedBitmap.height.toFloat()
                    var dw = targetBoxSize
                    var dh = dw / bRatio
                    if (dh > targetBoxSize) {
                        dh = targetBoxSize
                        dw = dh * bRatio
                    }
                    val drawX = photoX + ((targetBoxSize - dw) / 2f)
                    val drawY = py + ((targetBoxSize - dh) / 2f)
                    val drawRect = RectF(drawX, drawY, drawX + dw, drawY + dh)

                    pCanvas.drawBitmap(optimizedBitmap, null, drawRect, null)
                    pCanvas.drawRect(photoRect, borderPaint)
                } else {
                    pCanvas.drawRect(photoRect, borderPaint)
                    pCanvas.drawText("[Arquivo de fotografia não disponível no armazenamento]", photoX + 20f, py + 250f, valuePaint)
                }

                py += targetBoxSize + 16f

                // Caption details below the 500x500 image
                pCanvas.drawText("📍 Coordenadas: ${String.format(Locale.US, "%.6f°, %.6f° (Distrito Federal)", inspection.latitude, inspection.longitude)}", photoX, py, captionTitlePaint)
                py += 13f
                pCanvas.drawText("🏛️ Região Administrativa: ${inspection.administrativeRegion} • Data/Hora: ${inspection.formattedCaptureDate}", photoX, py, captionSubPaint)
                py += 12f
                pCanvas.drawText("🛡️ Carimbo Fiscalizatório Oficial de Campo • Fiscalização Ambiental DF", photoX, py, captionSubPaint)

                // Footer for photo page
                pCanvas.drawLine(margin, footerY, margin + contentWidth, footerY, linePaint)
                val pGenText = "Folha ${photoIndex + 2} de ${allPhotos.size + 1} | Anexo Fotográfico em Proporção 500x500 | Brasília Ambiental"
                pCanvas.drawText(pGenText, margin, footerY + 12f, footerTextPaint)

                document.finishPage(photoPage)
            }
        }

        val reportsDir = File(context.cacheDir, "reports").apply {
            if (!exists()) mkdirs()
        }
        val outputFile = File(reportsDir, "Levantamento_Protocolo_${inspection.protocolNumber}.pdf")

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

    fun generateConsolidatedPdf(
        context: Context,
        inspections: List<HorseInspection>,
        startDateMillis: Long?,
        endDateMillis: Long?,
        selectedRa: String?
    ): File? {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val contentWidth = pageWidth - (margin * 2)

        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        val periodText = if (startDateMillis != null && endDateMillis != null) {
            "${sdfDate.format(Date(startDateMillis))} até ${sdfDate.format(Date(endDateMillis))}"
        } else {
            "Todos os registros disponíveis"
        }

        val raFilterText = selectedRa ?: "Todas as Regiões Administrativas"

        // PAGE 1: CAPA E RESUMO ESTATÍSTICO
        val coverPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val coverPage = document.startPage(coverPageInfo)
        val coverCanvas = coverPage.canvas
        coverCanvas.drawColor(Color.WHITE)

        val greenBarPaint = Paint().apply { color = Color.rgb(0, 104, 74); style = Paint.Style.FILL }
        val blueBarPaint = Paint().apply { color = Color.rgb(0, 100, 149); style = Paint.Style.FILL }
        coverCanvas.drawRect(0f, 0f, pageWidth.toFloat(), 14f, greenBarPaint)
        coverCanvas.drawRect(0f, 14f, pageWidth.toFloat(), 18f, blueBarPaint)

        var y = 40f

        // Logo
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_brasilia_ambiental)
            if (logoBitmap != null) {
                val logoWidth = 60f
                val logoHeight = 60f
                val logoRect = RectF(margin, y, margin + logoWidth, y + logoHeight)
                coverCanvas.drawBitmap(logoBitmap, null, logoRect, null)
            }
        } catch (_: Exception) {}

        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.rgb(0, 104, 74)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        coverCanvas.drawText("GOVERNO DO DISTRITO FEDERAL", margin + 70f, y + 20f, titlePaint)
        coverCanvas.drawText("INSTITUTO BRASÍLIA AMBIENTAL", margin + 70f, y + 36f, subPaint)

        y += 75f

        // Document Title Box
        val titleBoxPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        coverCanvas.drawRoundRect(RectF(margin, y, margin + contentWidth, y + 48f), 8f, 8f, titleBoxPaint)

        val reportTitlePaint = Paint().apply {
            color = Color.rgb(0, 104, 74)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        coverCanvas.drawText("RELATÓRIO CONSOLIDADO DE LEVANTAMENTO OPERACIONAL", margin + 14f, y + 22f, reportTitlePaint)

        val reportSubPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        coverCanvas.drawText("Dossiê Oficial de Vistorias e Monitoramento com Marca D'Água Georreferenciada", margin + 14f, y + 38f, reportSubPaint)

        y += 65f

        // Section: Filtros Aplicados
        y = drawSectionHeader(coverCanvas, "PARÂMETROS DO RELATÓRIO & FILTROS SELECIONADOS", margin, y, contentWidth)

        val labelPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val valPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        coverCanvas.drawText("PERÍODO ANALISADO:", margin + 8f, y, labelPaint)
        coverCanvas.drawText(periodText, margin + 8f, y + 14f, valPaint)

        coverCanvas.drawText("REGIÃO ADMINISTRATIVA:", margin + (contentWidth / 2f), y, labelPaint)
        coverCanvas.drawText(raFilterText, margin + (contentWidth / 2f), y + 14f, valPaint)
        y += 38f

        // Section: Estatísticas Gerais
        y = drawSectionHeader(coverCanvas, "RESUMO ESTATÍSTICO DAS VISTORIAS", margin, y, contentWidth)

        val totalInspections = inspections.size
        val totalHorses = inspections.sumOf { it.horseCount }
        val mistreatedCount = inspections.sumOf { it.mistreatedHorseCount }
        val seagriCount = inspections.count { it.requiresSeagriApprehension }
        val pmdfCount = inspections.count { it.requiresPmdfSupport }

        coverCanvas.drawText("TOTAL DE REGISTROS DE CAMPO:", margin + 8f, y, labelPaint)
        coverCanvas.drawText("$totalInspections vistoria(s)", margin + 8f, y + 14f, valPaint)

        coverCanvas.drawText("TOTAL DE ANIMAIS IDENTIFICADOS:", margin + (contentWidth / 2f), y, labelPaint)
        coverCanvas.drawText("$totalHorses animal(is)", margin + (contentWidth / 2f), y + 14f, valPaint)
        y += 34f

        coverCanvas.drawText("ANIMAIS COM INDÍCIOS DE MAUS-TRATOS:", margin + 8f, y, labelPaint)
        val mistreatColor = if (mistreatedCount > 0) Color.rgb(198, 40, 40) else Color.rgb(46, 125, 50)
        val mistreatValPaint = Paint().apply {
            color = mistreatColor
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        coverCanvas.drawText("$mistreatedCount animal(is)", margin + 8f, y + 14f, mistreatValPaint)

        coverCanvas.drawText("DEMANDAS DE APREENSÃO (SEAGRI):", margin + (contentWidth / 2f), y, labelPaint)
        coverCanvas.drawText("$seagriCount solicitação(ões)", margin + (contentWidth / 2f), y + 14f, valPaint)
        y += 34f

        coverCanvas.drawText("DEMANDAS DE APOIO POLICIAL (PMDF):", margin + 8f, y, labelPaint)
        coverCanvas.drawText("$pmdfCount solicitação(ões)", margin + 8f, y + 14f, valPaint)
        y += 40f

        // Footer Cover
        val linePaint = Paint().apply { color = Color.rgb(203, 213, 225); strokeWidth = 1f }
        coverCanvas.drawLine(margin, pageHeight - 50f, margin + contentWidth, pageHeight - 50f, linePaint)
        val footerTextPaint = Paint().apply { color = Color.rgb(148, 163, 184); textSize = 8f; isAntiAlias = true }
        val sdfGen = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
        coverCanvas.drawText("Relatório gerado em ${sdfGen.format(Date())} | Brasília Ambiental", margin, pageHeight - 36f, footerTextPaint)
        coverCanvas.drawText("Página 1 de ${inspections.size + 1}", margin + contentWidth - 80f, pageHeight - 36f, footerTextPaint)

        document.finishPage(coverPage)

        // DETAIL PAGES: Each inspection with optimized image
        inspections.forEachIndexed { index, item ->
            val pageNumber = index + 2
            val detailPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val detailPage = document.startPage(detailPageInfo)
            val dCanvas = detailPage.canvas
            dCanvas.drawColor(Color.WHITE)

            dCanvas.drawRect(0f, 0f, pageWidth.toFloat(), 10f, greenBarPaint)
            dCanvas.drawRect(0f, 10f, pageWidth.toFloat(), 13f, blueBarPaint)

            var dy = 32f

            val dHeadPaint = Paint().apply {
                color = Color.rgb(0, 104, 74)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            dCanvas.drawText("REGISTRO DE CAMPO Nº ${index + 1} - PROTOCOLO: ${item.protocolNumber}", margin, dy, dHeadPaint)
            dy += 24f

            dy = drawSectionHeader(dCanvas, "DADOS DE GEORREFERENCIAMENTO & LOCALIZAÇÃO", margin, dy, contentWidth)
            dCanvas.drawText("REGIÃO ADMINISTRATIVA:", margin + 8f, dy, labelPaint)
            dCanvas.drawText(item.administrativeRegion, margin + 8f, dy + 13f, valPaint)

            dCanvas.drawText("DATA DA CAPTURA:", margin + (contentWidth / 2f), dy, labelPaint)
            dCanvas.drawText(item.formattedCaptureDate, margin + (contentWidth / 2f), dy + 13f, valPaint)
            dy += 28f

            dCanvas.drawText("ENDEREÇO DA AÇÃO FISCAL:", margin + 8f, dy, labelPaint)
            val fullDetailAddr = buildString {
                append("RA: ${item.administrativeRegion}")
                if (item.quadra.isNotBlank()) append(" | Q. ${item.quadra}")
                if (item.conjunto.isNotBlank()) append(" | Conj. ${item.conjunto}")
                if (item.numero.isNotBlank()) append(" | Nº ${item.numero}")
            }
            dCanvas.drawText(fullDetailAddr, margin + 8f, dy + 13f, valPaint)
            dy += 28f

            dCanvas.drawText("COORDENADAS (GPS):", margin + 8f, dy, labelPaint)
            dCanvas.drawText(String.format(Locale.US, "%.6f°, %.6f° (DF)", item.latitude, item.longitude), margin + 8f, dy + 13f, valPaint)

            dCanvas.drawText("TOTAL DE ANIMAIS:", margin + (contentWidth / 2f), dy, labelPaint)
            val cText = if (item.mistreatedHorseCount > 0) "${item.horseCount} (${item.mistreatedHorseCount} com maus-tratos)" else "${item.horseCount} (Sem maus-tratos)"
            dCanvas.drawText(cText, margin + (contentWidth / 2f), dy + 13f, valPaint)
            dy += 32f

            if (item.horseDescription.isNotBlank() || item.imageNotes.isNotBlank()) {
                dy = drawSectionHeader(dCanvas, "DESCRIÇÃO DOS ANIMAIS & NOTAS DE CAMPO", margin, dy, contentWidth)
                val combinedNotes = listOf(item.horseDescription, item.imageNotes).filter { it.isNotBlank() }.joinToString(" • ")
                dy = drawWrappedText(dCanvas, combinedNotes, margin + 8f, dy, contentWidth - 16f, valPaint, 12f)
                dy += 6f
            }

            // Section: Registro Fotográfico Otimizado
            dy = drawSectionHeader(dCanvas, "REGISTRO FOTOGRÁFICO GEORREFERENCIADO", margin, dy, contentWidth)

            val photoBitmap = decodeOptimizedBitmap(item.photoPath, targetMaxDimension = 900)
            if (photoBitmap != null) {
                val maxH = 260f
                val maxW = contentWidth - 16f
                val ratio = photoBitmap.width.toFloat() / photoBitmap.height.toFloat()
                var dw = maxW
                var dh = dw / ratio
                if (dh > maxH) {
                    dh = maxH
                    dw = dh * ratio
                }
                val px = margin + ((contentWidth - dw) / 2f)
                val pRect = RectF(px, dy, px + dw, dy + dh)
                val bp = Paint().apply { color = Color.rgb(203, 213, 225); style = Paint.Style.STROKE; strokeWidth = 1f }
                dCanvas.drawRect(pRect, bp)
                dCanvas.drawBitmap(photoBitmap, null, pRect, null)
                dy += dh + 6f
            } else {
                dCanvas.drawText("[Foto arquivada não encontrada]", margin + 8f, dy, valPaint)
                dy += 20f
            }

            // Footer
            val dFooterY = pageHeight - 45f
            dCanvas.drawLine(margin, dFooterY, margin + contentWidth, dFooterY, linePaint)
            dCanvas.drawText("Brasília Ambiental • Dossiê Consolidado • Página $pageNumber de ${inspections.size + 1}", margin, dFooterY + 14f, footerTextPaint)

            document.finishPage(detailPage)
        }

        val reportsDir = File(context.cacheDir, "reports").apply {
            if (!exists()) mkdirs()
        }
        val outputFile = File(reportsDir, "Dossie_Consolidado_LevantamentoOperacional_${System.currentTimeMillis()}.pdf")

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

    private fun drawSectionHeader(canvas: Canvas, title: String, x: Float, y: Float, width: Float): Float {
        val bgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + width, y + 16f, bgPaint)

        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(title, x + 6f, y + 11.5f, titlePaint)
        return y + 23f
    }

    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, startY: Float, maxWidth: Float, paint: Paint, lineSpacing: Float): Float {
        var y = startY
        val words = text.split(" ")
        var currentLine = StringBuilder()

        for (word in words) {
            val potentialLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val textWidth = paint.measureText(potentialLine)
            if (textWidth <= maxWidth) {
                currentLine = StringBuilder(potentialLine)
            } else {
                canvas.drawText(currentLine.toString(), x, y, paint)
                y += lineSpacing
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), x, y, paint)
            y += lineSpacing
        }
        return y
    }

    fun generateAndShareSinglePdf(context: Context, inspection: HorseInspection) {
        val file = generateSingleInspectionPdf(context, inspection)
        if (file != null) {
            sharePdf(
                context = context,
                file = file,
                subject = "Levantamento e número do protocolo - ${inspection.protocolNumber}",
                message = "Segue em anexo o Levantamento referente ao protocolo ${inspection.protocolNumber}."
            )
        } else {
            Toast.makeText(context, "Erro ao gerar PDF", Toast.LENGTH_SHORT).show()
        }
    }

    fun generateAndShareConsolidatedPdf(
        context: Context,
        inspections: List<HorseInspection>,
        startDateMillis: Long?,
        endDateMillis: Long?,
        selectedRa: String?
    ) {
        val file = generateConsolidatedPdf(context, inspections, startDateMillis, endDateMillis, selectedRa)
        if (file != null) {
            sharePdf(
                context = context,
                file = file,
                subject = "Relatório Consolidado - Levantamento Operacional",
                message = "Segue em anexo o Dossiê Oficial Consolidado de Levantamento Operacional."
            )
        } else {
            Toast.makeText(context, "Erro ao gerar relatório consolidado", Toast.LENGTH_SHORT).show()
        }
    }

    fun generateAndShareMultipleIndividualPdfs(context: Context, inspections: List<HorseInspection>) {
        if (inspections.isEmpty()) {
            Toast.makeText(context, "Nenhum relatório selecionado para exportar.", Toast.LENGTH_SHORT).show()
            return
        }
        val generatedFiles = mutableListOf<File>()
        for (item in inspections) {
            val file = generateSingleInspectionPdf(context, item)
            if (file != null) {
                generatedFiles.add(file)
            }
        }
        if (generatedFiles.isEmpty()) {
            Toast.makeText(context, "Erro ao gerar arquivos PDF dos relatórios selecionados.", Toast.LENGTH_SHORT).show()
            return
        }
        if (generatedFiles.size == 1) {
            sharePdf(
                context = context,
                file = generatedFiles[0],
                subject = "Relatório ${inspections[0].protocolNumber} - Levantamento Operacional",
                message = "Segue em anexo o relatório oficial em PDF."
            )
        } else {
            shareMultiplePdfs(
                context = context,
                files = generatedFiles,
                subject = "Exportação de ${generatedFiles.size} Relatórios PDF - Levantamento Operacional",
                message = "Seguem em anexo os ${generatedFiles.size} relatórios oficiais em formato PDF gerados pelo Levantamento Operacional (Brasília Ambiental)."
            )
        }
    }

    fun shareMultiplePdfs(context: Context, files: List<File>, subject: String, message: String) {
        try {
            val uris = ArrayList<android.net.Uri>()
            for (file in files) {
                uris.add(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file))
            }
            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "application/pdf"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Exportar Múltiplos Relatórios PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao compartilhar múltiplos PDFs: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun sharePdf(context: Context, file: File, subject: String, message: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Exportar Relatório PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao compartilhar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
