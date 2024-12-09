package com.example.directedsonarapp.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.directedsonarapp.viewmodel.HomeViewModel
import com.example.directedsonarapp.viewmodel.HomeViewModelFactory
import com.example.directedsonarapp.data.database.DatabaseProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import android.app.Application
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val db = DatabaseProvider.getDatabase(context)
    val dao = db.measurementDao()

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(application, dao)
    )

    // Use `rememberSaveable` to retain state during configuration changes
    var note by rememberSaveable { mutableStateOf("") }
    var isMeasuring by rememberSaveable { mutableStateOf(false) }
    var progress by rememberSaveable { mutableStateOf(0) }
    var messages by rememberSaveable { mutableStateOf("") } // Store messages as a single string
    val totalDuration = viewModel.signalCount * viewModel.signalDuration
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { Spacer(modifier = Modifier.height(56.dp)) }
    ) { innerPadding ->
        // Add LazyColumn for scrolling
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // App title
                Text(
                    text = "Directed Sonar App",
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
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Box for Progress Bar or Messages
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp), // Reserved height for progress bar or messages
                    contentAlignment = Alignment.Center
                ) {
                    if (isMeasuring) {
                        CircularCountdownTimer(
                            durationInSeconds = totalDuration,
                            remainingTime = progress
                        )
                    } else if (messages.isNotEmpty()) {
                        Text(
                            text = messages,
                            style = MaterialTheme.typography.body1.copy(
                                fontSize = 16.sp,
                                color = MaterialTheme.colors.onSurface
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Input field for Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Enter note (optional)") },
                    placeholder = { Text("E.g., Living room") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Start measurement button
                AnimatedButton(
                    onClick = {
                        isMeasuring = true
                        progress = totalDuration
                        messages = "" // Clear previous messages

                        viewModel.startMeasurement(
                            context = context,
                            note = note,
                            onProgressUpdate = { remainingTime ->
                                progress = remainingTime
                            },
                            onSignalComplete = { message ->
                                messages += "$message\n" // Append new message
                            },
                            onComplete = { success, message ->
                                isMeasuring = false
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !isMeasuring,
                    text = if (isMeasuring) "Measuring..." else "Start Measurement",
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(56.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Footer text
                Text(
                    text = "Measure distances with sound waves",
                    style = MaterialTheme.typography.body2.copy(
                        fontSize = 14.sp,
                        color = Color.Gray
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun CircularCountdownTimer(
    durationInSeconds: Int,
    remainingTime: Int
) {
    val progress = remainingTime.toFloat() / durationInSeconds.toFloat()

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
        CircularProgressIndicator(
            progress = progress,
            strokeWidth = 4.dp,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = "$remainingTime",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary
        )
    }
}

@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1.1f else 1f,
        animationSpec = tween(durationMillis = 300)
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (enabled) Color(0xFF3700B3) else Color(0xFF6200EE),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(30.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.button.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )
    }
}
