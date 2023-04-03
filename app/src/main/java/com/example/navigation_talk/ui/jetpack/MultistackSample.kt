package com.example.navigation_talk.ui.jetpack

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


@Composable
fun MultistackNavigation() {
    val navController = rememberNavController()
    val items = BottomNavScreens.values
    Scaffold(
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    BottomNavigationItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.route) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreens.Summary.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavScreens.Summary.route) { Screen("summary") }
            composable(BottomNavScreens.Payments.route) { Screen("payments") }
            composable(BottomNavScreens.Catalog.route) { Screen("catalog") }
            composable(BottomNavScreens.Profile.route) { Screen("profile") }
        }
    }
}

@Composable
private fun Screen(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}

internal sealed class BottomNavScreens(
    val route: String,
    val icon: ImageVector
) {
    object Summary : BottomNavScreens("summary", Icons.Default.Home)
    object Payments : BottomNavScreens("payments", Icons.Default.List)
    object Catalog : BottomNavScreens("catalog", Icons.Default.ShoppingCart)
    object Profile : BottomNavScreens("profile", Icons.Default.AccountCircle)

    companion object {
        val values = listOf(Summary, Payments, Catalog, Profile)
    }
}