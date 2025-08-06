package com.example.weather.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val name: String,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind // ✅ Add wind block
)

data class Weather(
    val main: String,
    val description: String,
    val icon: String
)

data class Main(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    val humidity: Int,       // ✅ Add humidity
    val pressure: Int        // Optional, used in detailed bottom sheet
)

data class Wind(
    val speed: Double,
    val deg: Int
)
