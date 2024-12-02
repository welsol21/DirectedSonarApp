package com.example.directedsonarapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context)
    val dao = db.measurementDao()
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(dao))

    var note by remember { mutableStateOf("") }
    var isMeasuring by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Request microphone permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Microphone permission is required", Toast.LENGTH_SHORT).show()
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }

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

                // Start measurement button with rounded corners, dark purple background, and contrasting text
                Button(
                    onClick = {
                        isMeasuring = true
                        viewModel.startMeasurement(context, note) { success, messageText ->
                            isMeasuring = false
                            message = messageText
                            Toast.makeText(context, messageText, Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = !isMeasuring,
                    modifier = Modifier
                        .fillMaxWidth(0.65f)  // 65% width (15% wider than 50%)
                        .height(56.dp)  // Make it taller for a more circular appearance
                        .clip(RoundedCornerShape(30.dp))  // Round the corners more for circular look
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE)) // Dark purple background
                ) {
                    Text(
                        text = if (isMeasuring) "Measuring..." else "Start Measurement",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = Color.White // Make text white for contrast
                    )
                }

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
