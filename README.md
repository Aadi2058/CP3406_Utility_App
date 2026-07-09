# WeatherForecast – CP3406 / CP5307 Assessment 1

A weather utility app built with **Jetpack Compose**, **MVVM architecture**, and **Retrofit**.
It fetches live weather data from the [OpenWeatherMap API](https://openweathermap.org/current) and displays it in a Material Design 3 UI.

---

## Features

- Live current weather for any city: temperature, condition + icon, feels-like, high/low, humidity, wind
- Pull-to-refresh (Material 3 `PullToRefreshBox`)
- Settings screen: change city, toggle Celsius/Fahrenheit
- Error handling with retry (invalid city / no network never crashes the app)
- Light/dark theme with Android 12+ dynamic color

---

## How to Run

1. Clone or download this repo
2. Open in Android Studio and let Gradle sync
3. Run on an emulator or physical device (API 26+ recommended)

---

## Architecture (MVVM)

```
UI (Composables) → ViewModel → Repository → Retrofit → OpenWeatherMap API
       ↑                                                      ↓
       └────────── state updates ← Gson ← JSON response ──────┘
```

| File | Role |
|------|------|
| `MainActivity.kt` | All UI: navigation, weather card, settings screen |
| `Weatherviemodel.kt` | Holds state (weather, loading, error, city, units); runs fetch in a coroutine |
| `WeatherRepository.kt` | Data layer; makes the API call, holds the API key |
| `WeatherApiService.kt` | Retrofit interface defining the GET request and query parameters |
| `RetrofitInstance.kt` | Retrofit singleton with base URL and Gson converter |
| `WeatherResponse.kt` | Data classes matching the API's JSON structure |

---

## API

- **Endpoint:** `https://api.openweathermap.org/data/2.5/weather`
- **Parameters:** `q` (city), `appid` (API key), `units` (metric/imperial)
- Example request: `.../weather?q=Brisbane&appid=KEY&units=metric`
- Response is JSON, converted to Kotlin objects by Gson

---

## Dependencies

- Jetpack Compose + Material 3 (UI)
- Retrofit 2 + Gson converter (networking / JSON)
- Lifecycle ViewModel Compose (state management)
- Coil (loads the weather condition icon from a URL)

---

## Known Limitations / Future Work

- API key is hardcoded in `WeatherRepository.kt` — should move to `local.properties` for production
- Settings don't persist across restarts — DataStore would fix this
- Could add a 5-day forecast using the `/forecast` endpoint

---

## License

Built on the CP3406 educational starter template.
