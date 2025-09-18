package com.rivvr.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rivvr.app.di.ServiceGraph
import kotlinx.coroutines.launch

// Define a simple data class here to avoid import conflicts
data class FlowRoomData(
    val id: String,
    val user_id: String,
    val name: String? = null,
    val created_at: String
)

@Composable
fun FlowsScreen(onSignOut: () -> Unit = {}) {
    var flowRooms by remember { mutableStateOf<List<FlowRoomData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Load real data from Supabase with Ktor 3.2.2
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                error = null
                
                val currentProfile = ServiceGraph.auth.currentProfile()
                if (currentProfile != null) {
                    val supabaseFlowRooms = ServiceGraph.flows.myRooms(currentProfile.id, limit = 50)
                    flowRooms = supabaseFlowRooms.map { room ->
                        FlowRoomData(
                            id = room.flow.id.toString(),
                            user_id = room.flow.createdByUserId,
                            name = room.flow.name,
                            created_at = room.flow.createdAtEpochMs.toString()
                        )
                    }
                } else {
                    error = "No user profile found"
                }
                
            } catch (e: Exception) {
                error = e.message ?: "Failed to load flows"
                // Fallback to mock data if Supabase fails
                flowRooms = listOf(
                    FlowRoomData(
                        id = "fallback1",
                        user_id = "user1",
                        name = "Fallback Flow (Error: ${e.message})",
                        created_at = "2025-09-04T16:00:00Z"
                    )
                )
            } finally {
                isLoading = false
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Header with sign out
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Flows", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onSignOut) {
                Text("Sign Out")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        scope.launch {
                            try {
                                isLoading = true
                                error = null
                                val currentProfile = ServiceGraph.auth.currentProfile()
                                if (currentProfile != null) {
                                    val supabaseFlowRooms = ServiceGraph.flows.myRooms(currentProfile.id, limit = 50)
                                    flowRooms = supabaseFlowRooms.map { room ->
                                        FlowRoomData(
                                            id = room.flow.id.toString(),
                                            user_id = room.flow.createdByUserId,
                                            name = room.flow.name,
                                            created_at = room.flow.createdAtEpochMs.toString()
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                error = e.message ?: "Failed to load flows"
                            } finally {
                                isLoading = false
                            }
                        }
                    }) {
                        Text("Retry")
                    }
                }
            }
            flowRooms.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No flows yet", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("Create your first flow to get started!", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(flowRooms) { flowRoom ->
                        FlowCard(flowRoom = flowRoom)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowCard(flowRoom: FlowRoomData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* TODO: Navigate to chat */ }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                flowRoom.name ?: "Unnamed Flow",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Creator: ${flowRoom.user_id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Created: ${flowRoom.created_at}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
