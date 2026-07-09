package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.ui.theme.CP3406_CP5603UtilityAppStarterTemplateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CP3406_CP5603UtilityAppStarterTemplateTheme {
                UtilityApp()
            }
        }
    }
}

@Composable
fun UtilityApp() {
    val weatherViewModel: WeatherViewModel = viewModel()
    var selectedTab by remember { mutableStateOf("Utility") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Utility") },
                    label = { Text("Utility") },
                    selected = selectedTab == "Utility",
                    onClick = { selectedTab = "Utility" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedTab == "Settings",
                    onClick = { selectedTab = "Settings" }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                "Utility" -> UtilityScreen(weatherViewModel)
                "Settings" -> SettingsScreen(weatherViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilityScreen(viewModel: WeatherViewModel) {

    // Fetch weather when screen first loads
    LaunchedEffect(Unit) {
        viewModel.fetchWeather()
    }

    PullToRefreshBox(
        isRefreshing = viewModel.isLoading,
        onRefresh = { viewModel.fetchWeather() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                viewModel.errorMessage != null -> {
                    Text(
                        text = viewModel.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchWeather() }) {
                        Text("Retry")
                    }
                }

                viewModel.weatherData != null -> {
                    WeatherCard(viewModel.weatherData!!, viewModel.units)
                }
            }
        }
    }
}

@Composable
fun WeatherCard(weather: WeatherResponse, units: String) {
    val tempUnit = if (units == "metric") "C" else "F"
    val speedUnit = if (units == "metric") "m/s" else "mph"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = weather.name,
                style = MaterialTheme.typography.headlineMedium
            )
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${weather.weather[0].icon}@4x.png",
                contentDescription = weather.weather[0].description,
                modifier = Modifier.size(120.dp)
            )
            Text(
                text = "${weather.main.temp.toInt()}°$tempUnit",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = weather.weather[0].description.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Feels like ${weather.main.feelsLike.toInt()}°$tempUnit  ·  " +
                        "H ${weather.main.tempMax.toInt()}° / L ${weather.main.tempMin.toInt()}°",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WeatherDetailCard(
            label = "Humidity",
            value = "${weather.main.humidity}%",
            modifier = Modifier.weight(1f)
        )
        WeatherDetailCard(
            label = "Wind",
            value = "${weather.wind.speed} $speedUnit",
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Pull down to refresh",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun WeatherDetailCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun SettingsScreen(viewModel: WeatherViewModel) {
    var cityInput by remember { mutableStateOf(viewModel.city) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        // City input
        OutlinedTextField(
            value = cityInput,
            onValueChange = { cityInput = it },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            viewModel.city = cityInput
            viewModel.fetchWeather()
        }) {
            Text("Apply City")
        }

        HorizontalDivider()

        // Units toggle
        Text("Temperature Units", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = viewModel.units == "metric",
                onClick = { viewModel.units = "metric" }
            )
            Text("Celsius")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = viewModel.units == "imperial",
                onClick = { viewModel.units = "imperial" }
            )
            Text("Fahrenheit")
        }
    }
}