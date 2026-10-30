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
        val outputFile = File(photosDir, "FOTO_${protocol}_Foto${exemplarIndex}${tagSuffix}_${System.currentTimeMillis()}_$uniqueSuffix.jpeg")

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
        val outputFile = File(photosDir, "FOTO_${protocol}_Foto${exemplarIndex}${tagSuffix}_${System.currentTimeMillis()}_$uniqueSuffix.jpeg")

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
        val bodyTextSize = (if (isLandscape) 16f else 18f) * scaleFactor
        val lineHeight = bodyTextSize * 1.4f

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
        val dateString = sdf.format(Date(captureTimeMillis))
        val coordsString = String.format(Locale.US, "LAT: %.6f° | LON: %.6f°", latitude, longitude)

        // 1. A tag digitada pelo fiscal
        val tagLine = "TAG: " + if (!photoTag.isNullOrBlank()) photoTag.trim().take(30) else "Não informada"
        // 2. Coordenadas geográficas
        val coordsLine = "COORDENADAS: $coordsString"
        // 3. Data e hora
        val dateLine = "DATA/HORA: $dateString"
        // 4. Região administrativa
        val raLine = "REGIÃO ADMINISTRATIVA: $administrativeRegion"

        val bgPaint = Paint().apply {
            color = Color.argb(215, 15, 23, 42) // Slate 900 translúcido
            style = Paint.Style.FILL
        }

        val stripePaint = Paint().apply {
            color = Color.rgb(245, 158, 11) // Amber 500
            strokeWidth = 4f * scaleFactor
            style = Paint.Style.FILL
        }

        val tagPaint = Paint().apply {
            color = Color.rgb(56, 189, 248) // Sky 400
            textSize = bodyTextSize * 1.05f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            setShadowLayer(3f, 1f, 1f, Color.BLACK)
        }

        val coordsPaint = Paint().apply {
            color = Color.WHITE
            textSize = bodyTextSize
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            setShadowLayer(3f, 1f, 1f, Color.BLACK)
        }

        val datePaint = Paint().apply {
            color = Color.rgb(254, 240, 138) // Light gold
            textSize = bodyTextSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
            setShadowLayer(3f, 1f, 1f, Color.BLACK)
        }

        val raPaint = Paint().apply {
            color = Color.rgb(203, 213, 225) // Slate 300
            textSize = bodyTextSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            setShadowLayer(3f, 1f, 1f, Color.BLACK)
        }

        if (isLandscape) {
            // MODO PAISAGEM: Banner horizontal compacto com 2 colunas contendo exclusivamente as 4 informações
            val bannerHeight = (2 * lineHeight) + (bannerPadding * 2f)
            val bannerTop = height - bannerHeight

            canvas.drawRect(0f, bannerTop, width.toFloat(), height.toFloat(), bgPaint)
            canvas.drawRect(0f, bannerTop, width.toFloat(), bannerTop + (3.5f * scaleFactor), stripePaint)

            val col2X = (width * 0.50f).coerceAtLeast(bannerPadding + 400f * scaleFactor)
            val row1Y = bannerTop + bannerPadding + bodyTextSize
            val row2Y = row1Y + lineHeight

            // Coluna 1: 1. Tag digitada pelo fiscal | 2. Coordenadas geográficas
            canvas.drawText(tagLine, bannerPadding, row1Y, tagPaint)
            canvas.drawText(coordsLine, bannerPadding, row2Y, coordsPaint)

            // Coluna 2: 3. Data e hora | 4. Região administrativa
            canvas.drawText(dateLine, col2X, row1Y, datePaint)
            canvas.drawText(raLine, col2X, row2Y, raPaint)
        } else {
            // MODO RETRATO: Formato empilhado contendo exclusivamente as 4 informações
            val bannerHeight = (4 * lineHeight) + (bannerPadding * 2f)
            val bannerTop = height - bannerHeight

            canvas.drawRect(0f, bannerTop, width.toFloat(), height.toFloat(), bgPaint)
            canvas.drawRect(0f, bannerTop, width.toFloat(), bannerTop + (4f * scaleFactor), stripePaint)

            var currentY = bannerTop + bannerPadding + bodyTextSize
            // 1. Tag digitada pelo fiscal
            canvas.drawText(tagLine, bannerPadding, currentY, tagPaint)
            currentY += lineHeight
            // 2. Coordenadas geográficas
            canvas.drawText(coordsLine, bannerPadding, currentY, coordsPaint)
            currentY += lineHeight
            // 3. Data e hora
            canvas.drawText(dateLine, bannerPadding, currentY, datePaint)
            currentY += lineHeight
            // 4. Região administrativa
            canvas.drawText(raLine, bannerPadding, currentY, raPaint)
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

    /**
     * Extrai a TAG inserida pelo fiscal a partir do nome do arquivo salvo da foto georreferenciada.
     */
    fun extractTagFromPhotoPath(path: String): String? {
        if (path.isBlank()) return null
        val filename = File(path).name
        val match = Regex("_TAG_([^_]+)_").find(filename)
        return match?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
    }
}
