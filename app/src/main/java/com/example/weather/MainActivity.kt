package com.example.weather

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weather.location.LocationService
import com.example.weather.service.NotificationService
import com.example.weather.ui.WeatherScreen
import com.example.weather.ui.theme.WeatherTheme
import com.example.weather.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    private lateinit var locationService: LocationService
    private var locationPermissionGranted = mutableStateOf(false)
    private var notificationPermissionGranted = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationService = LocationService(this)

        // Request location permission
        val locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            locationPermissionGranted.value = granted
        }

        // Request notification permission for Android 13+
        val notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            notificationPermissionGranted.value = granted
        }

        // Launch location permission request
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        // Launch notification permission request for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // For devices below Android 13, notification permission is granted by default
            notificationPermissionGranted.value = true
        }

        setContent {
            val viewModel: WeatherViewModel = viewModel()

            // Create notification channel on app start
            LaunchedEffect(Unit) {
                NotificationService.createNotificationChannel(this@MainActivity)
            }

            // Load weather data when location permission is granted
            LaunchedEffect(locationPermissionGranted.value) {
                if (locationPermissionGranted.value) {
                    locationService.getCurrentLocation { location ->
                        location?.let {
                            viewModel.loadWeather(
                                it.latitude,
                                it.longitude,
                                "0163914bfedc415e17c2470bdae1de4f"
                            )
                            viewModel.loadForecastByCoordinates(
                                it.latitude,
                                it.longitude,
                                "0163914bfedc415e17c2470bdae1de4f"
                            )
                            viewModel.loadAirQuality(
                                it.latitude,
                                it.longitude,
                                "0163914bfedc415e17c2470bdae1de4f"
                            )
                        }
                    }
                }
            }

            WeatherTheme {
                WeatherScreen(viewModel)
            }
        }
    }
}