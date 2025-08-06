package com.example.weather.location
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.*

class LocationService(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationReceived: (Location?) -> Unit) {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null && location.accuracy <= 100f) {
                Log.d("LocationDebug", "Fresh Location: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}")
                onLocationReceived(location)
            } else {
                // fallback to last known location
                fusedLocationClient.lastLocation.addOnSuccessListener { lastKnown ->
                    if (lastKnown != null) {
                        Log.d("LocationDebug", "Fallback Location: ${lastKnown.latitude}, ${lastKnown.longitude}, accuracy: ${lastKnown.accuracy}")
                    } else {
                        Log.d("LocationDebug", "No location available")
                    }
                    onLocationReceived(lastKnown)
                }.addOnFailureListener {
                    Log.e("LocationDebug", "Fallback failed", it)
                    onLocationReceived(null)
                }
            }
        }.addOnFailureListener {
            Log.e("LocationDebug", "getCurrentLocation failed", it)
            onLocationReceived(null)
        }
    }

    // ✅ NEW FUNCTION: Convert city name to lat/lon using Geocoder
    @Suppress("DEPRECATION")
    fun getCoordinatesFromCity(cityName: String, callback: (lat: Double, lon: Double) -> Unit) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13 (API 33) and above, use the non-deprecated method
                geocoder.getFromLocationName(cityName, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<android.location.Address>) {
                        if (addresses.isNotEmpty()) {
                            val location = addresses[0]
                            callback(location.latitude, location.longitude)
                        } else {
                            Log.w("LocationDebug", "No geocoding results for city: $cityName")
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        Log.e("LocationDebug", "Geocoding failed: $errorMessage")
                    }
                })
            } else {
                // For older Android versions, use the deprecated method (but suppress the warning)
                val addresses = geocoder.getFromLocationName(cityName, 1)
                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    callback(location.latitude, location.longitude)
                } else {
                    Log.w("LocationDebug", "No geocoding results for city: $cityName")
                }
            }
        } catch (e: Exception) {
            Log.e("LocationDebug", "Geocoding failed", e)
        }
    }
}