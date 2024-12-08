package com.example.directedsonarapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.alpha
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
    var messageText by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Request microphone permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                scope.launch {
                    messageText = "Microphone permission is required"
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // App Title
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
                // Box for Progress Bar or Message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp), // Reserved height for progress bar or message
                    contentAlignment = Alignment.Center
                ) {
                    if (isMeasuring) {
                        CircularCountdownTimer(
                            durationInSeconds = 3,
                            remainingTime = progress
                        )
                    } else if (messageText.isNotEmpty()) {
                        AnimatedMessage(
                            message = messageText,
                            visible = true,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
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
                    AnimatedButton(
                        onClick = {
                            isMeasuring = true
                            progress = 3 // Initial countdown duration

//                            note =

                            viewModel.startMeasurement(
                                context = context,
                                note = note,
                                duration = 3,
                                onProgressUpdate = { remainingTime ->
                                    progress = remainingTime
                                },
                                onComplete = { success, message ->
                                    isMeasuring = false
                                    messageText = message
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
            color = Color(0xFF6200EE),
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = "$remainingTime",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
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

@Composable
fun AnimatedMessage(
    message: String,
    modifier: Modifier = Modifier,
    visible: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 300)
    )

    if (visible) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .scale(scale)
                .alpha(alpha)
                .padding(8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF3700B3))
                .padding(16.dp)
        ) {
            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.body1.copy(
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

