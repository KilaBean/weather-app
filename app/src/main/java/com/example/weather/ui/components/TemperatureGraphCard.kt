package com.example.weather.ui.components
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.data.model.ForecastItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TemperatureGraphCard(forecastList: List<ForecastItem>, useFahrenheit: Boolean = false) {
    if (forecastList.isEmpty()) return
    val currentDate = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }
    val unitSymbol = if (useFahrenheit) "°F" else "°C"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Temperature Trend",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFF5F5F5),
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(
                    elevation = 40.dp,
                    shape = MaterialTheme.shapes.medium,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.05f)
                )
                .clip(MaterialTheme.shapes.medium),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.3f)  // Semi-transparent white background with 30% opacity
            ),
            elevation = CardDefaults.cardElevation(0.dp)  // Set to 0 since we're using the custom shadow modifier
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Add horizontal scrolling
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                ) {
                    // Calculate minimum width needed for all data points
                    val minGraphWidth = if (forecastList.size > 8) {
                        100.dp * forecastList.size
                    } else {
                        0.dp // Let it fill the container
                    }
                    TemperatureTrendGraph(
                        forecastList = forecastList,
                        useFahrenheit = useFahrenheit,
                        unitSymbol = unitSymbol,
                        modifier = Modifier
                            .width(minGraphWidth)
                            .height(200.dp)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TemperatureTrendGraph(
    forecastList: List<ForecastItem>,
    useFahrenheit: Boolean = false,
    unitSymbol: String = "°C",
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(forecastList) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = EaseOutQuart)
        )
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val padding = 16.dp.toPx()
        val graphHeight = height - 40.dp.toPx() // Reserve space for labels
        // Get temperature values and convert if needed
        val temps = forecastList.map {
            if (useFahrenheit) it.main.temp * 9/5 + 32 else it.main.temp
        }
        // Find min and max temperatures for scaling
        val minTemp = temps.minOrNull() ?: 0.0
        val maxTemp = temps.maxOrNull() ?: 0.0
        val tempRange = if (maxTemp - minTemp > 0) maxTemp - minTemp else 1.0 // Prevent division by zero
        // Create points for the graph
        val points = temps.mapIndexed { index, temp ->
            val x = padding + (index.toFloat() / (temps.size - 1)) * (width - 2 * padding)
            val y = graphHeight - padding - ((temp - minTemp) / tempRange) * (graphHeight - 2 * padding)
            Offset(x, y.toFloat())
        }
        // Draw gradient background
        val gradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE3F2FD).copy(alpha = 0.3f),
                Color.Transparent
            ),
            startY = 0f,
            endY = graphHeight
        )
        drawRect(
            brush = gradient,
            topLeft = Offset(padding, padding),
            size = androidx.compose.ui.geometry.Size(width - 2 * padding, graphHeight - 2 * padding)
        )
        // Draw grid lines (now invisible)
        val gridColor = Color.Gray.copy(alpha = 0f) // Changed alpha to 0f to make grid lines invisible
        // Horizontal grid lines
        for (i in 0..4) {
            val y = padding + (i / 4f) * (graphHeight - 2 * padding)
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        // Vertical grid lines
        for (i in 0 until forecastList.size) {
            val x = padding + (i.toFloat() / (forecastList.size - 1)) * (width - 2 * padding)
            drawLine(
                color = gridColor,
                start = Offset(x, padding),
                end = Offset(x, graphHeight - padding),
                strokeWidth = 1.dp.toPx()
            )
        }
        // Draw the temperature line with animation
        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                // Animate the path drawing
                val animatedPoints = points.take((points.size * animatedProgress.value).toInt())
                for (i in 1 until animatedPoints.size) {
                    lineTo(animatedPoints[i].x, animatedPoints[i].y)
                }
            }
        }
        drawPath(
            path = path,
            color = Color.White, // Changed from blue to white
            style = Stroke(width = 3.dp.toPx())
        )
        // Draw gradient fill under the line
        val fillPath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                // Animate the path drawing
                val animatedPoints = points.take((points.size * animatedProgress.value).toInt())
                for (i in 1 until animatedPoints.size) {
                    lineTo(animatedPoints[i].x, animatedPoints[i].y)
                }
                // Complete the path to create a fill area
                if (animatedPoints.isNotEmpty()) {
                    lineTo(animatedPoints.last().x, graphHeight - padding)
                    lineTo(points.first().x, graphHeight - padding)
                    close()
                }
            }
        }
        val fillGradient = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.4f), // Changed from blue to white
                Color.White.copy(alpha = 0.1f), // Changed from blue to white
                Color.Transparent
            )
        )
        drawPath(
            path = fillPath,
            brush = fillGradient
        )
        // Draw points with animation
        points.forEachIndexed { index, point ->
            if (index < (points.size * animatedProgress.value).toInt()) {
                // Outer circle
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = point
                )
                // Inner circle
                drawCircle(
                    color = Color.White, // Changed from blue to white
                    radius = 5.dp.toPx(),
                    center = point
                )
                // Temperature value at the point
                val tempText = "${temps[index].toInt()}$unitSymbol"
                drawText(
                    textMeasurer = textMeasurer,
                    text = tempText,
                    topLeft = Offset(
                        x = point.x - 15.dp.toPx(),
                        y = point.y - 25.dp.toPx()
                    ),
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 10.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
        // Draw temperature labels for min and max
        val maxTempText = "${maxTemp.toInt()}$unitSymbol"
        val minTempText = "${minTemp.toInt()}$unitSymbol"
        // Draw max temperature label
        drawText(
            textMeasurer = textMeasurer,
            text = maxTempText,
            topLeft = Offset(padding, padding),
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 12.sp,
                color = Color.Black,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )
        // Draw min temperature label
        drawText(
            textMeasurer = textMeasurer,
            text = minTempText,
            topLeft = Offset(padding, graphHeight - padding - 15.dp.toPx()),
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 12.sp,
                color = Color.Black,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )
        // Draw time labels at the bottom
        if (forecastList.size <= 8) { // Only show labels if not too many
            forecastList.forEachIndexed { index, item ->
                val x = padding + (index.toFloat() / (forecastList.size - 1)) * (width - 2 * padding)
                val timeText = formatToHour(item.dt_txt)
                drawText(
                    textMeasurer = textMeasurer,
                    text = timeText,
                    topLeft = Offset(
                        x = x - 15.dp.toPx(), // Center the text
                        y = graphHeight + 5.dp.toPx()
                    ),
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 10.sp,
                        color = Color.Black.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
        // Draw average temperature line
        if (temps.isNotEmpty()) {
            val avgTemp = temps.average()
            val avgY = graphHeight - padding - ((avgTemp - minTemp) / tempRange) * (graphHeight - 2 * padding)
            drawLine(
                color = Color.Red.copy(alpha = 0.5f),
                start = Offset(padding, avgY.toFloat()),
                end = Offset(width - padding, avgY.toFloat()),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
            )
            // Average temperature label
            val avgTempText = "Avg: ${avgTemp.toInt()}$unitSymbol"
            drawText(
                textMeasurer = textMeasurer,
                text = avgTempText,
                topLeft = Offset(width - padding - 50.dp.toPx(), (avgY - 15.dp.toPx()).toFloat()),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 10.sp,
                    color = Color.Red.copy(alpha = 0.7f),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }
    }
}

// Extension function to draw text using TextMeasurer
private fun DrawScope.drawText(
    textMeasurer: TextMeasurer,
    text: String,
    topLeft: Offset,
    style: androidx.compose.ui.text.TextStyle
) {
    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = style
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = topLeft
    )
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