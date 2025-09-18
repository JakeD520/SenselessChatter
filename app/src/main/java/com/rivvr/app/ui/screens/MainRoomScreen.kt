package com.rivvr.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.rivvr.app.di.ServiceGraph
import com.rivvr.data.api.Room
import com.rivvr.data.api.Message
import com.rivvr.app.ui.macros.processMessageMacros
import com.rivvr.app.ui.macros.effects.ChatEffectsOverlay
import com.rivvr.app.ui.macros.effects.rememberChatEffectsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRoomScreen(
    onSignOut: () -> Unit = {}
) {
    var currentRoom by remember { mutableStateOf<Room?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isEndOfFlow by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var isSendingMessage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Macro effects state
    val effectsState = rememberChatEffectsState()
    
    // Track which messages have already triggered effects to prevent re-triggering
    val processedMessageIds = remember { mutableSetOf<Long>() }
    
    // Track initial message count to only trigger effects for new messages
    var initialMessageCount by remember { mutableStateOf<Int?>(null) }
    
    // Auto-find and join a room when screen loads
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                println("🚀 MainRoomScreen: Starting auto-room-join flow...")
                
                // With pure Realtime architecture, we don't track current room in DB
                // Always try to get the first available room
                val result = ServiceGraph.rooms.getNextRoom(0)
                println("🏠 MainRoomScreen: getNextRoom(0) result: room=${result.room}, isEndOfFlow=${result.isEndOfFlow}, message=${result.message}")
                
                if (result.room != null) {
                    // Join the room
                    println("🚪 MainRoomScreen: Attempting to join room ${result.room!!.id} (${result.room!!.name})")
                    val joinResult = ServiceGraph.rooms.joinRoom(result.room!!.id)
                    if (joinResult.isSuccess) {
                        val room = joinResult.getOrNull()
                        println("✅ MainRoomScreen: Successfully joined room: $room")
                        currentRoom = room
                    } else {
                        println("❌ MainRoomScreen: Failed to join room: ${joinResult.exceptionOrNull()?.message}")
                        errorMessage = "Failed to join room: ${joinResult.exceptionOrNull()?.message}"
                    }
                } else {
                    println("⚠️ MainRoomScreen: No room available, end of flow: ${result.isEndOfFlow}")
                    isEndOfFlow = true
                    errorMessage = result.message
                }
                
                isLoading = false
            } catch (e: Exception) {
                println("❌ MainRoomScreen: Exception during auto-join: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error loading room: ${e.message}"
                isLoading = false
            }
        }
    }
    
    // Real-time message collection - no manual refresh needed!
    LaunchedEffect(currentRoom) {
        println("� LaunchedEffect triggered for real-time messages")
        currentRoom?.let { room ->
            println("🏠 MainRoomScreen currentRoom state: $room")
            println("� Starting real-time message subscription for room ${room.id}")
            try {
                ServiceGraph.rooms.getRoomMessages(room.id).collect { messageList ->
                    println("📨 Real-time UI received ${messageList.size} messages")
                    // Update UI state
                    messages = messageList
                    
                    // Clean up processed message IDs to prevent memory growth
                    // Keep only IDs of current messages
                    val currentMessageIds = messageList.map { it.id }.toSet()
                    processedMessageIds.retainAll(currentMessageIds)
                    println("🧹 Cleaned processedMessageIds, now contains ${processedMessageIds.size} entries")
                }
            } catch (e: Exception) {
                println("❌ Real-time message collection error: ${e.message}")
                e.printStackTrace()
            }
        } ?: run {
            println("⚠️ No current room available for message collection")
        }
    }
    
    // Wrap everything in Box so overlay can sit on top
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
        
        // Main content area
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Finding you a room...")
            }
        } else if (errorMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        if (isEndOfFlow) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        isEndOfFlow = false
                                        // Try to get first room again
                                        val result = ServiceGraph.rooms.getNextRoom(0)
                                        if (result.room != null) {
                                            val joinResult = ServiceGraph.rooms.joinRoom(result.room!!.id)
                                            if (joinResult.isSuccess) {
                                                currentRoom = joinResult.getOrNull()
                                            }
                                        }
                                        isLoading = false
                                    }
                                }
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        } else {
            // Show current room info 
            currentRoom?.let { room ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Room info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Welcome to",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = room.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "👥 ${room.userCount}/${room.softCap}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "🎯 Room #${room.seq}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    
                    // Navigation instructions
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Swipe Navigation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Previous room")
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Next room")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                    
                    // Chat area with real messages - wrapped in Box for effects overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Messages list
                                val listState = rememberLazyListState()
                                
                                // Auto-scroll to bottom when new messages arrive
                                LaunchedEffect(messages.size) {
                                    if (messages.isNotEmpty()) {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                                
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                if (messages.isEmpty()) {
                                    item {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "💬 No messages yet",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Be the first to say something!",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    items(messages) { message ->
                                        // Process macros when displaying message (only once per message)
                                        LaunchedEffect(message.id, message.text) {
                                            // Only process if this exact message hasn't been processed
                                            val messageKey = "${message.id}_${message.text.hashCode()}"
                                            if (!processedMessageIds.contains(message.id)) {
                                                processedMessageIds.add(message.id)
                                                val processedMessage = processMessageMacros(message.text)
                                                if (processedMessage.shouldTriggerEffects) {
                                                    // Only trigger effects if this is a recent message (prevent old message effects on room join)
                                                    val isRecentMessage = messages.indexOf(message) >= messages.size - 3 // Only last 3 messages
                                                    if (isRecentMessage) {
                                                        effectsState.triggerEffects(processedMessage.effects)
                                                    }
                                                }
                                            }
                                        }
                                        
                                        // Compact inline message format with emoji replacement
                                        val processedMessage = processMessageMacros(message.text)
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(style = SpanStyle(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )) {
                                                    append("<${message.senderAlias}>")
                                                }
                                                append(" : ${processedMessage.displayText}")
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            
                            // Message input
                            Divider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = messageText,
                                    onValueChange = { 
                                        if (it.length <= 200) { // Enforce 200 char limit in UI
                                            messageText = it 
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Type a message...") },
                                    maxLines = 3,
                                    supportingText = {
                                        Text("${messageText.length}/200")
                                    },
                                    enabled = !isSendingMessage
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                IconButton(
                                    onClick = {
                                        if (messageText.trim().isNotEmpty() && !isSendingMessage) {
                                            scope.launch {
                                                isSendingMessage = true
                                                
                                                currentRoom?.let { room ->
                                                    try {
                                                        val user = ServiceGraph.auth.currentProfile()
                                                        if (user != null) {
                                                            val result = ServiceGraph.rooms.sendMessage(room.id, messageText.trim())
                                                            
                                                            if (result.isSuccess) {
                                                                messageText = ""
                                                                // No manual refresh needed - real-time subscription will handle it!
                                                                println("✅ Message sent successfully, real-time subscription will update UI")
                                                            } else {
                                                                println("Failed to send message: ${result.exceptionOrNull()?.message}")
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        println("Error sending message: ${e.message}")
                                                    }
                                                }
                                                
                                                isSendingMessage = false
                                            }
                                        }
                                    },
                                    enabled = messageText.trim().isNotEmpty() && !isSendingMessage
                                ) {
                                    if (isSendingMessage) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Send,
                                            contentDescription = "Send message"
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Effects overlay for macro animations (love hearts, etc.) - positioned over chat area
                    ChatEffectsOverlay(
                        activeEffects = effectsState.activeEffects,
                        modifier = Modifier.fillMaxSize(),
                        onEffectComplete = { effect ->
                            effectsState.completeEffect(effect)
                        }
                    )
                } // End Box for chat area
            }
        } // End Column for room content
        } // End currentRoom?.let
        } // End Column
    } // End Box
}
