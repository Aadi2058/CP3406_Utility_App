package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    var weatherData by mutableStateOf<WeatherResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var units by mutableStateOf("metric")

    var city by mutableStateOf("Brisbane")

    fun fetchWeather() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                weatherData = repository.getWeather(city, units)
            } catch (e: Exception) {
                errorMessage = "Could not load weather. Check city name."
            }
            isLoading = false
        }
    }
}