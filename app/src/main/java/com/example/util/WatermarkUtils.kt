package com.example.util

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
import android.net.Uri
import android.media.ExifInterface
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
        exemplarIndex: Int = 1
    ): File? {
        val originalBitmap = decodeSampledBitmapFromUri(context, inputUri, 1920, 1920) ?: return null
        val orientedBitmap = fixBitmapOrientation(context, inputUri, originalBitmap)

        val watermarkedBitmap = addWatermarkToBitmap(
            source = orientedBitmap,
            latitude = latitude,
            longitude = longitude,
            administrativeRegion = administrativeRegion,
            protocol = protocol,
            captureTimeMillis = captureTimeMillis,
            exemplarIndex = exemplarIndex
        )

        // Save to app internal storage in 'photos' dir
        val photosDir = File(context.filesDir, "photos").apply {
            if (!exists()) mkdirs()
        }
        val uniqueSuffix = java.util.UUID.randomUUID().toString().take(6)
        val outputFile = File(photosDir, "FOTO_${protocol}_Ex${exemplarIndex}_${System.currentTimeMillis()}_$uniqueSuffix.jpg")

        return try {
            FileOutputStream(outputFile).use { out ->
                watermarkedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
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
        exemplarIndex: Int = 1
    ): File? {
        val watermarkedBitmap = addWatermarkToBitmap(
            source = source,
            latitude = latitude,
            longitude = longitude,
            administrativeRegion = administrativeRegion,
            protocol = protocol,
            captureTimeMillis = captureTimeMillis,
            exemplarIndex = exemplarIndex
        )

        val photosDir = File(context.filesDir, "photos").apply {
            if (!exists()) mkdirs()
        }
        val uniqueSuffix = java.util.UUID.randomUUID().toString().take(6)
        val outputFile = File(photosDir, "FOTO_${protocol}_Ex${exemplarIndex}_${System.currentTimeMillis()}_$uniqueSuffix.jpg")

        return try {
            FileOutputStream(outputFile).use { out ->
                watermarkedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            outputFile
        } catch (_: Exception) {
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
        exemplarIndex: Int = 1
    ): Bitmap {
        val width = source.width
        val height = source.height

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Draw original photo
        canvas.drawBitmap(source, 0f, 0f, null)

        val baseDimension = max(width, height)
        val scaleFactor = (baseDimension / 1000f).coerceIn(1f, 3.5f)

        val bannerPadding = (16f * scaleFactor)
        val titleTextSize = 22f * scaleFactor
        val bodyTextSize = 18f * scaleFactor
        val smallTextSize = 14f * scaleFactor
        val lineHeight = bodyTextSize * 1.4f

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
        val dateString = sdf.format(Date(captureTimeMillis))
        val coordsString = String.format(Locale.US, "LAT: %.6f° | LON: %.6f° (GPS)", latitude, longitude)
        val raString = "RA: $administrativeRegion"
        val protocolString = "PROTOCOLO: $protocol | APOIO À FISCALIZAÇÃO"

        val lines = listOf(
            "🐴 ANIMAL FOTOGRAFADO: Ex $exemplarIndex (Exemplar nº $exemplarIndex)",
            "📍 COORDENADAS: $coordsString",
            "📅 DATA/HORA: $dateString",
            "🏛 REGIÃO: $raString"
        )

        val bannerHeight = (lines.size * lineHeight) + titleTextSize + (bannerPadding * 2.4f)
        val bannerTop = height - bannerHeight

        // Draw translucent dark background bar
        val bgPaint = Paint().apply {
            color = Color.argb(210, 15, 23, 42) // Slate 900 translucent
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, bannerTop, width.toFloat(), height.toFloat(), bgPaint)

        // Draw accent colored top stripe on banner (Safety Amber Gold)
        val stripePaint = Paint().apply {
            color = Color.rgb(245, 158, 11) // Amber 500
            strokeWidth = 4f * scaleFactor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, bannerTop, width.toFloat(), bannerTop + (4f * scaleFactor), stripePaint)

        // Draw Title / Badge
        val titlePaint = Paint().apply {
            color = Color.rgb(254, 240, 138) // Light gold
            textSize = titleTextSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            setShadowLayer(4f, 1f, 1f, Color.BLACK)
        }

        var currentY = bannerTop + bannerPadding + titleTextSize
        canvas.drawText("★ LEVANTAMENTO DE CAMPO | APOIO À FISCALIZAÇÃO", bannerPadding, currentY, titlePaint)

        // Draw protocol on the right side if space allows
        val protoPaint = Paint().apply {
            color = Color.rgb(148, 163, 184) // Slate 400
            textSize = smallTextSize
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }
        val protoBounds = Rect()
        protoPaint.getTextBounds(protocol, 0, protocol.length, protoBounds)
        val protoX = width - bannerPadding - protoBounds.width()
        if (protoX > bannerPadding + 300 * scaleFactor) {
            canvas.drawText("REF: $protocol", protoX, currentY, protoPaint)
        }

        // Draw information lines
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

        currentY += lineHeight * 0.4f
        for (i in lines.indices) {
            currentY += lineHeight
            val text = lines[i]
            if (i == 0) {
                // Coordinates line with sky blue highlight
                canvas.drawText(text, bannerPadding, currentY, highlightPaint)
            } else if (i == lines.size - 1) {
                val subPaint = Paint().apply {
                    color = Color.rgb(203, 213, 225) // Slate 300
                    textSize = smallTextSize
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    isAntiAlias = true
                }
                canvas.drawText(text, bannerPadding, currentY, subPaint)
            } else {
                canvas.drawText(text, bannerPadding, currentY, bodyPaint)
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
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            context.contentResolver.openInputStream(uri)?.use {
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

    private fun fixBitmapOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val exif = inputStream?.use { ExifInterface(it) } ?: return bitmap
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (_: Exception) {
            bitmap
        }
    }
}
