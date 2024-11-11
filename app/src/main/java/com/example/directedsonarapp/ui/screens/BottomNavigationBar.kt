package com.example.directedsonarapp.ui.screens

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.directedsonarapp.R

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.History,
        Screen.Graph,
        Screen.Settings
    )

    BottomNavigation(
        modifier = Modifier.height(121.dp),
        backgroundColor = Color(0xFF6200EE),
        contentColor = Color.Unspecified
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        items.forEach { screen ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = when (screen) {
                            Screen.Home -> R.drawable.ic_home
                            Screen.History -> R.drawable.ic_history
                            Screen.Graph -> R.drawable.ic_graph
                            Screen.Settings -> R.drawable.ic_settings
                        }),
                        contentDescription = screen.route,
                        modifier = Modifier
                            .size(96.dp)
                            .padding(top = 8.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.route,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                selectedContentColor = Color.Unspecified,
                unselectedContentColor = Color.Unspecified
            )
        }
    }
}
