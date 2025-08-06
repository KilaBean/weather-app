package com.example.weather.ui

import android.annotation.SuppressLint
import android.location.Location
import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner // <-- UPDATED IMPORT
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.weather.R
import com.example.weather.location.LocationService
import com.example.weather.ui.components.*
import com.example.weather.ui.model.WeatherMetric
import com.example.weather.viewmodel.WeatherViewModel
import com.example.weather.service.NotificationService
import com.example.weather.service.WeatherNotificationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Add these extension functions at the top of the file (outside any class)
fun Double.celsiusToFahrenheit(): Double = (this * 9/5) + 32
fun Double.fahrenheitToCelsius(): Double = (this - 32) * 5/9
@OptIn(ExperimentalMaterial3Api::class) // <-- NEW ANNOTATION
@SuppressLint("MissingPermission")
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weather by viewModel.weather.collectAsState()
    val forecast by viewModel.forecast.collectAsState()
    val airQuality by viewModel.airQuality.collectAsState()
    val isForecastLoading by viewModel.isForecastLoading.collectAsState()
    val useNotifications by viewModel.useNotifications.collectAsState()
    val useFahrenheit by viewModel.useFahrenheit.collectAsState()
    var cityInput by remember { mutableStateOf(TextFieldValue("")) }
    var showSettings by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isLocationLoading by remember { mutableStateOf(false) } // Track location loading state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Pull-to-refresh state
    val pullToRefreshState = rememberPullToRefreshState()

    // Flag to prevent automatic GPS location requests during initial composition
    var hasInitialized by remember { mutableStateOf(false) }
    var hasShownWelcomeMessage by remember { mutableStateOf(false) }

    // Create notification channel on first launch
    LaunchedEffect(Unit) {
        NotificationService.createNotificationChannel(context)
        // Set initialization flag after a short delay to ensure initial composition is complete
        delay(100)
        hasInitialized = true
    }

    // Get weather condition for animations and sounds
    val weatherCondition = weather?.weather?.firstOrNull()?.main?.lowercase() ?: ""

    // Get weather condition colors based on the specific weather group
    val (startColor, endColor) = when (weatherCondition) {
        "clear" -> Pair(Color(0xFFFFC107), Color(0xFFFF9800)) // Sunny yellow
        "thunderstorm" -> Pair(Color(0xFF37474F), Color(0xFF263238)) // Dark gray for thunderstorm
        "drizzle", "rain" -> Pair(Color(0xFF546E7A), Color(0xFF37474F)) // Blue-gray for rain
        "clouds" -> Pair(Color(0xFF78909C), Color(0xFF546E7A)) // Medium gray for clouds
        "atmosphere" -> Pair(Color(0xFF455A64), Color(0xFF37474F)) // Dark gray for atmosphere
        "snow" -> Pair(Color(0xFFE3F2FD), Color(0xFFBBDEFB))  // Snow light blue
        else -> Pair(Color(0xFF03A9F4), Color(0xFF01579B))    // Default blue
    }

    // Animation state - Updated to handle weather groups separately
    val animationType = remember(weather) {
        when (weatherCondition) {
            "thunderstorm" -> "thunderstorm"
            "drizzle", "rain" -> "rain"
            "clear" -> "clear"
            "clouds" -> "clouds"
            "atmosphere" -> "atmosphere"
            "snow" -> "snow"
            else -> "none"
        }
    }

    // Weather background sound
    WeatherBackgroundSound(animationType = animationType, lifecycleOwner = lifecycleOwner)

    // Function to load weather by location with proper error handling
    fun loadWeatherByLocation(location: Location?) {
        scope.launch {
            try {
                location?.let {
                    val lat = it.latitude
                    val lon = it.longitude
                    val apiKey = "0163914bfedc415e17c2470bdae1de4f"
                    viewModel.loadWeather(lat, lon, apiKey)
                    viewModel.loadForecastByCoordinates(lat, lon, apiKey)
                    viewModel.loadAirQuality(lat, lon, apiKey)
                    snackbarHostState.showSnackbar("Showing weather for your location.")
                } ?: run {
                    snackbarHostState.showSnackbar("Unable to get current location.")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error loading weather: ${e.message}")
            } finally {
                isLocationLoading = false
                // Note: Removed isRefreshing = false from here to fix pull-to-refresh issue
            }
        }
    }

    // Function to load weather by city
    fun loadWeatherByCity(cityName: String) {
        scope.launch {
            try {
                val apiKey = "0163914bfedc415e17c2470bdae1de4f"
                viewModel.loadWeatherByCity(cityName, apiKey)
                viewModel.loadForecast(cityName, apiKey)
                locationService.getCoordinatesFromCity(cityName) { lat, lon ->
                    viewModel.loadAirQuality(lat, lon, apiKey)
                }
                snackbarHostState.showSnackbar("Showing weather for $cityName.")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error loading weather for $cityName: ${e.message}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(startColor, endColor),
                        start = Offset(0f, 0f),
                        end = Offset(1f, 1f)
                    )
                )
        )

        // Weather animation layer - moved to background with optimized rendering
        WeatherAnimation(
            animationType = animationType,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f) // Ensure it's at the background
        )

        // Main content layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f) // Ensure it's above the animation
        ) {
            // Conditionally show weather content or settings
            if (!showSettings) {
                // Search field overlay - positioned at the top
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .zIndex(1f)
                ) {
                    // Top bar with search and settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search field
                        OutlinedTextField(
                            value = cityInput,
                            onValueChange = {
                                cityInput = it
                                // Only trigger GPS location if field is blank and initialization is complete
                                if (it.text.isBlank() && hasInitialized && !isLocationLoading) {
                                    isLocationLoading = true
                                    try {
                                        locationService.getCurrentLocation { location ->
                                            loadWeatherByLocation(location)
                                        }
                                    } catch (e: Exception) {
                                        scope.launch {
                                            isLocationLoading = false
                                            snackbarHostState.showSnackbar("Error getting location: ${e.message}")
                                        }
                                    }
                                }
                            },
                            label = { Text("Enter city name", color = Color.White) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                cursorColor = Color.White
                            ),
                            trailingIcon = {
                                Row {
                                    if (cityInput.text.isNotEmpty()) {
                                        IconButton(onClick = { cityInput = TextFieldValue("") }) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                    IconButton(onClick = {
                                        val cleanedCity = cityInput.text.trim()
                                        when {
                                            cleanedCity.isBlank() -> {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Please enter a city name.")
                                                }
                                            }
                                            !cleanedCity.matches(Regex("^[a-zA-Z\\s-]{2,}$")) -> {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Invalid city name format.")
                                                }
                                            }
                                            else -> {
                                                loadWeatherByCity(cleanedCity)
                                            }
                                        }
                                    }) {
                                        Icon(
                                            Icons.Filled.Search,
                                            contentDescription = "Search",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        // Settings button
                        IconButton(
                            onClick = { showSettings = true },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }
                    // Snackbar
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Updated PullToRefreshBox with proper state management
                PullToRefreshBox(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        scope.launch {
                            isRefreshing = true
                            try {
                                // Always try to get GPS location on refresh
                                isLocationLoading = true
                                locationService.getCurrentLocation { location ->
                                    loadWeatherByLocation(location)
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error refreshing weather: ${e.message}")
                            } finally {
                                // Ensure isRefreshing is set to false after a small delay
                                // to allow the refresh animation to complete
                                delay(300)
                                isRefreshing = false
                            }
                        }
                    }
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 120.dp) // Space for search field
                            .padding(horizontal = 16.dp)
                    ) {
                        // Show welcome message if no weather data is available
                        if (weather == null && forecast.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (isLocationLoading) {
                                                "Getting your location..."
                                            } else {
                                                "Getting your location..."
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        // Main Weather
                        if (weather != null) {
                            item {
                                WeatherCard(weather = weather, useFahrenheit = useFahrenheit)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                        // Hourly Forecast
                        if (forecast.isNotEmpty()) {
                            item {
                                ForecastSection(
                                    forecastList = forecast,
                                    isLoading = isForecastLoading,
                                    useFahrenheit = useFahrenheit
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                        // Weather Metrics Grid
                        if (weather != null) {
                            item {
                                // Create a local variable to enable smart casting
                                val currentWeather = weather
                                val metrics = listOf(
                                    WeatherMetric(
                                        icon = Icons.Default.WaterDrop,
                                        label = "Humidity",
                                        value = "${currentWeather?.main?.humidity}%",
                                        description = "Current humidity",
                                        arrowDegrees = null
                                    ),
                                    WeatherMetric(
                                        icon = Icons.Default.Air,
                                        label = "Wind",
                                        value = "${currentWeather?.wind?.speed} m/s",
                                        description = "Wind speed",
                                        arrowDegrees = currentWeather?.wind?.deg?.toFloat()
                                    ),
                                    WeatherMetric(
                                        icon = Icons.Default.Thermostat,
                                        label = "Pressure",
                                        value = "${currentWeather?.main?.pressure} hPa",
                                        description = "Atmospheric pressure",
                                        arrowDegrees = null
                                    ),
                                    WeatherMetric(
                                        icon = Icons.Default.Air,
                                        label = "Air Quality",
                                        value = airQuality?.list?.firstOrNull()?.main?.aqi?.toString() ?: "N/A",
                                        description = "Current AQI",
                                        arrowDegrees = null
                                    )
                                )
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(256.dp), // Fixed height for the grid (2 rows * 120dp + 8dp spacing)
                                    contentPadding = PaddingValues(0.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(metrics.size) { index ->
                                        WeatherMetricCard(metric = metrics[index])
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                        // Temperature Graph
                        if (forecast.isNotEmpty()) {
                            item {
                                TemperatureGraphCard(forecastList = forecast)
                                // Add bottom padding to ensure content isn't hidden behind navigation
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
            // Settings Screen Overlay
            if (showSettings) {
                SettingsScreen(
                    useNotifications = useNotifications,
                    onToggleNotifications = { viewModel.setUseNotifications(it) },
                    useFahrenheit = useFahrenheit,
                    onToggleFahrenheit = { viewModel.setUseFahrenheit(it) },
                    onBack = { showSettings = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Always get GPS location on app launch
    LaunchedEffect(Unit) {
        if (!hasShownWelcomeMessage) {
            hasShownWelcomeMessage = true
            isLocationLoading = true
            try {
                locationService.getCurrentLocation { location ->
                    loadWeatherByLocation(location)
                }
            } catch (e: Exception) {
                scope.launch {
                    isLocationLoading = false
                    snackbarHostState.showSnackbar("Error getting location: ${e.message}")
                }
            }
        }
    }

    // Check for weather notifications when weather data changes
    LaunchedEffect(weather) {
        if (useNotifications) {
            weather?.let { weatherData ->
                WeatherNotificationManager.checkAndSendWeatherNotification(
                    context = context,
                    weather = weatherData,
                    scope = scope
                )
            }
        }
    }
}

@Composable
fun WeatherBackgroundSound(
    animationType: String,
    lifecycleOwner: LifecycleOwner
) {
    val context = LocalContext.current
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    var wasPlaying by remember { mutableStateOf(false) }

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // App is going to background
                    mediaPlayer.value?.let { player ->
                        if (player.isPlaying) {
                            wasPlaying = true
                            player.pause()
                        }
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    // App is coming to foreground
                    if (wasPlaying) {
                        mediaPlayer.value?.start()
                        wasPlaying = false
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    // App is completely stopped
                    mediaPlayer.value?.release()
                    mediaPlayer.value = null
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Handle animation type changes
    LaunchedEffect(animationType) {
        // Release previous media player if exists
        mediaPlayer.value?.release()

        // Create new media player based on weather type
        val soundResId = when (animationType) {
            "thunderstorm" -> {
                // Try to get the thunderstorm sound resource, but handle if it doesn't exist
                try {
                    R.raw::class.java.getField("thunderstorm_sound").getInt(null)
                } catch (e: Exception) {
                    // If thunderstorm_sound doesn't exist, use rain_sound as fallback
                    R.raw.rain_sound
                }
            }
            "rain" -> R.raw.rain_sound  // Rain sound for drizzle and rain
            else -> null  // No sound for other conditions
        }

        if (soundResId != null) {
            try {
                val player = MediaPlayer.create(context, soundResId)
                player.isLooping = true
                player.setVolume(0.5f, 0.5f) // Set volume to 50%
                player.start()
                mediaPlayer.value = player
            } catch (e: Exception) {
                // Handle exception (e.g., file not found)
                e.printStackTrace()
            }
        }
    }

    // Clean up when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.value?.release()
        }
    }
}

// Data class for a rain particle
private data class RandomRainParticle(
    val x: Float = Random.nextFloat(),
    val y: Float = Random.nextFloat(),
    val width: Float = Random.nextFloat() * 1.5f + 0.5f,
    val length: Float = Random.nextFloat() * 10f + 5f,
    val speed: Float = Random.nextFloat() * 0.3f + 0.2f,
    val alpha: Float = Random.nextFloat() * 0.4f + 0.1f // Add alpha for visual variety
)

// Data class for a snow particle
private data class RandomSnowParticle(
    val x: Float = Random.nextFloat(),
    val y: Float = Random.nextFloat(),
    val radius: Float = Random.nextFloat() * 4f + 1f,
    val speed: Float = Random.nextFloat() * 0.1f + 0.05f,
    val alpha: Float = Random.nextFloat() * 0.6f + 0.4f
)

@Composable
fun WeatherAnimation(
    animationType: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val density = LocalDensity.current

    when (animationType) {
        "thunderstorm" -> {
            // Thunderstorm animation - dark background with lightning effect
            val lightningAlpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 5000
                        0f at 0
                        0f at 4000
                        1f at 4100
                        0f at 4200
                        0f at 5000
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
            Canvas(modifier = modifier.fillMaxSize()) {
                try {
                    // Draw lightning flash
                    if (lightningAlpha > 0) {
                        drawRect(
                            color = Color.White.copy(alpha = lightningAlpha * 0.7f),
                            size = size
                        )
                    }
                } catch (e: Exception) {
                    // Silently handle exceptions to prevent crashes
                }
            }
        }
        "rain" -> {
            // Particle Flow Animation for Rain (Drizzle, Rain groups) - optimized
            val rainParticles = remember {
                List(40) {  // Reduced from 80 to 40 for lighter rain
                    RandomRainParticle(
                        width = with(density) { (Random.nextFloat() * 1.5f + 0.5f).dp.toPx() }, // Smaller drops
                        length = with(density) { (Random.nextFloat() * 10f + 5f).dp.toPx() }, // Shorter drops
                        speed = Random.nextFloat() * 0.3f + 0.2f // Slower speed
                    )
                }
            }
            val time by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing), // Slower animation
                    repeatMode = RepeatMode.Restart
                )
            )
            Canvas(modifier = modifier) {
                try {
                    rainParticles.forEach { particle ->
                        val x = particle.x * size.width
                        val y = (particle.y + time * particle.speed) % 1f * size.height
                        // Draw raindrop
                        drawLine(
                            color = Color(0xFF64B5F6).copy(alpha = particle.alpha),
                            start = Offset(x, y),
                            end = Offset(x, y + particle.length),
                            strokeWidth = particle.width
                        )
                    }
                } catch (e: Exception) {
                    // Silently handle exceptions to prevent crashes
                }
            }
        }
        "clouds" -> {
            // Cloud Animation for Clouds group - reverted to original code
            val cloudOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 30f,
                animationSpec = infiniteRepeatable(
                    animation = tween(8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Canvas(modifier = modifier.fillMaxSize()) {
                try {
                    // Position clouds lower on the screen
                    val topY = with(density) { 142.dp.toPx() }
                    // Draw 3 smaller gray clouds that match the background
                    drawCloud(size.width * 0.2f, topY + cloudOffset, 1.5f, density, Color(0xFFB0BEC5).copy(alpha = 0.8f))
                    drawCloud(size.width * 0.5f, topY - with(density) { 10.dp.toPx() } - cloudOffset * 1.8f, 1.0f, density, Color(0xFFB0BEC5).copy(alpha = 0.8f))
                    drawCloud(size.width * 0.8f, topY + with(density) { 5.dp.toPx() } + cloudOffset * 2.0f, 0.7f, density, Color(0xFFB0BEC5).copy(alpha = 0.8f))
                } catch (e: Exception) {
                    // Silently handle exceptions to prevent crashes
                }
            }
        }
        "atmosphere" -> {
            // Cloud Animation for Atmosphere group - reverted to original code
            val cloudOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 30f,
                animationSpec = infiniteRepeatable(
                    animation = tween(8000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Canvas(modifier = modifier.fillMaxSize()) {
                try {
                    // Position clouds lower on the screen
                    val topY = with(density) { 180.dp.toPx() }
                    // Draw 3 smaller dark gray clouds that match the background
                    drawCloud(size.width * 0.2f, topY + cloudOffset, 0.8f, density, Color(0xFF607D8B).copy(alpha = 0.7f))
                    drawCloud(size.width * 0.5f, topY - with(density) { 10.dp.toPx() } - cloudOffset * 0.5f, 0.7f, density, Color(0xFF607D8B).copy(alpha = 0.7f))
                    drawCloud(size.width * 0.8f, topY + with(density) { 5.dp.toPx() } + cloudOffset * 0.7f, 0.9f, density, Color(0xFF607D8B).copy(alpha = 0.7f))
                } catch (e: Exception) {
                    // Silently handle exceptions to prevent crashes
                }
            }
        }
        "snow" -> {
            // Particle Flow Animation for Snow (Snow group) - optimized
            val snowParticles = remember {
                List(60) {
                    RandomSnowParticle(
                        radius = with(density) { (Random.nextFloat() * 4f + 1f).dp.toPx() },
                        speed = Random.nextFloat() * 0.1f + 0.05f
                    )
                }
            }
            val time by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
            Canvas(modifier = modifier) {
                try {
                    snowParticles.forEach { particle ->
                        val x = particle.x * size.width
                        val y = (particle.y + time * particle.speed) % 1f * size.height
                        // Draw snowflake
                        drawCircle(
                            color = Color.White.copy(alpha = particle.alpha),
                            radius = particle.radius,
                            center = Offset(x, y)
                        )
                    }
                } catch (e: Exception) {
                    // Silently handle exceptions to prevent crashes
                }
            }
        }
        else -> {
            // No animation for other cases
        }
    }
}

// Utility function to draw a cloud - reverted to original code
private fun DrawScope.drawCloud(x: Float, y: Float, scale: Float, density: Density, color: Color) {
    val cloudRadius = with(density) { 20.dp.toPx() * scale }
    val cloudOffset = with(density) { 10.dp.toPx() * scale }
    // Main circle
    drawCircle(color, cloudRadius, Offset(x, y))
    // Side circles
    drawCircle(color, cloudRadius * 0.8f, Offset(x - cloudRadius * 0.8f, y + cloudOffset))
    drawCircle(color, cloudRadius * 0.8f, Offset(x + cloudRadius * 0.8f, y + cloudOffset))
    drawCircle(color, cloudRadius * 0.6f, Offset(x - cloudRadius, y))
    drawCircle(color, cloudRadius * 0.6f, Offset(x + cloudRadius, y))
}