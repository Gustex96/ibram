package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object WatermarkUtils {

    fun applyWatermarkAndSave(
        context: Context,
        inputUri: Uri,
        latitude: Double,
        longitude: Double,
        administrativeRegion: String,
        protocol: String,
        captureTimeMillis: Long = System.currentTimeMillis(),
        exemplarIndex: Int = 1,
        photoTag: String? = null,
        deviceOrientationDegrees: Int = 0
    ): File? {
        val originalBitmap = decodeSampledBitmapFromUri(context, inputUri, 1920, 1920) ?: return null
        val orientedBitmap = fixBitmapOrientation(context, inputUri, originalBitmap, deviceOrientationDegrees)

        val watermarkedBitmap = addWatermarkToBitmap(
            source = orientedBitmap,
            latitude = latitude,
            longitude = longitude,
            administrativeRegion = administrativeRegion,
            protocol = protocol,
            captureTimeMillis = captureTimeMillis,
            exemplarIndex = exemplarIndex,
            photoTag = photoTag
        )

        // Save to app internal storage in 'photos' dir
        val photosDir = File(context.filesDir, "photos").apply {
            if (!exists()) mkdirs()
        }
        val uniqueSuffix = java.util.UUID.randomUUID().toString().take(6)
        val tagSuffix = if (!photoTag.isNullOrBlank()) "_TAG_${photoTag.trim().take(20).replace(Regex("[^a-zA-Z0-9_-]"), "_")}" else ""
        val outputFile = File(photosDir, "FOTO_${protocol}_Foto${exemplarIndex}${tagSuffix}_${System.currentTimeMillis()}_$uniqueSuffix.jpg")

        return try {
            FileOutputStream(outputFile).use { out ->
                watermarkedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            // Salva na galeria de fotos do celular
            saveToGallery(
                context = context,
                imageFile = outputFile,
                title = "Fiscalização $protocol - Foto $exemplarIndex",
                description = "Levantamento $protocol • RA: $administrativeRegion • Data: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(captureTimeMillis))}"
            )
            outputFile
        } catch (_: Exception) {
            null
        }
    }

    fun applyWatermarkToBitmap(
        context: Context,
        source: Bitmap,
        latitude: Double,
        longitude: Double,
        administrativeRegion: String,
        protocol: String,
        captureTimeMillis: Long = System.currentTimeMillis(),
        exemplarIndex: Int = 1,
        photoTag: String? = null
    ): File? {
        val watermarkedBitmap = addWatermarkToBitmap(
            source = source,
            latitude = latitude,
            longitude = longitude,
            administrativeRegion = administrativeRegion,
            protocol = protocol,
            captureTimeMillis = captureTimeMillis,
            exemplarIndex = exemplarIndex,
            photoTag = photoTag
        )

        val photosDir = File(context.filesDir, "photos").apply {
            if (!exists()) mkdirs()
        }
        val uniqueSuffix = java.util.UUID.randomUUID().toString().take(6)
        val tagSuffix = if (!photoTag.isNullOrBlank()) "_TAG_${photoTag.trim().take(20).replace(Regex("[^a-zA-Z0-9_-]"), "_")}" else ""
        val outputFile = File(photosDir, "FOTO_${protocol}_Foto${exemplarIndex}${tagSuffix}_${System.currentTimeMillis()}_$uniqueSuffix.jpg")

        return try {
            FileOutputStream(outputFile).use { out ->
                watermarkedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            // Salva na galeria de fotos do celular
            saveToGallery(
                context = context,
                imageFile = outputFile,
                title = "Fiscalização $protocol - Foto $exemplarIndex",
                description = "Levantamento $protocol • RA: $administrativeRegion • Data: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(captureTimeMillis))}"
            )
            outputFile
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Salva uma cópia da fotografia tirada diretamente na Galeria de Fotos do celular (MediaStore).
     * Cria o álbum "FiscalizacaoDF" para fácil identificação pelo usuário e órgãos de fiscalização.
     */
    fun saveToGallery(
        context: Context,
        imageFile: File,
        title: String,
        description: String? = null
    ): Uri? {
        if (!imageFile.exists() || imageFile.length() == 0L) return null
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.TITLE, title)
                if (!description.isNullOrBlank()) {
                    put(MediaStore.Images.Media.DESCRIPTION, description)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "FiscalizacaoDF")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                resolver.openOutputStream(imageUri)?.use { outStream ->
                    imageFile.inputStream().use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
                try {
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(imageFile.absolutePath),
                        arrayOf("image/jpeg"),
                        null
                    )
                } catch (_: Exception) {}

                Log.d("WatermarkUtils", "Foto salva na galeria pública: $imageUri")
                imageUri
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("WatermarkUtils", "Erro ao salvar foto na galeria: ${e.message}", e)
            null
        }
    }

    private fun addWatermarkToBitmap(
        source: Bitmap,
        latitude: Double,
        longitude: Double,
        administrativeRegion: String,
        protocol: String,
        captureTimeMillis: Long,
        exemplarIndex: Int = 1,
        photoTag: String? = null
    ): Bitmap {
        val width = source.width
        val height = source.height
        val isLandscape = width > height

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Draw original photo
        canvas.drawBitmap(source, 0f, 0f, null)

        val baseDimension = max(width, height)
        val scaleFactor = (baseDimension / 1000f).coerceIn(1f, 3.5f)

        val bannerPadding = 16f * scaleFactor
        val titleTextSize = (if (isLandscape) 19f else 22f) * scaleFactor
        val bodyTextSize = (if (isLandscape) 16f else 18f) * scaleFactor
        val smallTextSize = (if (isLandscape) 13f else 14f) * scaleFactor
        val lineHeight = bodyTextSize * 1.35f

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
        val dateString = sdf.format(Date(captureTimeMillis))
        val coordsString = String.format(Locale.US, "LAT: %.6f° | LON: %.6f° (GPS)", latitude, longitude)
        val raString = "RA: $administrativeRegion"

        val tagLine = if (!photoTag.isNullOrBlank()) {
            "🏷️ TAG: ${photoTag.trim().take(20)}"
        } else {
            "🐴 REGISTRO FOTOGRÁFICO: Foto #$exemplarIndex"
        }

        val bgPaint = Paint().apply {
            color = Color.argb(215, 15, 23, 42) // Slate 900 translucent
            style = Paint.Style.FILL
        }

        val stripePaint = Paint().apply {
            color = Color.rgb(245, 158, 11) // Amber 500
            strokeWidth = 4f * scaleFactor
            style = Paint.Style.FILL
        }

        val titlePaint = Paint().apply {
            color = Color.rgb(254, 240, 138) // Light gold
            textSize = titleTextSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            setShadowLayer(4f, 1f, 1f, Color.BLACK)
        }

        val protoText = "REF: $protocol"
        val protoPaint = Paint().apply {
            color = Color.rgb(148, 163, 184) // Slate 400
            textSize = smallTextSize * 0.9f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.WHITE
            textSize = bodyTextSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
            setShadowLayer(3f, 1f, 1f, Color.BLACK)
        }

        val highlightPaint = Paint().apply {
            color = Color.rgb(56, 189, 248) // Sky 400
            textSize = bodyTextSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            setShadowLayer(3f, 1f, 1f, Color.BLACK)
        }

        val subPaint = Paint().apply {
            color = Color.rgb(203, 213, 225) // Slate 300
            textSize = bodyTextSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
            setShadowLayer(3f, 1f, 1f, Color.BLACK)
        }

        if (isLandscape) {
            // MODO PAISAGEM: Banner horizontal compacto com 2 colunas para manter proporção perfeita
            val bannerHeight = (2 * lineHeight) + titleTextSize + (bannerPadding * 2.2f)
            val bannerTop = height - bannerHeight

            // Fundo escuro translúcido
            canvas.drawRect(0f, bannerTop, width.toFloat(), height.toFloat(), bgPaint)
            // Faixa dourada no topo do banner
            canvas.drawRect(0f, bannerTop, width.toFloat(), bannerTop + (3.5f * scaleFactor), stripePaint)

            val titleY = bannerTop + bannerPadding + titleTextSize
            canvas.drawText("★ LEVANTAMENTO DE CAMPO | APOIO À FISCALIZAÇÃO", bannerPadding, titleY, titlePaint)

            val rightMargin = (bannerPadding * 2f).coerceAtLeast(32f * scaleFactor)
            val protoWidth = protoPaint.measureText(protoText)
            val protoX = width - rightMargin - protoWidth
            canvas.drawText(protoText, protoX.coerceAtLeast(width * 0.65f), titleY, protoPaint)

            val col2X = (width * 0.50f).coerceAtLeast(bannerPadding + 420f * scaleFactor)
            val row1Y = titleY + lineHeight + (4f * scaleFactor)
            val row2Y = row1Y + lineHeight

            // Coluna 1: Tag/Identificação + Coordenadas GPS
            canvas.drawText(tagLine, bannerPadding, row1Y, highlightPaint)
            canvas.drawText("📍 COORDENADAS: $coordsString", bannerPadding, row2Y, bodyPaint)

            // Coluna 2: Data/Hora + Região Administrativa
            canvas.drawText("📅 DATA/HORA: $dateString", col2X, row1Y, subPaint)
            canvas.drawText("🏛 REGIÃO: $raString", col2X, row2Y, subPaint)
        } else {
            // MODO RETRATO: Formato empilhado tradicional vertical
            val lines = listOf(
                tagLine,
                "📍 COORDENADAS: $coordsString",
                "📅 DATA/HORA: $dateString",
                "🏛 REGIÃO: $raString"
            )

            val bannerHeight = (lines.size * lineHeight) + titleTextSize + (bannerPadding * 2.4f)
            val bannerTop = height - bannerHeight

            canvas.drawRect(0f, bannerTop, width.toFloat(), height.toFloat(), bgPaint)
            canvas.drawRect(0f, bannerTop, width.toFloat(), bannerTop + (4f * scaleFactor), stripePaint)

            var currentY = bannerTop + bannerPadding + titleTextSize
            canvas.drawText("★ LEVANTAMENTO DE CAMPO | APOIO À FISCALIZAÇÃO", bannerPadding, currentY, titlePaint)

            val titleWidth = titlePaint.measureText("★ LEVANTAMENTO DE CAMPO | APOIO À FISCALIZAÇÃO")
            val rightMargin = (bannerPadding * 2.2f).coerceAtLeast(32f * scaleFactor)
            val maxProtoWidth = (width - rightMargin - bannerPadding - titleWidth - (16f * scaleFactor)).coerceAtLeast(width * 0.35f)
            while (protoPaint.measureText(protoText) > maxProtoWidth && protoPaint.textSize > 6.5f * scaleFactor) {
                protoPaint.textSize -= 0.5f
            }
            val protoWidth = protoPaint.measureText(protoText)
            val protoX = width - rightMargin - protoWidth
            if (protoX > bannerPadding + titleWidth + (12f * scaleFactor)) {
                canvas.drawText(protoText, protoX, currentY, protoPaint)
            } else {
                val safeX = (width - rightMargin - protoWidth).coerceAtLeast(bannerPadding)
                canvas.drawText(protoText, safeX, currentY - (titleTextSize * 0.3f), protoPaint)
            }

            currentY += lineHeight * 0.4f
            for (i in lines.indices) {
                currentY += lineHeight
                val text = lines[i]
                if (i == 0) {
                    canvas.drawText(text, bannerPadding, currentY, highlightPaint)
                } else if (i == lines.size - 1) {
                    canvas.drawText(text, bannerPadding, currentY, subPaint)
                } else {
                    canvas.drawText(text, bannerPadding, currentY, bodyPaint)
                }
            }
        }

        return result
    }

    private fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            getInputStreamFromUri(context, uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            getInputStreamFromUri(context, uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun getInputStreamFromUri(context: Context, uri: Uri): InputStream? {
        return try {
            if (uri.scheme == "file") {
                File(uri.path ?: "").inputStream()
            } else {
                context.contentResolver.openInputStream(uri)
            }
        } catch (_: Exception) {
            try {
                context.contentResolver.openInputStream(uri)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun fixBitmapOrientation(
        context: Context,
        uri: Uri,
        bitmap: Bitmap,
        deviceOrientationDegrees: Int = 0
    ): Bitmap {
        return try {
            val inputStream: InputStream? = getInputStreamFromUri(context, uri)
            val exif = inputStream?.use { ExifInterface(it) }
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            ) ?: ExifInterface.ORIENTATION_UNDEFINED

            val matrix = Matrix()
            var rotated = false
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    matrix.postRotate(90f)
                    rotated = true
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    matrix.postRotate(180f)
                    rotated = true
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    matrix.postRotate(270f)
                    rotated = true
                }
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                    matrix.postScale(-1f, 1f)
                    rotated = true
                }
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                    matrix.postScale(1f, -1f)
                    rotated = true
                }
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postRotate(90f)
                    matrix.postScale(-1f, 1f)
                    rotated = true
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(270f)
                    matrix.postScale(-1f, 1f)
                    rotated = true
                }
            }

            if (rotated) {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else if (deviceOrientationDegrees == 90 || deviceOrientationDegrees == 270) {
                // Se o EXIF não girou mas o fiscal segurou a câmera no modo paisagem:
                if (bitmap.width < bitmap.height) {
                    val rotMatrix = Matrix()
                    rotMatrix.postRotate(if (deviceOrientationDegrees == 270) 270f else 90f)
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotMatrix, true)
                } else {
                    bitmap
                }
            } else {
                bitmap
            }
        } catch (_: Exception) {
            bitmap
        }
    }
}
