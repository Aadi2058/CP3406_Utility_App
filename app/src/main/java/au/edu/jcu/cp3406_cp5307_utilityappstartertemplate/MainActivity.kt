package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

@Composable
fun UtilityScreen(viewModel: WeatherViewModel) {

    // Fetch weather when screen first loads
    LaunchedEffect(Unit) {
        viewModel.fetchWeather()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewModel.isLoading) {
            CircularProgressIndicator()

        } else if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.fetchWeather() }) {
                Text("Retry")
            }

        } else {
            val weather = viewModel.weatherData
            if (weather != null) {
                Text(
                    text = weather.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${weather.main.temp.toInt()}°${if (viewModel.units == "metric") "C" else "F"}",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = weather.weather[0].description.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Humidity: ${weather.main.humidity}%",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { viewModel.fetchWeather() }) {
                    Text("Refresh")
                }
            }
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