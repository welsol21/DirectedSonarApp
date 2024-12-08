package com.example.directedsonarapp.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun SetupNavGraph(navController: NavHostController, paddingValues: PaddingValues) {

    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.History.route) {
            HistoryScreen(context)
        }
        composable(Screen.Graph.route) {
            GraphScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}

