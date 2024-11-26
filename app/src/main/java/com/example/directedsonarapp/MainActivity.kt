package com.example.directedsonarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

//    private fun populateDatabase() {
//        val db = DatabaseProvider.getDatabase(this)
//        val dao = db.measurementDao()
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            dao.insert(Measurement(distance = 1.5, timestamp = System.currentTimeMillis(), note = "Test Note 1"))
//            dao.insert(Measurement(distance = 0.9, timestamp = System.currentTimeMillis(), note = "Test Note 2"))
//        }
//    }
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
