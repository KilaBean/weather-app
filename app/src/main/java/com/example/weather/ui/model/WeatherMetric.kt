package com.example.weather.ui.model

import androidx.compose.ui.graphics.vector.ImageVector

data class WeatherMetric(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val description: String,
    val arrowDegrees: Float? = null
)
