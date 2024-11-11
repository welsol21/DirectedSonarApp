package com.example.directedsonarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.directedsonarapp.ui.screens.BottomNavigationBar
import com.example.directedsonarapp.ui.screens.SetupNavGraph
import com.example.directedsonarapp.ui.theme.DirectedSonarAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DirectedSonarAppTheme {
                MainScreen()
            }
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
