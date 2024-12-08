package com.example.directedsonarapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.directedsonarapp.viewmodel.SettingsViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    // Observing ViewModel state
    val signalCount by viewModel.signalCount.observeAsState(1)
    val signalDuration by viewModel.signalDuration.observeAsState(3)
    val sampleRate by viewModel.sampleRate.observeAsState(48000)
    val frequency by viewModel.frequency.observeAsState(440)

    // Scroll state for vertical scrolling
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // Enable vertical scrolling
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Settings",
            style = MaterialTheme.typography.h4.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = MaterialTheme.colors.primary
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Signal Count
            SettingCard(
                title = "Number of signals",
                value = "$signalCount"
            ) {
                Slider(
                    value = signalCount.toFloat(),
                    onValueChange = { viewModel.setSignalCount(it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colors.primary,
                        activeTrackColor = MaterialTheme.colors.primary
                    )
                )
            }

            // Signal Duration
            SettingCard(
                title = "Signal duration",
                value = "$signalDuration seconds"
            ) {
                Slider(
                    value = signalDuration.toFloat(),
                    onValueChange = { viewModel.setSignalDuration(it.toInt()) },
                    valueRange = 1f..60f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colors.primary,
                        activeTrackColor = MaterialTheme.colors.primary
                    )
                )
            }

            // Sample Rate
            SettingCard(
                title = "Sample rate",
                value = "$sampleRate Hz"
            ) {
                SampleRateDropdown(
                    currentRate = sampleRate,
                    onRateSelected = { viewModel.setSampleRate(it) },
                    dropdownColor = MaterialTheme.colors.primary
                )
            }

            // Frequency
            SettingCard(
                title = "Frequency",
                value = "$frequency Hz"
            ) {
                Slider(
                    value = frequency.toFloat(),
                    onValueChange = { viewModel.setFrequency(it.toInt()) },
                    valueRange = 300f..3400f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colors.primary,
                        activeTrackColor = MaterialTheme.colors.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingCard(title: String, value: String, content: @Composable () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.subtitle1)
            Text(text = value, style = MaterialTheme.typography.body2, color = Color.Gray)
            content()
        }
    }
}

@Composable
fun SampleRateDropdown(currentRate: Int, onRateSelected: (Int) -> Unit, dropdownColor: Color) {
    var expanded by remember { mutableStateOf(false) }
    val rates = listOf(8000, 16000, 22050, 44100, 48000)

    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(contentColor = dropdownColor)
        ) {
            Text("$currentRate Hz")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            rates.forEach { rate ->
                DropdownMenuItem(onClick = {
                    onRateSelected(rate)
                    expanded = false
                }) {
                    Text("$rate Hz")
                }
            }
        }
    }
}
