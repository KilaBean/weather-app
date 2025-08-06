package com.example.weather.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ConstantColorScheme = lightColorScheme(
    primary = WeatherPrimary,
    secondary = WeatherSecondary,
    tertiary = WeatherTertiary,
    background = WeatherBackground,
    surface = WeatherSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = WeatherText,
    onSurface = WeatherText,
)

@Composable
fun WeatherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ConstantColorScheme,
        typography = Typography,
        content = content
    )
}