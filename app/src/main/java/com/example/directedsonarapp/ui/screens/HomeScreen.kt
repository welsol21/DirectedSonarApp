package com.example.directedsonarapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.directedsonarapp.viewmodel.HomeViewModel
import com.example.directedsonarapp.viewmodel.HomeViewModelFactory
import com.example.directedsonarapp.data.database.DatabaseProvider
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context)
    val dao = db.measurementDao()
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(dao))

    var note by remember { mutableStateOf("") }
    var isMeasuring by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Input field for note
        BasicTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(56.dp)
                .border(1.dp, Color.Gray),
            textStyle = TextStyle(color = Color.Black)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Start measurement button
        Button(
            onClick = {
                isMeasuring = true
                viewModel.startMeasurement(note) { success, message ->
                    isMeasuring = false
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            },
            enabled = !isMeasuring,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Start Measurement")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress indicator
        if (isMeasuring) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
