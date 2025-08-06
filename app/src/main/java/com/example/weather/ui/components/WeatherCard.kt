package com.example.weather.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.weather.data.model.WeatherResponse

@Composable
fun WeatherCard(weather: WeatherResponse?, useFahrenheit: Boolean = false) {
    val isLoading = weather == null
    val unitSymbol = if (useFahrenheit) "°F" else "°C"

    Box(
        modifier = Modifier
            .padding(16.dp)
            .shadow(
                elevation = 40.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(MaterialTheme.shapes.medium)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left column - City, condition, and temperature
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = weather?.name ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFFFFF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.placeholder(
                                visible = isLoading,
                                color = Color.Gray.copy(alpha = 0.3f)
                            )
                        )
                        Text(
                            text = weather?.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }
                                ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFF5F5F5),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.placeholder(
                                visible = isLoading,
                                color = Color.Gray.copy(alpha = 0.3f)
                            )
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val displayTemp = weather?.main?.temp?.toDisplayTemperature(useFahrenheit)
                        Text(
                            text = if (weather != null) "${displayTemp?.toInt()}" else "",
                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF),
                            modifier = Modifier
                                .placeholder(
                                    visible = isLoading,
                                    color = Color.Gray.copy(alpha = 0.3f)
                                )
                                .wrapContentWidth()
                        )
                        Text(
                            text = unitSymbol,
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF),
                            modifier = Modifier
                                .placeholder(
                                    visible = isLoading,
                                    color = Color.Gray.copy(alpha = 0.3f)
                                )
                                .padding(start = 2.dp, bottom = 4.dp)
                        )
                    }
                }
                // Right column - Centered Icon with Feels Like closer
                Column(
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Larger weather icon
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .placeholder(
                                visible = isLoading,
                                color = Color.Gray.copy(alpha = 0.3f)
                            )
                    ) {
                        val iconCode = weather?.weather?.firstOrNull()?.icon
                        if (!isLoading && iconCode != null) {
                            // Use the icon code from API which already includes day/night indicator
                            // For example: "01d" for clear day, "01n" for clear night
                            Image(
                                painter = rememberAsyncImagePainter(
                                    "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                                ),
                                contentDescription = "Weather icon: ${
                                    weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() }
                                } (${if (iconCode.endsWith("d")) "Day" else "Night"})",
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp)) // Closer spacing
                    val feelsLikeTemp = weather?.main?.feelsLike?.toDisplayTemperature(useFahrenheit)
                    Text(
                        text = if (weather != null) "Feels like: ${feelsLikeTemp?.toInt()}$unitSymbol" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF5F5F5),
                        modifier = Modifier.placeholder(
                            visible = isLoading,
                            color = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}