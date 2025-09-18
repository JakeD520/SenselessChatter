package com.rivvr.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rivvr.app.di.ServiceGraph
import kotlinx.coroutines.launch

// Data models for room occupants
data class RoomOccupant(
    val id: String,
    val alias: String,
    val isCurrentUser: Boolean = false,
    val isMuted: Boolean = false,
    val roomType: RoomType
)

enum class RoomType {
    MAIN_ROOM, PRIVATE_ROOM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSignOut: () -> Unit = {}
) {
    var currentAlias by remember { mutableStateOf("Anonymous") }
    var currentRoom by remember { mutableStateOf<String?>(null) }
    var mainRoomOccupants by remember { mutableStateOf<List<RoomOccupant>>(emptyList()) }
    var privateRoomOccupants by remember { mutableStateOf<List<RoomOccupant>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditAliasDialog by remember { mutableStateOf(false) }
    var newAlias by remember { mutableStateOf("") }
    var currentUserId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Load room data and user profile on composition
    LaunchedEffect(Unit) {
        try {
            // Load current user profile and alias
            val profile = ServiceGraph.auth.currentProfile()
            if (profile != null) {
                currentUserId = profile.id
                currentAlias = profile.displayName ?: "Anonymous"
            }
            
            // Load current room
            val room = ServiceGraph.rooms.getCurrentRoom()
            currentRoom = room?.name
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    // Pure Realtime presence tracking - no database dependency
    LaunchedEffect(currentRoom) {
        if (currentRoom != null) {
            val room = ServiceGraph.rooms.getCurrentRoom()
            if (room != null) {
                println("🔄 DashboardScreen: Starting PURE Realtime presence tracking for room ${room.id}")
                // ONLY use Realtime presence - no database calls
                ServiceGraph.rooms.getRealTimeOccupants(room.id).collect { occupants ->
                    println("🔄 DashboardScreen: Got ${occupants.size} Realtime occupants: ${occupants.map { it.alias }}")
                    
                    mainRoomOccupants = occupants.map { occupant ->
                        RoomOccupant(
                            id = occupant.userId,
                            alias = occupant.alias,
                            isCurrentUser = occupant.userId == currentUserId,
                            isMuted = false,
                            roomType = RoomType.MAIN_ROOM
                        )
                    }
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header with current alias, room status, and actions
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Alias",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentAlias,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Room Active",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentRoom ?: "Not in a room",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (currentRoom != null) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row {
                        IconButton(
                            onClick = {
                                newAlias = currentAlias
                                showEditAliasDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change Alias"
                            )
                        }
                        IconButton(onClick = onSignOut) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Room occupants with infinite scroll
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = rememberLazyListState()
            ) {
            // Main Room Section
            if (mainRoomOccupants.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Main Room (${mainRoomOccupants.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(
                    items = mainRoomOccupants,
                    key = { occupant -> "${occupant.roomType.name}_${occupant.id}" }
                ) { occupant ->
                    OccupantCard(
                        occupant = occupant,
                        onMute = { 
                            // TODO: Implement mute/unmute functionality
                        },
                        onRequestPrivateChat = { 
                            // TODO: Implement private chat request
                        },
                        onInviteToPrivateRoom = { 
                            // TODO: Implement room invitation
                        }
                    )
                }
            }
            
            // Private Room Section
            if (privateRoomOccupants.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Private Room (${privateRoomOccupants.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(
                    items = privateRoomOccupants,
                    key = { occupant -> "${occupant.roomType.name}_${occupant.id}" }
                ) { occupant ->
                    OccupantCard(
                        occupant = occupant,
                        onMute = { 
                            // TODO: Implement mute/unmute functionality
                        },
                        onRequestPrivateChat = { 
                            // TODO: Implement private chat request
                        },
                        onInviteToPrivateRoom = { 
                            // TODO: Implement room invitation
                        }
                    )
                }
            }
            
            // Empty state
            if (mainRoomOccupants.isEmpty() && privateRoomOccupants.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Room Occupants",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Join a room to see other users here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        }
    }
    
    // Edit Alias Dialog
    if (showEditAliasDialog) {
        AlertDialog(
            onDismissRequest = { showEditAliasDialog = false },
            title = { Text("Change Alias") },
            text = {
                Column {
                    Text(
                        text = "Enter your new alias:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newAlias,
                        onValueChange = { newAlias = it },
                        label = { Text("Alias") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newAlias.isNotBlank()) {
                            scope.launch {
                                try {
                                    // Update profile in database
                                    ServiceGraph.auth.updateProfile(displayName = newAlias.trim())
                                    // Update local state
                                    currentAlias = newAlias.trim()
                                    showEditAliasDialog = false
                                } catch (e: Exception) {
                                    // Handle error - for now just close dialog
                                    showEditAliasDialog = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditAliasDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun OccupantCard(
    occupant: RoomOccupant,
    onMute: () -> Unit,
    onRequestPrivateChat: () -> Unit,
    onInviteToPrivateRoom: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User info with mute indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = occupant.alias,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (occupant.isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                    color = if (occupant.isCurrentUser) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                if (occupant.isCurrentUser) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(You)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (occupant.isMuted) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Muted",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Action buttons (only show for other users)
            if (!occupant.isCurrentUser) {
                Row {
                    // Mute/Unmute button
                    IconButton(onClick = onMute) {
                        Icon(
                            imageVector = if (occupant.isMuted) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = if (occupant.isMuted) "Unmute" else "Mute",
                            tint = if (occupant.isMuted) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Private chat request
                    IconButton(onClick = onRequestPrivateChat) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Request Private Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Invite to private room
                    IconButton(onClick = onInviteToPrivateRoom) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Invite to Private Room",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
