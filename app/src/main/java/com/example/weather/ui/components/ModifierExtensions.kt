package com.example.weather.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// Common placeholder modifier extension
fun Modifier.placeholder(
    visible: Boolean,
    color: Color = Color.Gray.copy(alpha = 0.3f)
): Modifier = if (visible) this.background(color) else this

// Temperature conversion extension
fun Double.toDisplayTemperature(useFahrenheit: Boolean): Double {
    return if (useFahrenheit) this * 9/5 + 32 else this
}