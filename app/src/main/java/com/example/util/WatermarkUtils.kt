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
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WatermarkUtils {

    fun applyWatermarkAndSave(
        context: Context,
        inputUri: Uri,
        latitude: Double,
        longitude: Double,
        timestamp: Long,
        administrativeRegion: String,
        protocol: String,
        speciesTitle: String
    ): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(inputUri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return null

            val watermarked = applyWatermarkToBitmap(
                originalBitmap = originalBitmap,
                latitude = latitude,
                longitude = longitude,
                timestamp = timestamp,
                administrativeRegion = administrativeRegion,
                protocol = protocol,
                speciesTitle = speciesTitle
            )

            // Save in internal app cache/files
            val photosDir = File(context.filesDir, "photos").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "FISC_${protocol}_${System.currentTimeMillis()}.jpg"
            val outputFile = File(photosDir, fileName)
            FileOutputStream(outputFile).use { out ->
                watermarked.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }

            // Also save to device gallery (MediaStore) as requested by user
            saveToGallery(context, outputFile, "Fiscalização DF - $speciesTitle")

            outputFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun applyWatermarkToBitmap(
        originalBitmap: Bitmap,
        latitude: Double,
        longitude: Double,
        timestamp: Long,
        administrativeRegion: String,
        protocol: String,
        speciesTitle: String
    ): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val bannerHeight = (height * 0.16f).coerceIn(120f, 320f)
        val bannerTop = height - bannerHeight

        val bannerPaint = Paint().apply {
            color = Color.argb(205, 0, 45, 32) // Dark DF Legal Green
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, bannerTop, width.toFloat(), height.toFloat(), bannerPaint)

        // Accent top border
        val linePaint = Paint().apply {
            color = Color.rgb(255, 183, 3) // Gold accent line
            strokeWidth = (bannerHeight * 0.035f).coerceAtLeast(3f)
        }
        canvas.drawLine(0f, bannerTop, width.toFloat(), bannerTop, linePaint)

        val textSize = (bannerHeight * 0.18f).coerceAtLeast(18f)
        val titlePaint = Paint().apply {
            color = Color.WHITE
            this.textSize = textSize * 1.08f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(240, 248, 255)
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
        val dateStr = sdf.format(Date(timestamp))
        val latLngStr = String.format(Locale.US, "%.6f°, %.6f° (SIRGAS 2000)", latitude, longitude)

        val paddingX = width * 0.04f
        var currentY = bannerTop + (bannerHeight * 0.28f)

        canvas.drawText("GOVERNO DO DISTRITO FEDERAL • APOIO FISCALIZAÇÃO", paddingX, currentY, titlePaint)
        currentY += bannerHeight * 0.23f
        canvas.drawText("Espécie: $speciesTitle | Protocolo: $protocol", paddingX, currentY, textPaint)
        currentY += bannerHeight * 0.22f
        canvas.drawText("RA: $administrativeRegion • GPS: $latLngStr", paddingX, currentY, textPaint)
        currentY += bannerHeight * 0.20f
        canvas.drawText("Data/Hora: $dateStr • Evidência Georreferenciada", paddingX, currentY, textPaint)

        return mutableBitmap
    }

    fun saveToGallery(context: Context, imageFile: File, title: String): Uri? {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.TITLE, title)
                put(MediaStore.Images.Media.DESCRIPTION, "Registro Georreferenciado - Fiscalização DF")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FiscalizacaoDF")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val itemUri = context.contentResolver.insert(collection, values)
            if (itemUri != null) {
                context.contentResolver.openOutputStream(itemUri)?.use { out ->
                    imageFile.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(itemUri, values, null, null)
                }
            }
            itemUri
        } catch (_: Exception) {
            null
        }
    }
}
