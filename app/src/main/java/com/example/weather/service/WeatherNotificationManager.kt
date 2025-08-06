package com.example.weather.service

import android.content.Context
import com.example.weather.data.model.WeatherResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WeatherNotificationManager {
    fun checkAndSendWeatherNotification(
        context: Context,
        weather: WeatherResponse?,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            weather?.let { weatherData ->
                val condition = weatherData.weather.firstOrNull()?.main?.lowercase()
                val temp = weatherData.main.temp
                val cityName = weatherData.name

                when (condition) {
                    "rain" -> {
                        NotificationService.showWeatherNotification(
                            context = context,
                            title = "Rain Alert",
                            message = "It's raining in $cityName. Don't forget your umbrella!",
                            temperature = "${temp}°C"
                        )
                    }
                    "snow" -> {
                        NotificationService.showWeatherNotification(
                            context = context,
                            title = "Snow Alert",
                            message = "Snow expected in $cityName. Drive safely!",
                            temperature = "${temp}°C"
                        )
                    }
                    "clear" -> {
                        if (temp > 30) {
                            NotificationService.showWeatherNotification(
                                context = context,
                                title = "Hot Weather Alert",
                                message = "It's quite hot in $cityName. Stay hydrated!",
                                temperature = "${temp}°C"
                            )
                        }
                    }
                    "thunderstorm" -> {
                        NotificationService.showWeatherNotification(
                            context = context,
                            title = "Thunderstorm Warning",
                            message = "Thunderstorm expected in $cityName. Stay indoors!",
                            temperature = "${temp}°C"
                        )
                    }
                }
            }
        }
    }
}