package com.example.directedsonarapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.directedsonarapp.data.database.DatabaseProvider
import com.example.directedsonarapp.data.database.Measurement
import com.example.directedsonarapp.ui.screens.BottomNavigationBar
import com.example.directedsonarapp.ui.screens.SetupNavGraph
import com.example.directedsonarapp.ui.theme.DirectedSonarAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted
                Toast.makeText(this, "Audio recording permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(this, "Permission for recording audio denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        // Initialize the database and populate with test data
        populateDatabase()

        setContent {
            DirectedSonarAppTheme {
                MainScreen()
            }
        }
    }

    private fun populateDatabase() {
        val db = DatabaseProvider.getDatabase(this)
        val dao = db.measurementDao()

        lifecycleScope.launch(Dispatchers.IO) {
            repeat(10) {
                val randomDistance = Random.nextDouble(0.9, 1.5)
                val randomTimestamp = System.currentTimeMillis() - (0..10_000_000L).random()
                val randomNote = "Test Note ${it + 1}"

                dao.insert(
                    Measurement(
                        distance = randomDistance,
                        timestamp = randomTimestamp,
                        note = randomNote
                    )
                )
            }
        }
    }

    @Composable
    fun MainScreen() {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController)
            }
        ) { paddingValues ->
            SetupNavGraph(navController = navController, paddingValues = paddingValues)
        }
    }

    private fun checkAndRequestPermissions() {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }
}
