package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun MapPointThumbnail(
    latitude: Double,
    longitude: Double,
    administrativeRegion: String,
    protocolNumber: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable {
                openInExternalMap(context, latitude, longitude, protocolNumber)
            }
            .testTag("map_thumbnail_$protocolNumber")
    ) {
        // Stylized Map Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Base Map Ground
            drawRect(color = Color(0xFFF1F3F4))

            // Green park/rural area polygon
            val parkPath = Path().apply {
                moveTo(0f, height * 0.15f)
                cubicTo(
                    width * 0.3f, height * 0.05f,
                    width * 0.45f, height * 0.35f,
                    width * 0.4f, height * 0.6f
                )
                lineTo(0f, height * 0.65f)
                close()
            }
            drawPath(path = parkPath, color = Color(0xFFD7ECD9))

            // Water stream / lake patch
            val waterPath = Path().apply {
                moveTo(width * 0.7f, height)
                cubicTo(
                    width * 0.65f, height * 0.7f,
                    width * 0.85f, height * 0.5f,
                    width, height * 0.45f
                )
                lineTo(width, height)
                close()
            }
            drawPath(path = waterPath, color = Color(0xFFD2E3FC))

            // Secondary Street grid lines
            val roadPaintColor = Color.White
            val gridSpacing = 28f
            for (x in 0..(width.toInt()) step gridSpacing.toInt()) {
                drawLine(
                    color = roadPaintColor,
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat() + 20f, height),
                    strokeWidth = 3f
                )
            }
            for (y in 0..(height.toInt()) step gridSpacing.toInt()) {
                drawLine(
                    color = roadPaintColor,
                    start = Offset(0f, y.toFloat()),
                    end = Offset(width, y.toFloat() - 15f),
                    strokeWidth = 3f
                )
            }

            // Main Highway / Arterial Road intersecting near center
            drawLine(
                color = Color(0xFFFFD54F),
                start = Offset(0f, height * 0.55f),
                end = Offset(width, height * 0.45f),
                strokeWidth = 7f
            )
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(width * 0.45f, 0f),
                end = Offset(width * 0.55f, height),
                strokeWidth = 6f
            )

            // Radar pulse circles around center GPS fix
            val centerX = width / 2f
            val centerY = height / 2f

            drawCircle(
                color = Color(0xFF00684A).copy(alpha = 0.12f),
                radius = 36.dp.toPx(),
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0xFF00684A).copy(alpha = 0.22f),
                radius = 20.dp.toPx(),
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = Color(0xFF00684A).copy(alpha = 0.35f),
                radius = 6.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        // Center Pin
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ponto no mapa",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Top Overlay Header: "Ponto no Mapa"
        Surface(
            color = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(bottomEnd = 8.dp),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Ponto no Mapa",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top Right: External Map hint
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Abrir mapa",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(4.dp)
                    .size(12.dp)
            )
        }

        // Bottom Overlay: Coordinates Chip
        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(Locale.US, "%.4f°, %.4f°", latitude, longitude),
                    color = Color.White,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Abrir GPS",
                    color = Color(0xFF81C784),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun openInExternalMap(
    context: Context,
    latitude: Double,
    longitude: Double,
    protocolNumber: String
) {
    try {
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode("Fiscalização $protocolNumber")})")
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        try {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Não foi possível abrir aplicativo de mapas.", Toast.LENGTH_SHORT).show()
        }
    }
}
