package com.rivvr.data.conventional

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// ChatRepository.kt - Standard CRUD operations (following consultant report exactly)
class ConventionalChatRepository(private val client: SupabaseClient) {
    
    private val _messagesRefresh = MutableSharedFlow<List<Message>>()
    val messagesRefresh: Flow<List<Message>> = _messagesRefresh.asSharedFlow()
    
    // Send message - direct insert, then refresh for immediate feedback
    suspend fun sendMessage(content: String, roomId: String = "general") {
        client.from("messages").insert(
            mapOf(
                "content" to content,
                "room_id" to roomId,
                "user_id" to client.auth.currentUserOrNull()?.id
            )
        )
        
        // Immediate refresh for sender feedback (efficient - only after send)
        refreshMessages(roomId)
    }
    
    // Manual refresh - user-triggered (Pull-to-refresh pattern)
    suspend fun refreshMessages(roomId: String = "general") {
        val messages = getMessages(roomId)
        _messagesRefresh.emit(messages)
    }
    
    // Load messages - direct select (works with both room ID types)
    suspend fun getMessages(roomId: String = "general"): List<Message> {
        return client.from("messages")
            .select {
                filter {
                    eq("room_id", roomId)
                }
                order("created_at", Order.ASCENDING)
                limit(50)
            }
            .decodeList<Message>()
    }
    
    // TODO: Implement proper Supabase Real-time subscription
    // For now: load initial messages on subscribe
    suspend fun subscribeToMessages(roomId: String = "general") {
        refreshMessages(roomId)
    }
    
    // Get current user's profile
    suspend fun getCurrentUserProfile(): Profile? {
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        return client.from("profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingleOrNull<Profile>()
    }
    
    // Create or update user profile
    suspend fun upsertProfile(displayName: String) {
        val userId = client.auth.currentUserOrNull()?.id ?: return
        client.from("profiles").upsert(
            mapOf(
                "id" to userId,
                "display_name" to displayName
            )
        )
    }
}