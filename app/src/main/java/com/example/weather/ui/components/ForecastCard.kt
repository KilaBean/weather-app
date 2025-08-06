package com.example.weather.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.weather.data.model.ForecastItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastCard(item: ForecastItem, useFahrenheit: Boolean = false) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val unitSymbol = if (useFahrenheit) "°F" else "°C"

    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(97.dp)
            .height(137.dp)
            .shadow(
                elevation = 40.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                showBottomSheet = true
                coroutineScope.launch { sheetState.show() }
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    formatToHour(item.dt_txt),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFFFFF)
                )
                Image(
                    painter = rememberAsyncImagePainter("https://openweathermap.org/img/wn/${item.weather[0].icon}@2x.png"),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    "${item.main.temp.toDisplayTemperature(useFahrenheit).roundToInt()}$unitSymbol",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = Color(0xFFFFFFFF)
                )
                Text(
                    item.weather[0].main,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFFFFF)
                )
                val temp = item.main.temp.coerceIn(-10.0, 40.0)
                // Convert the temperature for the bar visualization if needed
                val displayTemp = if (useFahrenheit) temp * 9/5 + 32 else temp
                val targetWidth = ((displayTemp + 10) / 50f) * 100f
                val animatedWidth by animateDpAsState(targetValue = targetWidth.dp, label = "TempTrendWidth")
                val barColor = when {
                    displayTemp >= 30 -> Color(0xFFE57373)
                    displayTemp <= 10 -> Color(0xFF64B5F6)
                    else -> Color(0xFF81C784)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(animatedWidth)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(barColor)
                )
            }
        }
    }
    // Show the bottom sheet when card is clicked
    if (showBottomSheet) {
        ForecastDetailBottomSheet(
            item = item,
            sheetState = sheetState,
            useFahrenheit = useFahrenheit,
            onDismiss = { showBottomSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastDetailBottomSheet(
    item: ForecastItem,
    sheetState: SheetState,
    useFahrenheit: Boolean = false,
    onDismiss: () -> Unit
) {
    val unitSymbol = if (useFahrenheit) "°F" else "°C"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(
                    elevation = 40.dp,
                    shape = MaterialTheme.shapes.medium,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.05f)
                )
                .clip(MaterialTheme.shapes.medium),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val iconUrl = "https://openweathermap.org/img/wn/${item.weather[0].icon}@2x.png"
                val description = item.weather[0].description.replaceFirstChar { it.uppercase() }
                Text(text = formatToHour(item.dt_txt), style = MaterialTheme.typography.titleLarge)
                Image(
                    painter = rememberAsyncImagePainter(iconUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Text("Temperature: ${item.main.temp.toDisplayTemperature(useFahrenheit).roundToInt()}$unitSymbol")
                Text("Feels Like: ${item.main.feelsLike.toDisplayTemperature(useFahrenheit).roundToInt()}$unitSymbol")
                Text("Condition: $description")
                Text("Humidity: ${item.main.humidity}%")
                Text("Pressure: ${item.main.pressure} hPa")
                Text("Wind: ${item.wind.speed} m/s")
            }
        }
    }
}

private fun formatToHour(dt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("h a", Locale.getDefault())
        val date = inputFormat.parse(dt)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dt.take(5)
    }
}

@Composable
fun ForecastCardPlaceholder() {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(88.dp)
            .height(160.dp)
            .shadow(
                elevation = 40.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.5f)
                    .placeholder(
                        visible = true,
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .placeholder(
                        visible = true,
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Spacer(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.6f)
                    .placeholder(
                        visible = true,
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
            )
            Spacer(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth(0.4f)
                    .placeholder(
                        visible = true,
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
            )
        }
    }
}