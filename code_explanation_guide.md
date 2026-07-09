# Code Explanation Guide — Weather Utility App

How to walk your teacher through the app: what each file does, how data flows, and exactly what you changed.

---

## 1. Start with the big picture (30 seconds)

> "It's a weather app built with Jetpack Compose using MVVM architecture. It fetches live data from the OpenWeatherMap API using Retrofit, and has two screens — the weather display and a settings screen for changing city and temperature units."

Then explain the data flow — this is the most important thing to get right:

```
UI (Composables) → ViewModel → Repository → Retrofit API → OpenWeatherMap
                                                              ↓ JSON
UI recomposes  ←  state updates  ←  Gson converts JSON → WeatherResponse
```

The UI never talks to the internet directly. It asks the ViewModel, the ViewModel asks the Repository, the Repository calls the API. The response flows back up and the UI updates automatically because it observes state.

---

## 2. File-by-file walkthrough

Explain them in this order — it follows the data flow.

### WeatherApiService.kt — "the contract with the API"
A Retrofit interface. `@GET("weather")` means "make a GET request to the /weather endpoint". The `@Query` parameters become URL parameters (`?q=Brisbane&appid=...&units=metric`). `suspend` means it runs in a coroutine so it doesn't freeze the UI while waiting for the network.

### RetrofitInstance.kt — "the network setup"
A singleton (`object`) that builds Retrofit once with the base URL `api.openweathermap.org/data/2.5/` and a Gson converter (turns JSON text into Kotlin objects). `by lazy` means it's only created the first time it's used.

### WeatherResponse.kt — "the shape of the data"
Data classes that mirror the JSON structure the API returns. Gson matches JSON field names to property names automatically. Where they differ (JSON uses `feels_like`, Kotlin convention is `feelsLike`), `@SerializedName` bridges the gap.

### WeatherRepository.kt — "the data layer"
A thin wrapper around the API call that holds the API key. The point: the ViewModel doesn't need to know *where* data comes from. If I swapped the API or added caching, only this file changes.

### Weatherviemodel.kt — "the brain"
Holds all app state: `weatherData`, `isLoading`, `errorMessage`, `city`, `units` — each in `mutableStateOf`, so Compose automatically redraws whatever reads them when they change. `fetchWeather()` launches a coroutine in `viewModelScope`, sets loading, calls the repository in a try/catch, and stores either the result or an error message. `private set` means only the ViewModel can change its own state — the UI just reads it.

### MainActivity.kt — "the UI"
- `UtilityApp()` — a `Scaffold` with a bottom `NavigationBar`; a `selectedTab` state variable switches between the two screens.
- `UtilityScreen()` — `LaunchedEffect(Unit)` fetches weather once when the screen first appears. Then it shows one of three states: error (with Retry), or the weather card. Loading shows via the pull-to-refresh spinner.
- `WeatherCard()` — the main display: city, weather icon, temperature, description, feels-like, high/low, plus humidity and wind cards.
- `SettingsScreen()` — text field for city, radio buttons for Celsius/Fahrenheit; pressing Apply updates the ViewModel and refetches.

### ui/theme/ folder — "the app's look"

**Color.kt** — Defines the color palette as constants (`Purple80`, `Purple40`, etc. — the "80"/"40" refer to Material Design tone values: lighter shades for dark theme, darker for light theme).

**Theme.kt** — The app's `MaterialTheme` wrapper. It picks a color scheme in this order: on Android 12+ it uses **dynamic color** (colors generated from the user's wallpaper), otherwise it falls back to the purple `DarkColorScheme`/`LightColorScheme` depending on the system dark-mode setting. Every composable inside it can then use `MaterialTheme.colorScheme.primaryContainer` etc. — which is why my weather card automatically matches light/dark mode without any extra code.

**Type.kt** — Typography definitions (font sizes, weights) used via `MaterialTheme.typography.headlineMedium` etc.

### AndroidManifest.xml — "the app's ID card"
Declares to Android what the app is and needs: the **INTERNET permission** (required for the API calls — without this line every network request fails), the app icon/label, and that `MainActivity` is the launcher activity (the entry point when the icon is tapped).

### Gradle / build files — "how the app is built"

**app/build.gradle.kts** — The module build file, the one that matters most. Declares: `compileSdk`/`minSdk`/`targetSdk` (built against API 36, runs on 24+), the application ID, Kotlin/Compose setup, and **all dependencies** — Compose UI, Material 3, Retrofit + Gson converter, lifecycle-viewmodel-compose, and Coil (the one I added).

**build.gradle.kts** (project root) — Top-level build file; just registers the plugins. Doesn't list dependencies.

**settings.gradle.kts** — Tells Gradle which modules the project has (just `:app`) and where to download dependencies from (Google, Maven Central).

**gradle/libs.versions.toml** — The version catalog: a central place listing library versions so `build.gradle.kts` can reference them as `libs.androidx.material3` instead of hardcoding version numbers.

**gradle.properties / gradle wrapper files** — Build configuration and the pinned Gradle version so the project builds identically on any machine.

**proguard-rules.pro** — Rules for code shrinking/obfuscation in release builds (unused here since minify is off).

### res/ folder — "non-code resources"
`values/strings.xml` (app name), `mipmap/` (launcher icons), `xml/` (backup rules). Compose apps keep this folder small because layouts live in Kotlin, not XML.

### Test files
**ExampleUnitTest.kt** (`src/test`) — runs on your computer's JVM, no device needed. **ExampleInstrumentedTest.kt** (`src/androidTest`) — runs on a device/emulator. Both are the template defaults.

---

## 3. What I changed (the enhancements) and where

### Change 1 — Extended the data model
**File:** `WeatherResponse.kt`
Added `feelsLike`, `tempMin`, `tempMax` to `Main` (using `@SerializedName` because the API calls them `feels_like` etc.), and a new `Wind` data class for wind speed. The API was already sending this data — I just wasn't parsing it before.

### Change 2 — Redesigned the weather screen
**File:** `MainActivity.kt` (the `UtilityScreen`, `WeatherCard`, `WeatherDetailCard` composables)
- Replaced the plain text column with a **Card layout** using Material 3 theming (`primaryContainer` color, rounded corners).
- Added the **weather condition icon** loaded from OpenWeatherMap's icon URL using Coil's `AsyncImage` — the icon code (e.g. `04d`) comes from the API response.
- Added **feels-like, high/low, humidity and wind** displays using the new model fields.
- Replaced the Refresh button with **pull-to-refresh** (`PullToRefreshBox` from Material 3) — the standard mobile pattern. Its `isRefreshing` is bound to the ViewModel's `isLoading`, so the same state drives both first-load and refresh spinners.

### Change 3 — New dependency
**File:** `app/build.gradle.kts`
Added `io.coil-kt:coil-compose:2.7.0` — Coil is the standard Compose library for loading images from URLs (handles downloading, caching, and displaying asynchronously).

---

## 4. Questions your teacher will probably ask

**"Why MVVM instead of putting everything in the Activity?"**
Separation of concerns. When I redesigned the whole UI, I didn't touch a single line of networking code. Also, the ViewModel survives screen rotation, so the weather data isn't refetched on every rotate.

**"What does `suspend` do?"**
Marks a function that can pause without blocking the thread. The network call pauses the coroutine, the UI stays responsive, and it resumes when the response arrives.

**"What is recomposition?"**
When a `mutableStateOf` value changes, Compose automatically re-runs (recomposes) only the composables that read that value. That's why there's no manual "update the screen" code anywhere.

**"What does `LaunchedEffect(Unit)` do?"**
Runs a coroutine once when the composable first enters the screen — used here to auto-fetch weather on launch instead of requiring a button press.

**"How does the units toggle work?"**
The `units` state ("metric"/"imperial") is passed as a query parameter to the API, so the API itself returns Celsius or Fahrenheit — the app doesn't convert anything. The UI reads the same state to pick the °C/°F and m/s vs mph labels.

**"What happens if the API call fails?"**
The try/catch in `fetchWeather()` catches the exception, sets `errorMessage`, and the UI shows the message with a Retry button instead of crashing.

**"Any weaknesses you know about?"**
Yes — the API key is hardcoded in the repository (should be in `local.properties`), and settings don't persist across restarts (DataStore would fix that). Knowing your own app's flaws makes a strong impression.

---

## 5. Suggested demo order

1. Open the app — point out auto-fetch on launch (LaunchedEffect)
2. Pull down to refresh — point out the Material 3 pattern
3. Go to Settings, change city to something invalid — show the error handling + Retry
4. Fix the city, toggle Fahrenheit — show the units flow through API → UI
5. Then walk the code in the file order above
