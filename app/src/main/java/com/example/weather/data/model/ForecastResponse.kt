package com.example.weather.data.model

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    val list: List<ForecastItem>
)

data class ForecastItem(
    val dt_txt: String,
    val main: ForecastMain,
    val weather: List<ForecastWeather>,
    val wind: ForecastWind
)

data class ForecastMain(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    val pressure: Int,
    val humidity: Int
)

data class ForecastWeather(
    val main: String,
    val description: String,
    val icon: String
)

data class ForecastWind(
    val speed: Double,
    val deg: Int
)
