package com.example.weather.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.data.model.*
import com.example.weather.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val repo = WeatherRepository()
    private val _weather = MutableStateFlow<WeatherResponse?>(null)
    val weather = _weather.asStateFlow()
    private val _forecast = MutableStateFlow<List<ForecastItem>>(emptyList())
    val forecast = _forecast.asStateFlow()
    private val _isForecastLoading = MutableStateFlow(false)
    val isForecastLoading = _isForecastLoading.asStateFlow()
    private val _airQuality = MutableStateFlow<AirQualityResponse?>(null)
    val airQuality = _airQuality.asStateFlow()

    // GPS settings state - default to false to show London on launch
    private val _useGpsLocation = MutableStateFlow(false)
    val useGpsLocation = _useGpsLocation.asStateFlow()

    // Notification settings state
    private val _useNotifications = MutableStateFlow(true)
    val useNotifications = _useNotifications.asStateFlow()

    // Temperature units settings state
    private val _useFahrenheit = MutableStateFlow(false)
    val useFahrenheit = _useFahrenheit.asStateFlow()


    fun loadWeather(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            try {
                _weather.value = repo.getWeather(lat, lon, apiKey)
            } catch (e: Exception) {
                _weather.value = null
            }
        }
    }

    fun loadWeatherByCity(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                _weather.value = repo.getWeatherByCity(city, apiKey)
            } catch (e: Exception) {
                _weather.value = null
            }
        }
    }

    fun loadForecast(city: String, apiKey: String) {
        viewModelScope.launch {
            _isForecastLoading.value = true
            try {
                _forecast.value = repo.getForecast(city, apiKey).list
            } catch (e: Exception) {
                _forecast.value = emptyList()
            } finally {
                _isForecastLoading.value = false
            }
        }
    }

    fun loadForecastByCoordinates(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            _isForecastLoading.value = true
            try {
                _forecast.value = repo.getForecastByCoordinates(lat, lon, apiKey).list
            } catch (e: Exception) {
                _forecast.value = emptyList()
            } finally {
                _isForecastLoading.value = false
            }
        }
    }

    fun loadAirQuality(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            try {
                _airQuality.value = repo.getAirQuality(lat, lon, apiKey)
            } catch (e: Exception) {
                _airQuality.value = null
            }
        }
    }

    // Function to toggle GPS location setting
    fun setUseGpsLocation(use: Boolean) {
        _useGpsLocation.value = use
    }

    // Function to toggle notifications setting
    fun setUseNotifications(use: Boolean) {
        _useNotifications.value = use
    }

    // Add this function to toggle temperature units
    fun setUseFahrenheit(use: Boolean) {
        _useFahrenheit.value = use
    }
}