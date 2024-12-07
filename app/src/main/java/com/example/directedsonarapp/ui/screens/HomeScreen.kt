package com.example.directedsonarapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context)
    val dao = db.measurementDao()
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(dao))

    var note by remember { mutableStateOf("") }
    var isMeasuring by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Request microphone permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                scope.launch {
                    snackbarHostState.showSnackbar("Microphone permission is required")
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { Spacer(modifier = Modifier.height(56.dp)) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // App Title and SnackbarHost
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Input field for note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Enter note (optional)") },
                    placeholder = { Text("E.g., Living room") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { /* Handle action */ }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Start measurement button
//                Button(
//                    onClick = {
//                        isMeasuring = true
//                        viewModel.startMeasurement(context, note) { success, messageText ->
//                            isMeasuring = false
//                            scope.launch {
//                                snackbarHostState.showSnackbar(messageText)
//                            }
//                        }
//                    },
//                    enabled = !isMeasuring,
//                    modifier = Modifier
//                        .fillMaxWidth(0.65f)
//                        .height(56.dp)
//                        .clip(RoundedCornerShape(30.dp))
//                        .padding(8.dp),
//                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
//                ) {
//                    Text(
//                        text = if (isMeasuring) "Measuring..." else "Start Measurement",
//                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
//                        color = Color.White
//                    )
//                }

                AnimatedButton(
                    onClick = {
                        isMeasuring = true
                        viewModel.startMeasurement(context, note) { success, messageText ->
                            isMeasuring = false
                            scope.launch {
                                snackbarHostState.showSnackbar(messageText)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(56.dp),
                    enabled = !isMeasuring,
                    text = if (isMeasuring) "Measuring..." else "Start Measurement"
                )


                Spacer(modifier = Modifier.height(32.dp))

                // Progress indicator
                if (isMeasuring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colors.primary
                    )
                }
            }

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

    // Кнопка с анимацией
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (enabled) Color(0xFF3700B3) else Color(0xFF6200EE), // Более темный фон
            contentColor = Color.White // Белый текст для контраста
        ),
        shape = RoundedCornerShape(30.dp) // Оставляем скругленные углы
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.button.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )
    }
}
