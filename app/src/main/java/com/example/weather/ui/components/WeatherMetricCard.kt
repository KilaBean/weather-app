package com.example.weather.ui.components
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.weather.ui.model.WeatherMetric

@Composable
fun WeatherMetricCard(metric: WeatherMetric) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // Fixed height instead of aspect ratio
            .shadow(
                elevation = 40.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)  // Semi-transparent white background with 30% opacity
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)  // Set to 0 since we're using the custom shadow modifier
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Icon(
                    imageVector = metric.icon,
                    contentDescription = metric.label,
                    tint = Color(0xFFFFFFFF) // White hex color for icon
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFF5F5F5) // White hex color for text
                )
            }
            Text(
                text = metric.value,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color(0xFFF5F5F5) // White hex color for text
            )
            Text(
                text = metric.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFF5F5F5) // White hex color for text
            )
            metric.arrowDegrees?.let {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Direction",
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(it),
                    tint = Color(0xFFFFFFFF) // White hex color for icon
                )
            }
        }
    }
}