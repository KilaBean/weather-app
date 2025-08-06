package com.example.weather.data.repository

import com.example.weather.data.model.ForecastResponse
import com.example.weather.data.model.WeatherResponse
import com.example.weather.data.model.AirQualityResponse
import com.example.weather.data.remote.WeatherApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {
    private val api: WeatherApi = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)

    suspend fun getWeather(lat: Double, lon: Double, apiKey: String): WeatherResponse {
        return api.getCurrentWeather(lat, lon, apiKey)
    }

    suspend fun getWeatherByCity(city: String, apiKey: String): WeatherResponse {
        return api.getWeatherByCity(city, apiKey)
    }

    suspend fun getForecast(city: String, apiKey: String): ForecastResponse {
        return api.getForecast(city, apiKey)
    }

    suspend fun getForecastByCoordinates(lat: Double, lon: Double, apiKey: String): ForecastResponse {
        return api.getForecastByCoordinates(lat, lon, apiKey)
    }

    // ✅ Fixed: use the correct method name from WeatherApi
    suspend fun getAirQuality(lat: Double, lon: Double, apiKey: String): AirQualityResponse {
        return api.getAirQualityByCoords(lat, lon, apiKey)
    }
}
