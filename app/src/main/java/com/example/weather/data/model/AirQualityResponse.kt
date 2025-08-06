package com.example.weather.data.model

data class AirQualityResponse(
    val list: List<AirQualityData>
)

data class AirQualityData(
    val main: AQIMain,
    val components: AQIComponents
)

data class AQIMain(
    val aqi: Int // 1 = Good, 5 = Very Poor
)

data class AQIComponents(
    val co: Double,
    val no: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    val pm2_5: Double,
    val pm10: Double,
    val nh3: Double
)
