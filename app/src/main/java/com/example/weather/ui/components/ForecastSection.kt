package com.example.weather.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.weather.data.model.ForecastItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ForecastSection(
    forecastList: List<ForecastItem>,
    isLoading: Boolean,
    useFahrenheit: Boolean = false
) {
    val listState = rememberLazyListState()

    // Auto-scroll to current hour
    LaunchedEffect(forecastList) {
        if (forecastList.isNotEmpty()) {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val targetIndex = forecastList.indexOfFirst {
                try {
                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val date = format.parse(it.dt_txt)
                    val hour = if (date != null) {
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        calendar.get(Calendar.HOUR_OF_DAY)
                    } else {
                        -1
                    }
                    hour == currentHour
                } catch (e: Exception) { false }
            }.coerceAtLeast(0)
            listState.animateScrollToItem(index = targetIndex)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header Title
        Text(
            text = "Hourly Forecast",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFF5F5F5),
            modifier = Modifier
                .padding(start = 16.dp, bottom = 8.dp)
                .placeholder(
                    visible = isLoading,
                    color = Color.Gray.copy(alpha = 0.3f)
                )
        )
        // Forecast LazyRow
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                items(6) { ForecastCardPlaceholder() }
            } else {
                items(forecastList.size) { index ->
                    ForecastCard(
                        item = forecastList[index],
                        useFahrenheit = useFahrenheit
                    )
                }
            }
        }
    }
}