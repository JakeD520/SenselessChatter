package com.rivvr.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rivvr.app.ui.nav.NavDest
import com.rivvr.app.ui.screens.PrivateRoomScreen
import com.rivvr.app.ui.screens.MainRoomScreen
import com.rivvr.app.ui.screens.PrivateMessageScreen
import com.rivvr.app.ui.screens.DashboardScreen
import com.rivvr.app.ui.screens.AuthScreen
import com.rivvr.app.di.ServiceGraph
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RivvrApp() {
    var isAuthenticated by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Check if user is already authenticated
    LaunchedEffect(Unit) {
        try {
            // Test real Supabase auth with Ktor 3.2.2
            val currentUser = ServiceGraph.auth.currentProfile()
            isAuthenticated = currentUser != null
        } catch (e: Exception) {
            // If ServiceGraph fails, start unauthenticated
            isAuthenticated = false
        }
    }
    
    if (!isAuthenticated) {
        // Show login screen
        AuthScreen(
            onAuthed = { isAuthenticated = true },
            signIn = { email, password ->
                try {
                    ServiceGraph.auth.signIn(email, password)
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            signUp = { email, password ->
                try {
                    ServiceGraph.auth.signUp(email, password)
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        )
    } else {
        // Show main app with navigation
        val navController = rememberNavController()
        
        MaterialTheme {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        bottomNavItems.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
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
                    startDestination = NavDest.MainRoom.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(NavDest.PrivateRoom.route) {
                        PrivateRoomScreen()
                    }
                    composable(NavDest.MainRoom.route) {
                        MainRoomScreen(
                            onSignOut = { 
                                scope.launch {
                                    try {
                                        ServiceGraph.auth.signOut()
                                        isAuthenticated = false
                                    } catch (e: Exception) {
                                        // Handle sign out error, but still set to unauthenticated
                                        isAuthenticated = false
                                    }
                                }
                            }
                        )
                    }
                    composable(NavDest.PrivateMessage.route) {
                        PrivateMessageScreen()
                    }
                    composable(NavDest.Dashboard.route) {
                        DashboardScreen(
                            onSignOut = { 
                                scope.launch {
                                    try {
                                        ServiceGraph.auth.signOut()
                                        isAuthenticated = false
                                    } catch (e: Exception) {
                                        // Handle sign out error, but still set to unauthenticated
                                        isAuthenticated = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = NavDest.PrivateRoom.route,
        label = "Private Room",
        icon = Icons.Default.Lock
    ),
    BottomNavItem(
        route = NavDest.MainRoom.route,
        label = "Main Room", 
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        route = NavDest.PrivateMessage.route,
        label = "Private Chat",
        icon = Icons.Default.Email
    ),
    BottomNavItem(
        route = NavDest.Dashboard.route,
        label = "Dashboard",
        icon = Icons.Default.Settings
    )
)
