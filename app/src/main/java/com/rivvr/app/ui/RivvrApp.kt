package com.rivvr.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.jan.supabase.auth.auth
import com.rivvr.app.ui.nav.NavDest
import com.rivvr.app.ui.screens.PrivateRoomScreen
import com.rivvr.app.ui.screens.MainRoomScreen
import com.rivvr.app.ui.screens.PrivateMessageScreen
import com.rivvr.app.ui.screens.DashboardScreen
import com.rivvr.app.ui.screens.AuthScreen
import com.rivvr.app.ui.screens.ConventionalTestScreen
import com.rivvr.app.di.ServiceGraph
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RivvrApp() {
    var isAuthenticated by remember { mutableStateOf(false) }
    var isCheckingAuth by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    // Check if user is already authenticated on app startup
    LaunchedEffect(Unit) {
        try {
            println("🔐 === AUTHENTICATION CHECK STARTED ===")
            println("🔐 App starting up, checking for existing session...")
            
            // Wait for Supabase to fully initialize and load session
            println("🔐 Waiting for Supabase session restoration...")
            
            // Poll for session restoration with timeout
            var attempts = 0
            val maxAttempts = 20 // 2 seconds max wait
            var sessionRestored = false
            
            while (attempts < maxAttempts && !sessionRestored) {
                kotlinx.coroutines.delay(100)
                attempts++
                
                // Check if session restoration is complete by checking auth state
                try {
                    val hasSession = com.rivvr.app.data.SupabaseClientProvider.client.auth.currentSessionOrNull() != null
                    if (hasSession) {
                        sessionRestored = true
                        println("🔐 Session restoration detected after ${attempts * 100}ms")
                    }
                } catch (e: Exception) {
                    // Client might not be ready yet, continue waiting
                }
            }
            
            if (!sessionRestored) {
                println("🔐 No session found after ${attempts * 100}ms wait")
            }
            
            println("🔐 Checking authentication state...")
            
            // Test real Supabase auth with automatic session restore
            val currentUser = ServiceGraph.auth.currentProfile()
            isAuthenticated = currentUser != null
            
            if (currentUser != null) {
                println("✅ === USER AUTHENTICATED VIA STORED SESSION ===")
                println("✅ User email: ${currentUser.email}")
                println("✅ User ID: ${currentUser.id}")
                println("✅ Display name: ${currentUser.displayName}")
                
                // Clean up any stale room presence on app start
                try {
                    val currentRoom = ServiceGraph.rooms.getCurrentRoom()
                    if (currentRoom != null) {
                        // Re-join current room with Realtime presence (server-managed)
                        ServiceGraph.rooms.joinRoomWithPresence(currentRoom.id)
                        println("🏠 Rejoined room: ${currentRoom.name}")
                    } else {
                        println("🏠 No previous room found")
                    }
                } catch (e: Exception) {
                    println("⚠️ Room rejoin error (non-fatal): ${e.message}")
                }
            } else {
                println("❌ === NO STORED SESSION FOUND ===")
                println("❌ User will need to login")
            }
        } catch (e: Exception) {
            println("❌ === AUTHENTICATION ERROR ===")
            println("❌ Error: ${e.message}")
            println("❌ Stack trace: ${e.stackTraceToString()}")
            // If ServiceGraph fails, start unauthenticated
            isAuthenticated = false
        } finally {
            isCheckingAuth = false
            println("🔐 === AUTHENTICATION CHECK COMPLETE ===")
        }
    }
    
    if (isCheckingAuth) {
        // Show loading while checking authentication
        Surface(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Loading...")
                }
            }
        }
    } else if (!isAuthenticated) {
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
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    startDestination = "conventional", // Use conventional chat as default
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("conventional") {
                        ConventionalTestScreen()
                    }
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
        route = "conventional",
        label = "Conventional",
        icon = Icons.Default.Email // Using Email icon for chat
    ),
    BottomNavItem(
        route = NavDest.PrivateRoom.route,
        label = "Private Room",
        icon = Icons.Default.Lock
    ),
    BottomNavItem(
        route = NavDest.MainRoom.route,
        label = "Main Room (Old)", 
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        route = NavDest.PrivateMessage.route,
        label = "Private Chat",
        icon = Icons.Default.Settings // Changed to Settings to avoid duplicate
    ),
    BottomNavItem(
        route = NavDest.Dashboard.route,
        label = "Dashboard",
        icon = Icons.Default.Home // Changed to Home to avoid duplicate
    )
)
