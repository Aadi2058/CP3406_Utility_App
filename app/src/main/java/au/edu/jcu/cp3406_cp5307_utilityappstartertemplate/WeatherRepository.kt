package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

class WeatherRepository {
    private val api = RetrofitInstance.api
    private val apiKey = "f08b108677cd1d8d180b798b785e9098"

    suspend fun getWeather(city: String, units: String): WeatherResponse {
        return api.getWeather(city, apiKey, units)
    }
}