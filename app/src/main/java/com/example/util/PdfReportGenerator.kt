package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.EnquadramentoMausTratos
import com.example.data.model.GrauBemEstar
import com.example.data.model.HorseInspection
import com.example.data.model.IndividualAnimalEvaluation
import com.example.data.model.ParecerDomain
import com.example.data.model.PpbeaCriteriaRepository
import com.example.data.model.PpbeaDiagnosis
import com.example.data.model.PpbeaDomain
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
                inPreferredConfig = Bitmap.Config.RGB_565 // Drastically reduces memory and PDF size
            }
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } catch (_: Exception) {
            null
        }
    }

    fun cleanEccTerms(text: String): String {
        return text
            .replace(Regex("""\s*[-–]\s*ECC\s*1\s*(ou|a|e)\s*2""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*\(ECC\s*1\s*(ou|a|e)\s*2\s*.*?\)|ECC\s*1\s*(ou|a|e)\s*2""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*[-–]\s*ECC\s*1""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*\(ECC\s*1\)|ECC\s*1""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*\(ECC\s*5\)|ECC\s*5""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*\(ECC\s*[^)]+\)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*[-–]\s*ECC\s*\d+(\s*(a|ou|e)\s*\d+)?\s*[-–]?""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\bECC\s*\d+(\s*(a|ou|e)\s*\d+)?""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\bECC\b""", RegexOption.IGNORE_CASE), "")
            .replace("  ", " ")
            .trim()
    }

    fun formatAnimalCount(count: Int, isDog: Boolean = false): String {
        return if (isDog) {
            if (count == 1) "1 cão" else "$count cães"
        } else {
            if (count == 1) "1 animal" else "$count animais"
        }
    }

    private fun isDomainAdequate(domainName: String, inspection: HorseInspection): Boolean {
        if (inspection.individualEvaluations.isNotEmpty()) {
            val evaluatedAnimals = inspection.individualEvaluations.mapNotNull { it.ppbeaDiagnosis }
            if (evaluatedAnimals.isNotEmpty()) {
                return when (domainName) {
                    "Nutrição" -> evaluatedAnimals.all { it.nutricionalParecer == ParecerDomain.ADEQUADO }
                    "Conforto" -> evaluatedAnimals.all { it.confortoParecer == ParecerDomain.ADEQUADO }
                    "Saúde" -> evaluatedAnimals.all { it.saudeParecer == ParecerDomain.ADEQUADO }
                    "Comportamento" -> evaluatedAnimals.all { it.comportamentalParecer == ParecerDomain.ADEQUADO }
                    else -> true
                }
            }
        }
        val diag = inspection.ppbeaDiagnosis
        if (diag != null) {
            return when (domainName) {
                "Nutrição" -> diag.nutricionalParecer == ParecerDomain.ADEQUADO
                "Conforto" -> diag.confortoParecer == ParecerDomain.ADEQUADO
                "Saúde" -> diag.saudeParecer == ParecerDomain.ADEQUADO
                "Comportamento" -> diag.comportamentalParecer == ParecerDomain.ADEQUADO
                else -> true
            }
        }
        val mList = inspection.mistreatmentList.map { it.lowercase() }
        return when (domainName) {
            "Nutrição" -> !mList.any { it.contains("nutri") || it.contains("magro") || it.contains("água") || it.contains("comida") || it.contains("alimenta") || it.contains("fome") || it.contains("costela") }
            "Conforto" -> !mList.any { it.contains("confort") || it.contains("abrigo") || it.contains("lama") || it.contains("chuva") || it.contains("sol") || it.contains("espaço") || it.contains("amarrado") || it.contains("corrente") || it.contains("piso") }
            "Saúde" -> !mList.any { it.contains("saúde") || it.contains("saude") || it.contains("ferid") || it.contains("lesão") || it.contains("manca") || it.contains("doen") || it.contains("dor") || it.contains("sangue") || it.contains("carrapato") }
            "Comportamento" -> !mList.any { it.contains("comport") || it.contains("medo") || it.contains("agressiv") || it.contains("estresse") || it.contains("apatia") || it.contains("estereotip") }
            else -> true
        }
    }

    private fun calculateEstimatedTextPages(inspection: HorseInspection): Int {
        var estimatedH = 0f
        // Section 1: Loc & GPS
        estimatedH += 145f
        // Section 2: Quant & PPBEA (table + mistreatments)
        estimatedH += 85f
        val mistreatments = inspection.mistreatmentList
            .map { cleanEccTerms(it) }
            .filter { it.isNotBlank() }
            .distinct()
        estimatedH += mistreatments.size * 13f
        // Section 3: Providencias, BO & Apoio
        estimatedH += 65f
        if (inspection.requiresSeagriApprehension && inspection.seagriNotes.isNotBlank()) estimatedH += 25f
        if (inspection.requiresPmdfSupport && inspection.pmdfNotes.isNotBlank()) estimatedH += 25f
        if (inspection.requiresOtherAgencySupport) estimatedH += 35f
        // Section 4: Tutores
        estimatedH += 35f + (inspection.allTutors.size * 15f)
        // Section 5: Diagnostico Geral & Parecer
        if (inspection.individualEvaluations.isNotEmpty()) {
            estimatedH += 45f + (inspection.individualEvaluations.size * 135f)
        } else {
            estimatedH += 85f
            val notesLen = (inspection.horseDescription + " " + (inspection.ppbeaDiagnosis?.observacoes ?: "")).length
            estimatedH += (notesLen / 55) * 12f
        }
        // Section 6: Fotos
        estimatedH += 40f

        val page1Capacity = 640f
        val nextPagesCapacity = 710f
        return if (estimatedH <= page1Capacity) {
            1
        } else {
            1 + Math.ceil(((estimatedH - page1Capacity) / nextPagesCapacity).toDouble()).toInt()
        }
    }

    private class TextPageManager(
        val document: PdfDocument,
        val pageWidth: Int = 595,
        val pageHeight: Int = 842,
        val margin: Float = 36f,
        val contentWidth: Float = 523f,
        val protocolNumber: String,
        val auditorName: String,
        val context: Context,
        var totalPages: Int = 1
    ) {
        var currentPageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
        var currentPage = document.startPage(pageInfo)
        var canvas = currentPage.canvas
        val footerY = pageHeight - 48f
        var y = 32f

        init {
            drawPageDecorations(isFirstPage = true)
        }

        fun ensureSpace(neededHeight: Float) {
            if (y + neededHeight > footerY - 12f) {
                drawFooter()
                document.finishPage(currentPage)
                currentPageNumber++
                if (currentPageNumber > totalPages) {
                    totalPages = currentPageNumber
                }
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                currentPage = document.startPage(pageInfo)
                canvas = currentPage.canvas
                y = 32f
                drawPageDecorations(isFirstPage = false)
            }
        }

        private fun drawPageDecorations(isFirstPage: Boolean) {
            canvas.drawColor(Color.WHITE)
            val greenBarPaint = Paint().apply { color = Color.rgb(0, 104, 74); style = Paint.Style.FILL }
            val blueBarPaint = Paint().apply { color = Color.rgb(0, 100, 149); style = Paint.Style.FILL }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 12f, greenBarPaint)
            canvas.drawRect(0f, 12f, pageWidth.toFloat(), 16f, blueBarPaint)

            if (isFirstPage) {
                val logoWidth = 46f
                val logoHeight = 46f
                try {
                    val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_meio_ambiente)
                    if (logoBitmap != null) {
                        val logoRect = RectF(margin, y, margin + logoWidth, y + logoHeight)
                        val saveCount = canvas.save()
                        val clipPath = Path().apply { addRoundRect(logoRect, 6f, 6f, Path.Direction.CW) }
                        canvas.clipPath(clipPath)
                        canvas.drawBitmap(logoBitmap, null, logoRect, null)
                        canvas.restoreToCount(saveCount)
                    }
                } catch (_: Exception) {}

                val textStartX = margin + logoWidth + 12f
                val headerTitlePaint = Paint().apply {
                    color = Color.rgb(0, 104, 74)
                    textSize = 12f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("RELATÓRIO DE APOIO • FISCALIZAÇÃO AMBIENTAL", textStartX, y + 14f, headerTitlePaint)

                val headerSubPaint = Paint().apply {
                    color = Color.rgb(15, 23, 42)
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("LEVANTAMENTO OPERACIONAL E MONITORAMENTO DE CAMPO", textStartX, y + 27f, headerSubPaint)

                val headerSmallPaint = Paint().apply {
                    color = Color.rgb(100, 116, 139)
                    textSize = 7.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }
                canvas.drawText("DOCUMENTO INDEPENDENTE PARA FINS DE APOIO • SEM VÍNCULO OFICIAL COM O GDF", textStartX, y + 39f, headerSmallPaint)

                y += 50f

                val badgeBgPaint = Paint().apply {
                    color = Color.rgb(241, 245, 249)
                    style = Paint.Style.FILL
                }
                val badgeRect = RectF(margin, y, margin + contentWidth, y + 22f)
                canvas.drawRoundRect(badgeRect, 4f, 4f, badgeBgPaint)

                val docNamePaint = Paint().apply {
                    color = Color.rgb(0, 104, 74)
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("LEVANTAMENTO DE CAMPO • RELATÓRIO GERAL", margin + 10f, y + 15f, docNamePaint)

                val protocolPaint = Paint().apply {
                    color = Color.rgb(15, 23, 42)
                    textSize = 9f
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    isAntiAlias = true
                }
                val protoText = "Nº $protocolNumber"
                val protoWidth = protocolPaint.measureText(protoText)
                canvas.drawText(protoText, margin + contentWidth - protoWidth - 10f, y + 15f, protocolPaint)

                y += 28f
            } else {
                val contPaint = Paint().apply {
                    color = Color.rgb(0, 104, 74)
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("LEVANTAMENTO DE CAMPO Nº $protocolNumber (FOLHA CONTINUAÇÃO)", margin, y + 14f, contPaint)
                y += 24f
            }
        }

        fun drawFooter() {
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
            val genText = "Levantamento expedido em ${sdfGen.format(Date())} • Folha $currentPageNumber de $totalPages"
            canvas.drawText(genText, margin, footerY + 12f, footerTextPaint)
            val legalNote = "Documento gerado por aplicativo para fins de apoio, sem vínculo oficial com o GDF."
            canvas.drawText(legalNote, margin, footerY + 22f, footerTextPaint)

            val signLineX = margin + contentWidth - 200f
            canvas.drawLine(signLineX, footerY + 18f, margin + contentWidth, footerY + 18f, linePaint)
            val auditorSignLabel = if (auditorName.isNotBlank()) auditorName else "Auditor Fiscal de Campo"
            canvas.drawText(auditorSignLabel, signLineX + 6f, footerY + 28f, footerTextPaint)
            canvas.drawText("Auditor Fiscal / Agente Responsável", signLineX + 6f, footerY + 38f, footerTextPaint)
        }

        fun finishCurrentPage() {
            drawFooter()
            document.finishPage(currentPage)
        }
    }

    private fun drawWrappedText(
        pageManager: TextPageManager,
        text: String,
        x: Float,
        maxWidth: Float,
        paint: Paint,
        lineSpacing: Float
    ) {
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            val trimmed = paragraph.trim()
            if (trimmed.isEmpty()) {
                pageManager.ensureSpace(lineSpacing)
                pageManager.y += lineSpacing * 0.6f
                continue
            }
            val words = trimmed.split(Regex("\\s+"))
            var currentLine = StringBuilder()

            for (word in words) {
                val potentialLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val textWidth = paint.measureText(potentialLine)
                if (textWidth <= maxWidth) {
                    currentLine = StringBuilder(potentialLine)
                } else {
                    pageManager.ensureSpace(lineSpacing + 2f)
                    pageManager.canvas.drawText(currentLine.toString(), x, pageManager.y, paint)
                    pageManager.y += lineSpacing
                    currentLine = StringBuilder(word)
                }
            }
            if (currentLine.isNotEmpty()) {
                pageManager.ensureSpace(lineSpacing + 2f)
                pageManager.canvas.drawText(currentLine.toString(), x, pageManager.y, paint)
                pageManager.y += lineSpacing
            }
        }
    }

    private fun countWrappedLines(text: String, maxWidth: Float, paint: Paint): Int {
        var count = 0
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            val trimmed = paragraph.trim()
            if (trimmed.isEmpty()) {
                count++
                continue
            }
            val words = trimmed.split(Regex("\\s+"))
            var currentLine = StringBuilder()
            for (word in words) {
                val potentialLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(potentialLine) <= maxWidth) {
                    currentLine = StringBuilder(potentialLine)
                } else {
                    if (currentLine.isNotEmpty()) count++
                    currentLine = StringBuilder(word)
                }
            }
            if (currentLine.isNotEmpty()) count++
        }
        return maxOf(1, count)
    }

    private fun drawTextLinesOnCanvas(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineSpacing: Float
    ): Float {
        var curY = startY
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            val trimmed = paragraph.trim()
            if (trimmed.isEmpty()) {
                curY += lineSpacing * 0.6f
                continue
            }
            val words = trimmed.split(Regex("\\s+"))
            var currentLine = StringBuilder()
            for (word in words) {
                val potentialLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(potentialLine) <= maxWidth) {
                    currentLine = StringBuilder(potentialLine)
                } else {
                    if (currentLine.isNotEmpty()) {
                        canvas.drawText(currentLine.toString(), x, curY, paint)
                        curY += lineSpacing
                    }
                    currentLine = StringBuilder(word)
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine.toString(), x, curY, paint)
                curY += lineSpacing
            }
        }
        return curY
    }

    private fun drawIndividualAnimalEvaluationInPdf(
        pageManager: TextPageManager,
        index: Int,
        animal: IndividualAnimalEvaluation,
        margin: Float,
        contentWidth: Float,
        col1X: Float,
        col2X: Float
    ) {
        val diag = animal.ppbeaDiagnosis
        val isMausTratos = diag?.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS
        val isRegular = diag?.ocorrenciaMausTratos == EnquadramentoMausTratos.PRECISA_RECOMENDACOES_RETORNO

        // Coleta critérios de alerta/inconformidades do animal específico
        val animalAlerts = mutableListOf<String>()
        val answers = animal.ppbeaAnswersMap
        if (answers.isNotEmpty()) {
            PpbeaCriteriaRepository.criteria.forEach { criterion ->
                if (answers[criterion.id] == criterion.simIsRuim) {
                    val domainPrefix = when (criterion.domain) {
                        PpbeaDomain.NUTRICIONAL -> "[Nutrição]"
                        PpbeaDomain.CONFORTO -> "[Conforto]"
                        PpbeaDomain.SAUDE -> "[Saúde]"
                        PpbeaDomain.COMPORTAMENTAL -> "[Comportamento]"
                    }
                    val critQ = criterion.question.replace("?", "").trim()
                    animalAlerts.add("$domainPrefix $critQ: ${criterion.badStateExplanation}")
                }
            }
        }

        val alertItemP = Paint().apply {
            color = Color.rgb(153, 27, 27)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val noteP = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        // Medição precisa da altura necessária para evitar sobreposições e quebras errôneas
        val textMaxWidth = contentWidth - 20f
        var calculatedAlertsH = 0f
        if (animalAlerts.isNotEmpty()) {
            calculatedAlertsH += 14f // Título "INCONFORMIDADES TÉCNICAS CONSTATADAS:"
            for (alert in animalAlerts) {
                val lines = countWrappedLines("• $alert", textMaxWidth, alertItemP)
                calculatedAlertsH += (lines * 10.5f) + 2f
            }
        } else {
            calculatedAlertsH += 16f // Mensagem de conformidade
        }

        var calculatedNotesH = 0f
        if (animal.notes.isNotBlank()) {
            val lines = countWrappedLines("Observações: ${animal.notes}", textMaxWidth, noteP)
            calculatedNotesH += (lines * 10.5f) + 4f
        }

        val topSectionH = 72f
        val cardTotalH = topSectionH + calculatedAlertsH + calculatedNotesH + 10f

        // Garante que o card inteiro cabe na página atual, sem quebras indesejadas
        pageManager.ensureSpace(cardTotalH + 12f)
        val cardTop = pageManager.y

        val bgPaint = Paint().apply {
            color = when {
                isMausTratos -> Color.rgb(254, 242, 242)
                isRegular -> Color.rgb(255, 251, 235)
                else -> Color.rgb(240, 253, 244)
            }
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = when {
                isMausTratos -> Color.rgb(252, 165, 165)
                isRegular -> Color.rgb(253, 230, 138)
                else -> Color.rgb(187, 247, 208)
            }
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        pageManager.canvas.drawRoundRect(RectF(margin, cardTop, margin + contentWidth, cardTop + cardTotalH), 5f, 5f, bgPaint)
        pageManager.canvas.drawRoundRect(RectF(margin, cardTop, margin + contentWidth, cardTop + cardTotalH), 5f, 5f, borderPaint)

        // 1. Linha Superior: Exemplar + Espécie | TAG / Proprietário
        val titleP = Paint().apply {
            color = when {
                isMausTratos -> Color.rgb(185, 28, 28)
                isRegular -> Color.rgb(180, 83, 9)
                else -> Color.rgb(22, 101, 52)
            }
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val tagP = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        pageManager.canvas.drawText("EXEMPLAR #${index + 1} • ESPÉCIE: ${animal.species.uppercase()}", col1X, cardTop + 14f, titleP)
        val tagLabel = "TAG / PROPRIETÁRIO: ${animal.tagOrCpf.ifBlank { "Sem identificação / TAG informada" }}"
        pageManager.canvas.drawText(tagLabel, col2X, cardTop + 14f, tagP)

        val divPaint = Paint().apply {
            color = borderPaint.color
            strokeWidth = 0.8f
        }
        pageManager.canvas.drawLine(col1X, cardTop + 19f, margin + contentWidth - 8f, cardTop + 19f, divPaint)

        // 2. Grau de Bem-Estar e Enquadramento Legal
        val subDiagP = Paint().apply {
            color = if (isMausTratos) Color.rgb(185, 28, 28) else Color.rgb(51, 65, 85)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val grauStr = "GRAU DE BEM-ESTAR: " + (diag?.grauBemEstar?.label?.uppercase() ?: "PENDENTE")
        val enquadStr = "ENQUADRAMENTO: " + (diag?.ocorrenciaMausTratos?.label ?: "Não avaliado")
        pageManager.canvas.drawText(grauStr, col1X, cardTop + 30f, subDiagP)
        pageManager.canvas.drawText(enquadStr, col2X, cardTop + 30f, subDiagP)

        // 3. Os 4 Domínios do Protocolo PPBEA
        val domCardY = cardTop + 37f
        val domColW = (contentWidth - 24f) / 4f
        val domList = listOf(
            "🥣 Nutrição" to (diag?.nutricionalParecer ?: ParecerDomain.ADEQUADO),
            "🏠 Conforto" to (diag?.confortoParecer ?: ParecerDomain.ADEQUADO),
            "🩺 Saúde" to (diag?.saudeParecer ?: ParecerDomain.ADEQUADO),
            "🧠 Comportamento" to (diag?.comportamentalParecer ?: ParecerDomain.ADEQUADO)
        )

        for (dIdx in domList.indices) {
            val (dTitle, dParecer) = domList[dIdx]
            val dX = margin + 6f + (dIdx * (domColW + 4f))
            val isDomOk = dParecer == ParecerDomain.ADEQUADO
            val isDomReg = dParecer == ParecerDomain.REGULAR

            val domBgP = Paint().apply {
                color = when {
                    isDomOk -> Color.rgb(236, 253, 245)
                    isDomReg -> Color.rgb(254, 249, 195)
                    else -> Color.rgb(254, 226, 226)
                }
                style = Paint.Style.FILL
            }
            val domBorderP = Paint().apply {
                color = when {
                    isDomOk -> Color.rgb(167, 243, 208)
                    isDomReg -> Color.rgb(253, 224, 71)
                    else -> Color.rgb(252, 165, 165)
                }
                style = Paint.Style.STROKE
                strokeWidth = 0.6f
            }
            pageManager.canvas.drawRoundRect(RectF(dX, domCardY, dX + domColW, domCardY + 20f), 3f, 3f, domBgP)
            pageManager.canvas.drawRoundRect(RectF(dX, domCardY, dX + domColW, domCardY + 20f), 3f, 3f, domBorderP)

            val dTitleP = Paint().apply {
                color = Color.rgb(51, 65, 85)
                textSize = 7f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val dStatusP = Paint().apply {
                color = when {
                    isDomOk -> Color.rgb(22, 101, 52)
                    isDomReg -> Color.rgb(180, 83, 9)
                    else -> Color.rgb(185, 28, 28)
                }
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            pageManager.canvas.drawText(dTitle, dX + 4f, domCardY + 8f, dTitleP)
            pageManager.canvas.drawText(dParecer.label.uppercase(), dX + 4f, domCardY + 17f, dStatusP)
        }

        // 4. Critérios e Inconformidades (Desenho posicionado linha a linha)
        var curY = domCardY + 29f
        if (animalAlerts.isNotEmpty()) {
            val alertHeaderP = Paint().apply {
                color = Color.rgb(185, 28, 28)
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            pageManager.canvas.drawText("INCONFORMIDADES TÉCNICAS CONSTATADAS:", col1X, curY, alertHeaderP)
            curY += 10f

            for (alert in animalAlerts) {
                curY = drawTextLinesOnCanvas(pageManager.canvas, "• $alert", col1X + 4f, curY, textMaxWidth, alertItemP, 9.5f)
                curY += 2f
            }
        } else {
            val okP = Paint().apply {
                color = Color.rgb(22, 101, 52)
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val okMsg = if (diag != null)
                "✓ Todos os critérios avaliados encontram-se em conformidade técnica nos 4 domínios."
            else
                "• Avaliação detalhada do Protocolo PPBEA pendente para este exemplar."
            pageManager.canvas.drawText(okMsg, col1X, curY + 2f, okP)
            curY += 12f
        }

        if (animal.notes.isNotBlank()) {
            val noteHeaderP = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            pageManager.canvas.drawText("Observações:", col1X, curY, noteHeaderP)
            curY += 9f
            curY = drawTextLinesOnCanvas(pageManager.canvas, animal.notes, col1X + 4f, curY, textMaxWidth, noteP, 9.5f)
        }

        pageManager.y = cardTop + cardTotalH + 8f
    }

    fun generateSingleInspectionPdf(context: Context, inspection: HorseInspection): File? {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val contentWidth = pageWidth - (margin * 2)

        val allPhotos = inspection.allPhotoPaths
        val isDog = inspection.isDog

        val photoPagesCount = if (allPhotos.isEmpty()) 0 else ((allPhotos.size + 1) / 2)
        val estimatedTextPages = calculateEstimatedTextPages(inspection)
        val totalDocumentPages = estimatedTextPages + photoPagesCount

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

        val pageManager = TextPageManager(
            document = document,
            pageWidth = pageWidth,
            pageHeight = pageHeight,
            margin = margin,
            contentWidth = contentWidth,
            protocolNumber = inspection.protocolNumber,
            auditorName = inspection.auditorName,
            context = context,
            totalPages = totalDocumentPages
        )

        val col1X = margin + 8f
        val col2X = margin + (contentWidth / 2f) + 8f

        // SECTION 1: 1. DADOS DE LOCALIZAÇÃO & GPS
        pageManager.ensureSpace(28f)
        pageManager.y = drawSectionHeader(pageManager.canvas, "1. DADOS DE LOCALIZAÇÃO & GPS", margin, pageManager.y, contentWidth)

        // 1.1 Região Administrativa & Data/Hora
        pageManager.ensureSpace(26f)
        pageManager.canvas.drawText("REGIÃO ADMINISTRATIVA (RA):", col1X, pageManager.y, labelPaint)
        pageManager.canvas.drawText(inspection.administrativeRegion, col1X, pageManager.y + 11f, valuePaint)

        pageManager.canvas.drawText("DATA E HORA DO REGISTRO:", col2X, pageManager.y, labelPaint)
        pageManager.canvas.drawText(inspection.formattedCaptureDate, col2X, pageManager.y + 11f, valuePaint)
        pageManager.y += 24f

        // 1.2 Endereço Residencial / Logradouro
        val addrParts = mutableListOf<String>()
        if (inspection.quadra.isNotBlank()) addrParts.add("Quadra: ${inspection.quadra}")
        if (inspection.conjunto.isNotBlank()) addrParts.add("Conjunto: ${inspection.conjunto}")
        if (inspection.numero.isNotBlank()) addrParts.add("Nº / Lote: ${inspection.numero}")
        val fullAddrText = if (addrParts.isNotEmpty()) {
            addrParts.joinToString("  |  ")
        } else {
            "Área / logradouro público em ${inspection.administrativeRegion} (Sem numeração específica informada)"
        }

        val addrWords = fullAddrText.split(" ")
        val addrLines = mutableListOf<String>()
        var currLine = StringBuilder()
        val maxAddrW = contentWidth - 20f
        for (w in addrWords) {
            val cand = if (currLine.isEmpty()) w else "$currLine $w"
            if (valuePaint.measureText(cand) <= maxAddrW) {
                currLine = StringBuilder(cand)
            } else {
                if (currLine.isNotEmpty()) addrLines.add(currLine.toString())
                currLine = StringBuilder(w)
            }
        }
        if (currLine.isNotEmpty()) addrLines.add(currLine.toString())
        val effectiveLines = if (addrLines.isEmpty()) listOf(fullAddrText) else addrLines

        val boxHeight = 14f + (effectiveLines.size * 11.5f) + 4f
        pageManager.ensureSpace(boxHeight + 10f)

        val addrBoxPaint = Paint().apply { color = Color.rgb(248, 250, 252); style = Paint.Style.FILL }
        val addrBorderPaint = Paint().apply { color = Color.rgb(226, 232, 240); style = Paint.Style.STROKE; strokeWidth = 0.8f }
        val boxTop = pageManager.y - 2f
        pageManager.canvas.drawRoundRect(RectF(margin, boxTop, margin + contentWidth, boxTop + boxHeight), 4f, 4f, addrBoxPaint)
        pageManager.canvas.drawRoundRect(RectF(margin, boxTop, margin + contentWidth, boxTop + boxHeight), 4f, 4f, addrBorderPaint)

        val addrLabelPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val addrTextPaint = Paint(valuePaint).apply { textSize = 8.5f }

        pageManager.canvas.drawText("ENDEREÇO RESIDENCIAL / LOGRADOURO DA AÇÃO FISCAL:", col1X, pageManager.y + 8f, addrLabelPaint)
        var addrDrawY = pageManager.y + 19.5f
        for (line in effectiveLines) {
            pageManager.canvas.drawText(line, col1X, addrDrawY, addrTextPaint)
            addrDrawY += 11.5f
        }
        pageManager.y = boxTop + boxHeight + 8f

        // 1.3 Coordenadas Geográficas (GPS) com Link Clicável & Auditor Fiscal
        pageManager.ensureSpace(34f)
        pageManager.canvas.drawText("COORDENADAS GEOGRÁFICAS (GPS):", col1X, pageManager.y, labelPaint)
        val coordsText = String.format(Locale.US, "%.6f°, %.6f° (SIRGAS 2000 / WGS 84)", inspection.latitude, inspection.longitude)
        val gpsValPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        pageManager.canvas.drawText(coordsText, col1X, pageManager.y + 11f, gpsValPaint)

        val mapsUrl = String.format(Locale.US, "https://www.google.com/maps?q=%.6f,%.6f", inspection.latitude, inspection.longitude)
        val linkPaint = Paint().apply {
            color = Color.rgb(26, 115, 232)
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isUnderlineText = true
            isAntiAlias = true
        }
        pageManager.canvas.drawText(mapsUrl, col1X, pageManager.y + 22f, linkPaint)

        pageManager.canvas.drawText("AUDITOR FISCAL RESPONSÁVEL:", col2X, pageManager.y, labelPaint)
        val auditorReportName = if (inspection.auditorName.isNotBlank()) inspection.auditorName else "Auditor Fiscal de Campo (Equipe Operacional)"
        val audValPaint = Paint().apply {
            color = Color.rgb(0, 104, 74)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        pageManager.canvas.drawText(auditorReportName, col2X, pageManager.y + 11f, audValPaint)
        pageManager.y += 32f

        // 1.4 Espécie Avaliada & Finalidade do Documento
        pageManager.ensureSpace(26f)
        pageManager.canvas.drawText("ESPÉCIE / CATEGORIA AVALIADA:", col1X, pageManager.y, labelPaint)
        val speciesInfoText = if (inspection.individualEvaluations.isNotEmpty()) {
            val speciesList = inspection.individualEvaluations.map { it.species }.distinct()
            "Individualizada (${speciesList.joinToString(", ")})"
        } else if (inspection.isDog) {
            "${inspection.speciesEmoji} Cachorro"
        } else {
            "${inspection.speciesEmoji} Cavalo"
        }
        pageManager.canvas.drawText(speciesInfoText, col1X, pageManager.y + 11f, audValPaint)

        pageManager.canvas.drawText("FINALIDADE DO DOCUMENTO:", col2X, pageManager.y, labelPaint)
        pageManager.canvas.drawText("Apoio em Fiscalização / Bem-Estar Animal", col2X, pageManager.y + 11f, valuePaint)
        pageManager.y += 26f

        // SECTION 2: 2. QUANTIFICAÇÃO E AVALIAÇÃO PPBEA
        pageManager.ensureSpace(28f)
        pageManager.y = drawSectionHeader(pageManager.canvas, "2. QUANTIFICAÇÃO E AVALIAÇÃO PPBEA", margin, pageManager.y, contentWidth)

        pageManager.ensureSpace(26f)
        val quantLabel = if (inspection.individualEvaluations.isNotEmpty()) {
            "ANIMAIS PERICIADOS (INDIVIDUALIZADOS):"
        } else if (isDog) {
            "QUANTIDADE TOTAL DE CÃES:"
        } else {
            "QUANTIDADE TOTAL DE ANIMAIS:"
        }
        pageManager.canvas.drawText(quantLabel, col1X, pageManager.y, labelPaint)
        val quantValue = if (inspection.individualEvaluations.isNotEmpty()) {
            "${inspection.individualEvaluations.size} exemplar(es) periciado(s)"
        } else {
            formatAnimalCount(inspection.horseCount, isDog)
        }
        pageManager.canvas.drawText(quantValue, col1X, pageManager.y + 11f, valuePaint)

        pageManager.canvas.drawText("SINAIS DE MAUS-TRATOS:", col2X, pageManager.y, labelPaint)
        val countText = if (inspection.mistreatedHorseCount > 0) {
            "${formatAnimalCount(inspection.mistreatedHorseCount, isDog)} com sinais observados"
        } else {
            "Nenhum sinal aparente identificado"
        }
        val countValPaint = Paint().apply {
            color = if (inspection.mistreatedHorseCount > 0) Color.rgb(198, 40, 40) else Color.rgb(46, 125, 50)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        pageManager.canvas.drawText(countText, col2X, pageManager.y + 11f, countValPaint)
        pageManager.y += 25f

        // Linha/Tabela Resumo dos 4 Domínios (Nutrição, Conforto, Saúde, Comportamento)
        val domains = listOf(
            "Nutrição" to isDomainAdequate("Nutrição", inspection),
            "Conforto" to isDomainAdequate("Conforto", inspection),
            "Saúde" to isDomainAdequate("Saúde", inspection),
            "Comportamento" to isDomainAdequate("Comportamento", inspection)
        )
        val colW = (contentWidth - 18f) / 4f
        val cardH = 26f
        pageManager.ensureSpace(cardH + 12f)
        val topCardY = pageManager.y
        for (i in domains.indices) {
            val (dName, isOk) = domains[i]
            val cardX = margin + (i * (colW + 6f))
            val bgP = Paint().apply {
                color = if (isOk) Color.rgb(240, 253, 244) else Color.rgb(254, 242, 242)
                style = Paint.Style.FILL
            }
            val borderP = Paint().apply {
                color = if (isOk) Color.rgb(187, 247, 208) else Color.rgb(254, 202, 202)
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }
            pageManager.canvas.drawRoundRect(RectF(cardX, topCardY, cardX + colW, topCardY + cardH), 4f, 4f, bgP)
            pageManager.canvas.drawRoundRect(RectF(cardX, topCardY, cardX + colW, topCardY + cardH), 4f, 4f, borderP)

            val titleP = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val statusP = Paint().apply {
                color = if (isOk) Color.rgb(22, 101, 52) else Color.rgb(185, 28, 28)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            pageManager.canvas.drawText(dName, cardX + 6f, topCardY + 10f, titleP)
            val statusText = if (isOk) "ADEQUADO" else "IRREGULAR"
            pageManager.canvas.drawText(statusText, cardX + 6f, topCardY + 21f, statusP)
        }
        pageManager.y = topCardY + cardH + 10f

        val cleanedMistreatments = inspection.mistreatmentList
            .map { cleanEccTerms(it) }
            .map { it.replace(Regex("""^\[.*?\]\s*"""), "").replace("?", "").trim() }
            .filter { it.isNotBlank() }
            .distinct()

        if (cleanedMistreatments.isNotEmpty()) {
            pageManager.ensureSpace(22f)
            val alertLabelPaint = Paint().apply {
                color = Color.rgb(198, 40, 40)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            pageManager.canvas.drawText("INCONFORMIDADES / ALERTAS CONSTATADOS (PROTOCOLO PPBEA):", col1X, pageManager.y, alertLabelPaint)
            pageManager.y += 11f
            val alertValPaint = Paint().apply {
                color = Color.rgb(185, 28, 28)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            for (ind in cleanedMistreatments) {
                drawWrappedText(pageManager, "• $ind", col1X + 4f, contentWidth - 16f, alertValPaint, 11.5f)
            }
            pageManager.y += 4f
        } else {
            pageManager.ensureSpace(14f)
            val goodValPaint = Paint().apply {
                color = Color.rgb(46, 125, 50)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            pageManager.canvas.drawText("✓ Nenhuma inconformidade técnica de maus-tratos apontada nos domínios do Protocolo PPBEA.", col1X, pageManager.y, goodValPaint)
            pageManager.y += 14f
        }

        // SECTION 3: 3. PROVIDÊNCIAS OPERACIONAIS, BO E APOIO
        pageManager.ensureSpace(30f)
        pageManager.y = drawSectionHeader(pageManager.canvas, "3. PROVIDÊNCIAS OPERACIONAIS, BO E APOIO", margin, pageManager.y, contentWidth)

        val col3W = (contentWidth - 12f) / 3f
        val c1 = margin + 4f
        val c2 = margin + col3W + 6f
        val c3 = margin + (col3W * 2f) + 8f

        val boStatus = if (inspection.ppbeaDiagnosis?.gerouBoletimOcorrencia == true) "SIM (REGISTRADO)" else "NÃO"
        val boPaint = Paint().apply {
            color = if (inspection.ppbeaDiagnosis?.gerouBoletimOcorrencia == true) Color.rgb(198, 40, 40) else Color.rgb(46, 125, 50)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val seagriStatus = if (isDog) "N/A (NÃO APLICÁVEL)" else if (inspection.requiresSeagriApprehension) "SIM (SOLICITADO)" else "NÃO"
        val seagriPaint = if (inspection.requiresSeagriApprehension) {
            Paint().apply { color = Color.rgb(230, 81, 0); textSize = 8.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        } else valuePaint

        val pmdfStatus = if (inspection.requiresPmdfSupport) "SIM (SOLICITADO)" else "NÃO"
        val pmdfPaint = if (inspection.requiresPmdfSupport) {
            Paint().apply { color = Color.rgb(21, 101, 192); textSize = 8.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        } else valuePaint

        pageManager.ensureSpace(28f)
        pageManager.canvas.drawText("BOLETIM DE OCORRÊNCIA (BO):", c1, pageManager.y, labelPaint)
        pageManager.canvas.drawText(boStatus, c1, pageManager.y + 11f, boPaint)

        pageManager.canvas.drawText("APREENSÃO (SEAGRI-DF):", c2, pageManager.y, labelPaint)
        pageManager.canvas.drawText(seagriStatus, c2, pageManager.y + 11f, seagriPaint)

        pageManager.canvas.drawText("APOIO POLICIAL (PMDF/BPMA):", c3, pageManager.y, labelPaint)
        pageManager.canvas.drawText(pmdfStatus, c3, pageManager.y + 11f, pmdfPaint)
        pageManager.y += 26f

        if (inspection.requiresSeagriApprehension && inspection.seagriNotes.isNotBlank()) {
            pageManager.ensureSpace(18f)
            pageManager.canvas.drawText("OBSERVAÇÕES DA APREENSÃO:", col1X, pageManager.y, labelPaint)
            pageManager.y += 10f
            drawWrappedText(pageManager, inspection.seagriNotes, col1X, contentWidth - 16f, valuePaint, 11f)
            pageManager.y += 4f
        }
        if (inspection.requiresPmdfSupport && inspection.pmdfNotes.isNotBlank()) {
            pageManager.ensureSpace(18f)
            pageManager.canvas.drawText("JUSTIFICATIVA APOIO PMDF / BPMA:", col1X, pageManager.y, labelPaint)
            pageManager.y += 10f
            drawWrappedText(pageManager, inspection.pmdfNotes, col1X, contentWidth - 16f, valuePaint, 11f)
            pageManager.y += 4f
        }
        if (inspection.requiresOtherAgencySupport) {
            val otherStatus = "SIM (SOLICITADO)"
            val otherPaint = Paint().apply { color = Color.rgb(106, 27, 154); textSize = 8.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
            pageManager.ensureSpace(24f)
            pageManager.canvas.drawText("REQUISIÇÃO DE OUTRO ÓRGÃO PÚBLICO:", col1X, pageManager.y, labelPaint)
            pageManager.canvas.drawText(otherStatus, col1X, pageManager.y + 11f, otherPaint)

            if (inspection.otherAgencyName.isNotBlank()) {
                pageManager.canvas.drawText("ÓRGÃO REQUISITADO:", col2X, pageManager.y, labelPaint)
                pageManager.canvas.drawText(inspection.otherAgencyName, col2X, pageManager.y + 11f, valuePaint)
            }
            pageManager.y += 24f

            if (inspection.otherAgencyNotes.isNotBlank()) {
                pageManager.ensureSpace(18f)
                pageManager.canvas.drawText("PROVIDÊNCIAS REQUERIDAS / JUSTIFICATIVA:", col1X, pageManager.y, labelPaint)
                pageManager.y += 10f
                drawWrappedText(pageManager, inspection.otherAgencyNotes, col1X, contentWidth - 16f, valuePaint, 11f)
                pageManager.y += 4f
            }
        }

        // SECTION 4: 4. IDENTIFICAÇÃO DOS TUTORES
        val tutors = inspection.allTutors
        pageManager.ensureSpace(30f)
        pageManager.y = drawSectionHeader(pageManager.canvas, "4. IDENTIFICAÇÃO DOS TUTORES (${tutors.size})", margin, pageManager.y, contentWidth)
        if (tutors.isEmpty()) {
            pageManager.ensureSpace(16f)
            pageManager.canvas.drawText("Nenhum tutor ou responsável identificado no local da vistoria.", col1X, pageManager.y, valuePaint)
            pageManager.y += 16f
        } else {
            for ((tIdx, tutor) in tutors.withIndex()) {
                pageManager.ensureSpace(16f)
                val tName = tutor.name.ifBlank { "Não informado" }
                val tCpf = if (tutor.cpf.isNotBlank()) tutor.cpf else "Não informado"
                val docLabel = if (CpfValidator.isCnpj(tutor.cpf)) "CNPJ" else "CPF"
                pageManager.canvas.drawText("TUTOR #${tIdx + 1}: $tName", col1X, pageManager.y, valuePaint)
                pageManager.canvas.drawText("$docLabel: $tCpf", col2X, pageManager.y, valuePaint)
                pageManager.y += 14f
            }
        }
        pageManager.y += 4f

        // SECTION 5: 5. DIAGNÓSTICO GERAL & PARECER TÉCNICO
        pageManager.ensureSpace(34f)
        val section5Title = if (inspection.individualEvaluations.isNotEmpty()) {
            "5. DIAGNÓSTICO E PARECER TÉCNICO INDIVIDUALIZADO (${inspection.individualEvaluations.size} ANIMAIS)"
        } else {
            "5. DIAGNÓSTICO GERAL & PARECER TÉCNICO"
        }
        pageManager.y = drawSectionHeader(pageManager.canvas, section5Title, margin, pageManager.y, contentWidth)

        val relatorioGeral = cleanEccTerms(
            inspection.horseDescription.trim().ifBlank {
                inspection.ppbeaDiagnosis?.observacoes?.trim() ?: ""
            }
        )
        val extraNotes = cleanEccTerms(
            inspection.imageNotes.trim().takeIf { it.isNotBlank() && it != relatorioGeral } ?: ""
        )

        // Se houver parecer técnico geral preenchido, desenha primeiro
        if (relatorioGeral.isNotBlank() && relatorioGeral != "Sem parecer descritivo adicional preenchido pelo auditor de campo na fiscalização.") {
            pageManager.ensureSpace(36f)
            val parecP = Paint().apply {
                color = Color.rgb(0, 104, 74)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            pageManager.canvas.drawText("PARECER TÉCNICO GERAL DA FISCALIZAÇÃO:", col1X, pageManager.y + 10f, parecP)
            pageManager.y += 18f
            drawWrappedText(pageManager, relatorioGeral, col1X, contentWidth - 16f, valuePaint, 11f)
            pageManager.y += 6f
        }

        if (inspection.individualEvaluations.isNotEmpty()) {
            // AVALIAÇÃO INDIVIDUALIZADA: Desenha cada exemplar com seus dados periciais
            for ((idx, animal) in inspection.individualEvaluations.withIndex()) {
                drawIndividualAnimalEvaluationInPdf(
                    pageManager = pageManager,
                    index = idx,
                    animal = animal,
                    margin = margin,
                    contentWidth = contentWidth,
                    col1X = col1X,
                    col2X = col2X
                )
            }

            if (extraNotes.isNotBlank()) {
                pageManager.ensureSpace(28f)
                val noteLabelPaint = Paint().apply {
                    color = Color.rgb(71, 85, 105)
                    textSize = 8f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                pageManager.canvas.drawText("NOTAS COMPLEMENTARES:", col1X, pageManager.y + 6f, noteLabelPaint)
                pageManager.y += 16f
                drawWrappedText(pageManager, extraNotes, col1X, contentWidth - 20f, valuePaint, 11f)
                pageManager.y += 6f
            }
        } else {
            // AVALIAÇÃO GERAL POR AMOSTRAGEM (SEM INDIVIDUALIZAR OS ANIMAIS)
            val diag = inspection.ppbeaDiagnosis
            val isMausTratos = (diag?.ocorrenciaMausTratos == EnquadramentoMausTratos.CONFIGURA_MAUS_TRATOS) ||
                               (diag == null && inspection.mistreatedHorseCount > 0)
            val grauBemEstarLabel = diag?.grauBemEstar?.label?.uppercase() ?: (if (inspection.mistreatedHorseCount > 0) "BAIXO / COMPROMETIDO" else "ALTO / ADEQUADO")
            val enquadramentoLabel = if (isMausTratos) {
                "CONFIGURA MAUS-TRATOS (Lei nº 9.605/1998 e Res. CFMV nº 1.236/2018)"
            } else if (diag?.ocorrenciaMausTratos == EnquadramentoMausTratos.PRECISA_RECOMENDACOES_RETORNO) {
                "NÃO CONFIGURA MAUS-TRATOS (Recomendações e Retorno Necessários)"
            } else {
                "NÃO CONFIGURA MAUS-TRATOS"
            }

            val boxBgPaint = Paint().apply {
                color = if (isMausTratos) Color.rgb(254, 242, 242) else Color.rgb(240, 253, 244)
                style = Paint.Style.FILL
            }
            val boxBorderPaint = Paint().apply {
                color = if (isMausTratos) Color.rgb(252, 165, 165) else Color.rgb(187, 247, 208)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            val grauColor = if (isMausTratos) Color.rgb(185, 28, 28) else Color.rgb(22, 101, 52)
            val grauPaint = Paint().apply {
                color = grauColor
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val enquadPaint = Paint().apply {
                color = if (isMausTratos) Color.rgb(185, 28, 28) else Color.rgb(22, 101, 52)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val descContentText = if (relatorioGeral.isNotBlank()) {
                relatorioGeral
            } else {
                "Sem parecer descritivo adicional preenchido pelo auditor de campo na fiscalização."
            }

            val parecerTitlePaint = Paint().apply {
                color = Color.rgb(0, 104, 74)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val descBodyPaint = Paint(valuePaint).apply { textSize = 9f }

            val descWords = descContentText.split(Regex("\\s+"))
            var lineCount = 1
            var curLine = ""
            for (w in descWords) {
                val cand = if (curLine.isEmpty()) w else "$curLine $w"
                if (descBodyPaint.measureText(cand) <= contentWidth - 20f) {
                    curLine = cand
                } else {
                    lineCount++
                    curLine = w
                }
            }
            val extraLines = if (extraNotes.isNotBlank()) {
                val eWords = extraNotes.split(Regex("\\s+"))
                var eCount = 1
                var eLine = ""
                for (w in eWords) {
                    val cand = if (eLine.isEmpty()) w else "$eLine $w"
                    if (valuePaint.measureText(cand) <= contentWidth - 20f) {
                        eLine = cand
                    } else {
                        eCount++
                        eLine = w
                    }
                }
                eCount + 2
            } else 0

            val totalBoxHeight = 44f + (lineCount * 12f) + (extraLines * 11.5f) + 14f
            pageManager.ensureSpace(totalBoxHeight + 8f)
            val diagBoxTop = pageManager.y
            pageManager.canvas.drawRoundRect(RectF(margin, diagBoxTop, margin + contentWidth, diagBoxTop + totalBoxHeight), 6f, 6f, boxBgPaint)
            pageManager.canvas.drawRoundRect(RectF(margin, diagBoxTop, margin + contentWidth, diagBoxTop + totalBoxHeight), 6f, 6f, boxBorderPaint)

            pageManager.canvas.drawText("GRAU DE BEM-ESTAR ANIMAL: $grauBemEstarLabel", col1X, diagBoxTop + 14f, grauPaint)
            pageManager.canvas.drawText("ENQUADRAMENTO: $enquadramentoLabel", col1X, diagBoxTop + 27f, enquadPaint)

            val divPaint = Paint().apply { color = if (isMausTratos) Color.rgb(252, 165, 165) else Color.rgb(187, 247, 208); strokeWidth = 0.8f }
            pageManager.canvas.drawLine(col1X, diagBoxTop + 33f, margin + contentWidth - 8f, diagBoxTop + 33f, divPaint)

            pageManager.canvas.drawText("PARECER TÉCNICO E OBSERVAÇÕES PERICIAIS DE CAMPO:", col1X, diagBoxTop + 45f, parecerTitlePaint)
            pageManager.y = diagBoxTop + 57f

            drawWrappedText(pageManager, descContentText, col1X, contentWidth - 20f, descBodyPaint, 12f)
            if (extraNotes.isNotBlank()) {
                val noteLabelPaint = Paint().apply {
                    color = Color.rgb(71, 85, 105)
                    textSize = 8f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                pageManager.canvas.drawText("NOTAS COMPLEMENTARES:", col1X, pageManager.y + 6f, noteLabelPaint)
                pageManager.y += 16f
                drawWrappedText(pageManager, extraNotes, col1X, contentWidth - 20f, valuePaint, 11f)
            }
            pageManager.y = diagBoxTop + totalBoxHeight + 10f
        }

        // SECTION 6: 6. REGISTRO FOTOGRÁFICO GEORREFERENCIADO
        pageManager.ensureSpace(30f)
        pageManager.y = drawSectionHeader(pageManager.canvas, "6. REGISTRO FOTOGRÁFICO GEORREFERENCIADO (${allPhotos.size} FOTO(S))", margin, pageManager.y, contentWidth)
        pageManager.ensureSpace(16f)
        val photoNote = if (allPhotos.isEmpty()) {
            "[Nenhuma fotografia anexada nesta ocorrência]"
        } else if (allPhotos.size == 1) {
            "✓ 1 fotografia georreferenciada anexada na folha seguinte em alta resolução."
        } else {
            "✓ ${allPhotos.size} fotografias georreferenciadas anexadas nas folhas seguintes em alta resolução."
        }
        pageManager.canvas.drawText(photoNote, col1X, pageManager.y, valuePaint)
        pageManager.y += 16f

        pageManager.finishCurrentPage()

        // PAGE 2 (AND BEYOND): FOTOGRAFIAS EM ALTA RESOLUÇÃO (ATÉ 2 FOTOS POR PÁGINA)
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

        if (allPhotos.isNotEmpty()) {
            val photoChunks = allPhotos.chunked(2)
            val startPhotoPageNumber = pageManager.currentPageNumber + 1

            photoChunks.forEachIndexed { pageChunkIndex, chunk ->
                val currentPhotoPageNumber = startPhotoPageNumber + pageChunkIndex
                val photoPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPhotoPageNumber).create()
                val photoPage = document.startPage(photoPageInfo)
                val pCanvas = photoPage.canvas
                pCanvas.drawColor(Color.WHITE)

                val greenBarPaint = Paint().apply { color = Color.rgb(0, 104, 74); style = Paint.Style.FILL }
                val blueBarPaint = Paint().apply { color = Color.rgb(0, 100, 149); style = Paint.Style.FILL }
                pCanvas.drawRect(0f, 0f, pageWidth.toFloat(), 12f, greenBarPaint)
                pCanvas.drawRect(0f, 12f, pageWidth.toFloat(), 16f, blueBarPaint)

                var py = 32f
                val headerSubPaint = Paint().apply {
                    color = Color.rgb(15, 23, 42)
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                val headerSmallPaint = Paint().apply {
                    color = Color.rgb(100, 116, 139)
                    textSize = 7.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }
                pCanvas.drawText("ANEXO FOTOGRÁFICO GEORREFERENCIADO • LEVANTAMENTO OPERACIONAL", margin, py, headerSubPaint)
                py += 15f
                pCanvas.drawText("Levantamento: ${inspection.protocolNumber} • RA: ${inspection.administrativeRegion} • ${inspection.formattedCaptureDate}", margin, py, headerSmallPaint)
                py += 20f

                py = drawSectionHeader(pCanvas, "REGISTRO FOTOGRÁFICO FISCAL EM ALTA RESOLUÇÃO", margin, py, contentWidth)

                if (chunk.size == 1) {
                    val globalPhotoIdx = pageChunkIndex * 2
                    val photoPath = chunk[0]
                    val maxW = 500f
                    val maxH = 460f
                    val optBitmap = decodeOptimizedBitmap(photoPath, targetMaxDimension = 1200)

                    val photoX = (pageWidth - maxW) / 2f
                    val photoRect = RectF(photoX, py, photoX + maxW, py + maxH)
                    val bgFramePaint = Paint().apply { color = Color.rgb(248, 250, 252); style = Paint.Style.FILL }
                    pCanvas.drawRect(photoRect, bgFramePaint)

                    if (optBitmap != null) {
                        val bRatio = optBitmap.width.toFloat() / optBitmap.height.toFloat()
                        var dw = maxW
                        var dh = dw / bRatio
                        if (dh > maxH) {
                            dh = maxH
                            dw = dh * bRatio
                        }
                        val drawX = photoX + ((maxW - dw) / 2f)
                        val drawY = py + ((maxH - dh) / 2f)
                        val drawRect = RectF(drawX, drawY, drawX + dw, drawY + dh)
                        pCanvas.drawBitmap(optBitmap, null, drawRect, null)
                        pCanvas.drawRect(photoRect, borderPaint)
                    } else {
                        pCanvas.drawRect(photoRect, borderPaint)
                        pCanvas.drawText("[Arquivo de fotografia não disponível no armazenamento]", photoX + 20f, py + (maxH / 2f), valuePaint)
                    }

                    py += maxH + 16f
                    // Clean caption below photo without repetition of RA/Date/Coords
                    val caption = "Evidência Fotográfica #${globalPhotoIdx + 1} — Protocolo ${inspection.protocolNumber}"
                    pCanvas.drawText(caption, photoX, py, captionTitlePaint)
                } else {
                    // 2 photos on the page: stacked vertically
                    val slotMaxH = 280f
                    val slotMaxW = 490f
                    val photoX = (pageWidth - slotMaxW) / 2f

                    for ((subIdx, photoPath) in chunk.withIndex()) {
                        val globalPhotoIdx = (pageChunkIndex * 2) + subIdx
                        val optBitmap = decodeOptimizedBitmap(photoPath, targetMaxDimension = 1000)

                        val photoRect = RectF(photoX, py, photoX + slotMaxW, py + slotMaxH)
                        val bgFramePaint = Paint().apply { color = Color.rgb(248, 250, 252); style = Paint.Style.FILL }
                        pCanvas.drawRect(photoRect, bgFramePaint)

                        if (optBitmap != null) {
                            val bRatio = optBitmap.width.toFloat() / optBitmap.height.toFloat()
                            var dw = slotMaxW
                            var dh = dw / bRatio
                            if (dh > slotMaxH) {
                                dh = slotMaxH
                                dw = dh * bRatio
                            }
                            val drawX = photoX + ((slotMaxW - dw) / 2f)
                            val drawY = py + ((slotMaxH - dh) / 2f)
                            val drawRect = RectF(drawX, drawY, drawX + dw, drawY + dh)
                            pCanvas.drawBitmap(optBitmap, null, drawRect, null)
                            pCanvas.drawRect(photoRect, borderPaint)
                        } else {
                            pCanvas.drawRect(photoRect, borderPaint)
                            pCanvas.drawText("[Arquivo de fotografia não disponível]", photoX + 20f, py + (slotMaxH / 2f), valuePaint)
                        }

                        py += slotMaxH + 12f
                        val caption = "Evidência Fotográfica #${globalPhotoIdx + 1} — Protocolo ${inspection.protocolNumber}"
                        pCanvas.drawText(caption, photoX, py, captionTitlePaint)
                        py += 24f
                    }
                }

                val footerY = pageHeight - 48f
                val linePaint = Paint().apply { color = Color.rgb(203, 213, 225); strokeWidth = 1f }
                pCanvas.drawLine(margin, footerY, margin + contentWidth, footerY, linePaint)
                val footerTextPaint = Paint().apply { color = Color.rgb(148, 163, 184); textSize = 7.5f; isAntiAlias = true }
                val pGenText = "Folha $currentPhotoPageNumber de $totalDocumentPages • Anexo Fotográfico • Relatório de Apoio"
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
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_meio_ambiente)
            if (logoBitmap != null) {
                val logoWidth = 60f
                val logoHeight = 60f
                val logoRect = RectF(margin, y, margin + logoWidth, y + logoHeight)
                val saveCount = coverCanvas.save()
                val clipPath = Path().apply { addRoundRect(logoRect, 8f, 8f, Path.Direction.CW) }
                coverCanvas.clipPath(clipPath)
                coverCanvas.drawBitmap(logoBitmap, null, logoRect, null)
                coverCanvas.restoreToCount(saveCount)
            }
        } catch (_: Exception) {}

        val titlePaint = Paint().apply {
            color = Color.rgb(0, 104, 74)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        coverCanvas.drawText("RELATÓRIO DE APOIO", margin + 70f, y + 20f, titlePaint)
        coverCanvas.drawText("LEVANTAMENTO DE CAMPO E BEM-ESTAR ANIMAL", margin + 70f, y + 36f, subPaint)

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
        coverCanvas.drawText("Dossiê de Vistorias e Monitoramento com Registro Georreferenciado", margin + 14f, y + 38f, reportSubPaint)

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
        y += 34f

        val distinctAuditors = inspections.mapNotNull { it.auditorName.takeIf { a -> a.isNotBlank() } }.distinct()
        val auditorSummaryText = if (distinctAuditors.isNotEmpty()) distinctAuditors.joinToString(", ") else "Equipe de Apoio em Fiscalização"
        coverCanvas.drawText("AUDITOR(ES) FISCAL(IS) RESPONSÁVEL(IS):", margin + 8f, y, labelPaint)
        coverCanvas.drawText(auditorSummaryText, margin + 8f, y + 14f, valPaint)
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
        coverCanvas.drawText(formatAnimalCount(totalHorses, false), margin + (contentWidth / 2f), y + 14f, valPaint)
        y += 34f

        coverCanvas.drawText("ANIMAIS COM INDÍCIOS DE MAUS-TRATOS:", margin + 8f, y, labelPaint)
        val mistreatColor = if (mistreatedCount > 0) Color.rgb(198, 40, 40) else Color.rgb(46, 125, 50)
        val mistreatValPaint = Paint().apply {
            color = mistreatColor
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val mistreatSummaryText = if (mistreatedCount == 1) "1 animal" else "$mistreatedCount animais"
        coverCanvas.drawText(mistreatSummaryText, margin + 8f, y + 14f, mistreatValPaint)

        coverCanvas.drawText("DEMANDAS DE APREENSÃO (SEAGRI):", margin + (contentWidth / 2f), y, labelPaint)
        coverCanvas.drawText("$seagriCount solicitação(ões)", margin + (contentWidth / 2f), y + 14f, valPaint)
        y += 34f

        coverCanvas.drawText("DEMANDAS DE APOIO POLICIAL (PMDF):", margin + 8f, y, labelPaint)
        coverCanvas.drawText("$pmdfCount solicitação(ões)", margin + 8f, y + 14f, valPaint)

        val otherAgencyCount = inspections.count { it.requiresOtherAgencySupport }
        coverCanvas.drawText("OUTROS ÓRGÃOS / PROVIDÊNCIAS:", margin + (contentWidth / 2f), y, labelPaint)
        coverCanvas.drawText("$otherAgencyCount solicitação(ões)", margin + (contentWidth / 2f), y + 14f, valPaint)
        y += 40f

        // Footer Cover
        val linePaint = Paint().apply { color = Color.rgb(203, 213, 225); strokeWidth = 1f }
        coverCanvas.drawLine(margin, pageHeight - 50f, margin + contentWidth, pageHeight - 50f, linePaint)
        val footerTextPaint = Paint().apply { color = Color.rgb(148, 163, 184); textSize = 8f; isAntiAlias = true }
        val sdfGen = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", java.util.Locale("pt", "BR"))
        coverCanvas.drawText("Relatório gerado em ${sdfGen.format(Date())} • Apoio em Fiscalização", margin, pageHeight - 36f, footerTextPaint)
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
            dCanvas.drawText("REGISTRO DE CAMPO Nº ${index + 1} - LEVANTAMENTO: ${item.protocolNumber}", margin, dy, dHeadPaint)
            dy += 14f
            val itemAuditor = if (item.auditorName.isNotBlank()) item.auditorName else "Não informado"
            val dAuditorPaint = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            dCanvas.drawText("Auditor Fiscal: $itemAuditor • Espécie: ${item.speciesDisplay}", margin, dy, dAuditorPaint)
            dy += 16f

            dy = drawSectionHeader(dCanvas, "DADOS DE GEORREFERENCIAMENTO & LOCALIZAÇÃO", margin, dy, contentWidth)
            dCanvas.drawText("REGIÃO ADMINISTRATIVA:", margin + 8f, dy, labelPaint)
            dCanvas.drawText(item.administrativeRegion, margin + 8f, dy + 13f, valPaint)

            dCanvas.drawText("DATA DO REGISTRO:", margin + (contentWidth / 2f), dy, labelPaint)
            dCanvas.drawText(item.formattedCaptureDate, margin + (contentWidth / 2f), dy + 13f, valPaint)
            dy += 26f

            // Endereço Residencial em bloco contido e protegido contra sobreposição
            val dAddrBoxPaint = Paint().apply {
                color = Color.rgb(248, 250, 252)
                style = Paint.Style.FILL
            }
            val dAddrBorderPaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }
            val fullDetailAddr = buildString {
                val parts = mutableListOf<String>()
                if (item.quadra.isNotBlank()) parts.add("Quadra: ${item.quadra}")
                if (item.conjunto.isNotBlank()) parts.add("Conjunto: ${item.conjunto}")
                if (item.numero.isNotBlank()) parts.add("Nº / Lote: ${item.numero}")
                if (parts.isNotEmpty()) append(parts.joinToString("  |  "))
                else append("Área / logradouro público em ${item.administrativeRegion}")
            }

            val dAddrLabelPaint = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val dAddrTextPaint = Paint(valPaint).apply {
                textSize = 8.5f
            }

            val dWords = fullDetailAddr.split(" ")
            val dLines = mutableListOf<String>()
            var dCurr = StringBuilder()
            val dMaxW = contentWidth - 20f
            for (w in dWords) {
                val cand = if (dCurr.isEmpty()) w else "$dCurr $w"
                if (dAddrTextPaint.measureText(cand) <= dMaxW) {
                    dCurr = StringBuilder(cand)
                } else {
                    if (dCurr.isNotEmpty()) dLines.add(dCurr.toString())
                    dCurr = StringBuilder(w)
                }
            }
            if (dCurr.isNotEmpty()) dLines.add(dCurr.toString())
            val dEffectiveLines = if (dLines.isEmpty()) listOf(fullDetailAddr) else dLines

            val dBoxTop = dy - 2f
            val dBoxH = 14f + (dEffectiveLines.size * 11.5f) + 4f
            dCanvas.drawRoundRect(RectF(margin, dBoxTop, margin + contentWidth, dBoxTop + dBoxH), 4f, 4f, dAddrBoxPaint)
            dCanvas.drawRoundRect(RectF(margin, dBoxTop, margin + contentWidth, dBoxTop + dBoxH), 4f, 4f, dAddrBorderPaint)

            dCanvas.drawText("ENDEREÇO RESIDENCIAL / LOGRADOURO DA AÇÃO FISCAL:", margin + 8f, dy + 8f, dAddrLabelPaint)
            var dTextY = dy + 19.5f
            for (line in dEffectiveLines) {
                dCanvas.drawText(line, margin + 8f, dTextY, dAddrTextPaint)
                dTextY += 11.5f
            }
            dy = dBoxTop + dBoxH + 8f

            // Coordenadas GPS (coluna 1) e Total de Animais (coluna 2)
            dCanvas.drawText("COORDENADAS GEOGRÁFICAS (GPS):", margin + 8f, dy, labelPaint)
            val dGpsPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 9f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }
            dCanvas.drawText(String.format(Locale.US, "%.6f°, %.6f° (SIRGAS 2000)", item.latitude, item.longitude), margin + 8f, dy + 13f, dGpsPaint)

            val cText = if (item.mistreatedHorseCount > 0) {
                "${formatAnimalCount(item.horseCount, item.isDog)} (${formatAnimalCount(item.mistreatedHorseCount, item.isDog)} com sinais de maus-tratos)"
            } else {
                "${formatAnimalCount(item.horseCount, item.isDog)} (Sem sinais aparentes)"
            }
            dCanvas.drawText(cText, margin + (contentWidth / 2f), dy + 13f, valPaint)
            dy += 30f

            if (item.horseDescription.isNotBlank() || item.imageNotes.isNotBlank()) {
                dy = drawSectionHeader(dCanvas, "DESCRIÇÃO DOS ANIMAIS & NOTAS DE CAMPO", margin, dy, contentWidth)
                val combinedNotes = listOf(item.horseDescription, item.imageNotes)
                    .asSequence()
                    .filter { it.isNotBlank() }
                    .joinToString(" • ")
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
            dCanvas.drawText("Relatório de Apoio • Página $pageNumber de ${inspections.size + 1}", margin, dFooterY + 14f, footerTextPaint)

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
                subject = "Levantamento Técnico - ${inspection.protocolNumber}",
                message = "Segue em anexo o Levantamento referente ao registro ${inspection.protocolNumber}."
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
                message = "Segue em anexo o Relatório Consolidado para Apoio em Fiscalização."
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
                message = "Segue em anexo o Relatório de Apoio (PDF)."
            )
        } else {
            shareMultiplePdfs(
                context = context,
                files = generatedFiles,
                subject = "Exportação de ${generatedFiles.size} Relatórios PDF - Apoio em Fiscalização",
                message = "Seguem em anexo os ${generatedFiles.size} relatórios em formato PDF (Relatório de Apoio)."
            )
        }
    }

    const val DISCLAIMER_TEXT = "Documento gerado por aplicativo independente para fins de apoio, sem vínculo oficial com o GDF."

    fun shareMultiplePdfs(context: Context, files: List<File>, subject: String, message: String) {
        try {
            Toast.makeText(context, DISCLAIMER_TEXT, Toast.LENGTH_LONG).show()

            val uris = ArrayList<android.net.Uri>()
            for (file in files) {
                uris.add(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file))
            }
            val fullMessage = "$message\n\n$DISCLAIMER_TEXT"
            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "application/pdf"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, fullMessage)
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
            Toast.makeText(context, DISCLAIMER_TEXT, Toast.LENGTH_LONG).show()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val fullMessage = "$message\n\n$DISCLAIMER_TEXT"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, fullMessage)
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
