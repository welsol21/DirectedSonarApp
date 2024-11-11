package com.example.directedsonarapp.ui.screens

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object Graph : Screen("graph")
    object Settings : Screen("settings")
}