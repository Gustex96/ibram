package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.example.data.model.DfConstants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class GeoCoordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val altitude: Double = 0.0,
    val speed: Float = 0f,
    val isRealGps: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedCoordinates: String
        get() = String.format(java.util.Locale.US, "%.6f°, %.6f°", latitude, longitude)

    val formattedShort: String
        get() = String.format(java.util.Locale.US, "%.6f, %.6f", latitude, longitude)

    val accuracyDisplay: String
        get() = if (accuracy > 0f) String.format(java.util.Locale.US, "±%.1fm", accuracy) else "GPS"
}

class LocationHelper(private val context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Fluxo contínuo e dinâmico de coordenadas (princípio do Google Maps / Timestamp Camera).
     * Atualiza em tempo real as coordenadas conforme o fiscal se movimenta em campo.
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdatesFlow(hasPermission: Boolean): Flow<GeoCoordinates> = callbackFlow {
        if (!hasPermission) {
            trySend(
                GeoCoordinates(
                    latitude = DfConstants.DEFAULT_DF_LATITUDE,
                    longitude = DfConstants.DEFAULT_DF_LONGITUDE,
                    accuracy = 10f,
                    isRealGps = false
                )
            )
            awaitClose {}
            return@callbackFlow
        }

        // Envia imediatamente a última localização conhecida como ponto de partida
        try {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    trySend(
                        GeoCoordinates(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            accuracy = loc.accuracy,
                            altitude = loc.altitude,
                            speed = loc.speed,
                            isRealGps = true,
                            timestamp = loc.time.takeIf { it > 0 } ?: System.currentTimeMillis()
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // Atualização contínua de alta precisão (1 segundo ou menos, 0m de deslocamento)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .setMinUpdateDistanceMeters(0f)
            .setWaitForAccurateLocation(false)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val coords = GeoCoordinates(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    accuracy = loc.accuracy,
                    altitude = loc.altitude,
                    speed = loc.speed,
                    isRealGps = true,
                    timestamp = loc.time.takeIf { it > 0 } ?: System.currentTimeMillis()
                )
                trySend(coords)
            }
        }

        try {
            fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: Exception) {
            Log.e("LocationHelper", "Erro ao iniciar stream dinâmico de localização: ${e.message}", e)
        }

        awaitClose {
            try {
                fusedClient.removeLocationUpdates(locationCallback)
            } catch (_: Exception) {}
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCoordinates(hasPermission: Boolean): GeoCoordinates {
        if (!hasPermission) {
            return GeoCoordinates(
                latitude = DfConstants.DEFAULT_DF_LATITUDE,
                longitude = DfConstants.DEFAULT_DF_LONGITUDE,
                accuracy = 10f,
                isRealGps = false
            )
        }

        return try {
            val cts = CancellationTokenSource()
            val location: Location? = suspendCancellableCoroutine { continuation ->
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        continuation.resume(loc)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            }

            if (location != null) {
                GeoCoordinates(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    altitude = location.altitude,
                    speed = location.speed,
                    isRealGps = true
                )
            } else {
                val lastLoc: Location? = suspendCancellableCoroutine { continuation ->
                    fusedClient.lastLocation
                        .addOnSuccessListener { loc -> continuation.resume(loc) }
                        .addOnFailureListener { continuation.resume(null) }
                }

                if (lastLoc != null) {
                    GeoCoordinates(
                        latitude = lastLoc.latitude,
                        longitude = lastLoc.longitude,
                        accuracy = lastLoc.accuracy,
                        altitude = lastLoc.altitude,
                        speed = lastLoc.speed,
                        isRealGps = true
                    )
                } else {
                    GeoCoordinates(
                        latitude = DfConstants.DEFAULT_DF_LATITUDE,
                        longitude = DfConstants.DEFAULT_DF_LONGITUDE,
                        accuracy = 15f,
                        isRealGps = false
                    )
                }
            }
        } catch (_: Exception) {
            GeoCoordinates(
                latitude = DfConstants.DEFAULT_DF_LATITUDE,
                longitude = DfConstants.DEFAULT_DF_LONGITUDE,
                accuracy = 20f,
                isRealGps = false
            )
        }
    }
}
