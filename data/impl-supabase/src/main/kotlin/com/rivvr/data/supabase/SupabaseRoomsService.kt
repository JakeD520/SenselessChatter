package com.rivvr.data.supabase

import com.rivvr.data.api.RoomsService
import com.rivvr.data.api.Room
import com.rivvr.data.api.RoomNavigationResult
import com.rivvr.data.api.Message
import com.rivvr.data.api.RoomOccupant
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RoomRpcResponse(
    val room_id: Long? = null,
    val room_seq: Int? = null,
    val room_name: String? = null,
    val user_count: Int? = null,
    val soft_cap: Int? = null,
    val hard_cap: Int? = null,
    val end_of_flow: Boolean? = null,
    val new_room: Boolean? = null,
    val message: String? = null,
    val error: String? = null,
    val success: Boolean? = null
)

// STANDARD TABLE ROW REPRESENTATION - matches database schema exactly  
@Serializable
data class MessageRow(
    val id: Long,
    val room_id: String, // Database stores as string
    val user_id: String,
    val body: String,
    val created_at: String,
    val user_alias: String? = null // From JOIN with profiles or direct column
)

@Serializable
data class RippleRow(
    val id: Long,
    val room_id: Long,
    val sender_user_id: String,
    val text: String?,
    val created_at: String
)

// LIGHTWEIGHT CLIENT - Backend-First Architecture
// Client is thin presentation layer, server does all the work

class SupabaseRoomsService(private val client: SupabaseClient) : RoomsService {
    
    // Ultra-simple request/response data classes
    @Serializable
    data class SendMessageRequest(
        val room_id: String,
        val text: String  // Client sends raw text, server processes everything
    )
    
    @Serializable 
    data class ServerResponse(
        val success: Boolean,
        val error: String? = null,
        val message: ServerMessage? = null,
        val messages: List<ServerMessage>? = null,
        val room: ServerRoom? = null
    )
    
    @Serializable
    data class EffectInstruction(
        val type: String,
        val particle: String? = null,
        val particles: List<String> = emptyList(),
        val count: Int = 0,
        val duration: Int = 0,
        val animation: String? = null
    )
    
    @Serializable
    data class ServerMessage(
        val id: Long,
        val room_id: Long,
        val user_id: String,
        val user_alias: String,
        val body: String,
        val formatted_message: String? = null, // New field from server
        val original_text: String? = null,
        val effects: List<String> = emptyList(),
        val effect_instructions: List<EffectInstruction> = emptyList(),
        val created_at: String,
        val has_effects: Boolean = false
    )
    
    @Serializable
    data class ServerRoom(
        val id: Long,
        val name: String,
        val seq: Int? = null,
        val user_count: Int = 0,
        val soft_cap: Int = 8,
        val hard_cap: Int = 12
    )
    
    // CLEAN MESSAGE SENDING: Direct PostgREST insert
    override suspend fun sendMessage(roomId: Long, text: String): Result<Unit> {
        return try {
            val currentUser = client.auth.currentUserOrNull()
            val userId = currentUser?.id ?: "anonymous"
            
            client.postgrest.from("messages").insert(
                mapOf(
                    "room_id" to roomId.toString(),
                    "user_id" to userId,
                    "body" to text
                )
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // PRODUCTION MESSAGING: Clean direct database operations
    override suspend fun getRoomMessages(roomId: Long): Flow<List<Message>> = flow {
        while (true) {
            try {
                // Direct PostgREST query - no custom RPC functions
                val messages = client.postgrest.from("messages")
                    .select() {
                        filter {
                            eq("room_id", roomId)
                        }
                        order("created_at", Order.ASCENDING)
                        limit(50) // Last 50 messages
                    }
                    .decodeList<MessageRow>()
                    .map { row ->
                        Message(
                            id = row.id,
                            roomId = row.room_id.toLong(),
                            senderUserId = row.user_id,
                            senderAlias = row.user_alias ?: "Anonymous",
                            text = row.body,
                            createdAt = row.created_at
                        )
                    }
                
                emit(messages)
                delay(2000L) // Simple 2-second polling for now - can be optimized later
                
            } catch (e: Exception) {
                println("❌ Error loading messages: ${e.message}")
                emit(emptyList())
                delay(5000L)
            }
        }
    }
    
    override suspend fun getNextRoom(fromSeq: Int): RoomNavigationResult {
        return try {
            // Try accessing postgrest plugin directly and call RPC
            val postgrestPlugin = client.pluginManager.getPlugin(Postgrest)
            val response = postgrestPlugin.rpc(
                function = "rpc_next_room", 
                parameters = mapOf("p_from_seq" to fromSeq)
            )
            
            val rpcResult = Json.decodeFromString<RoomRpcResponse>(response.data)
            
            if (rpcResult.error != null) {
                return RoomNavigationResult(
                    room = null,
                    isEndOfFlow = true,
                    message = rpcResult.error
                )
            }
            
            if (rpcResult.end_of_flow == true) {
                return RoomNavigationResult(
                    room = null,
                    isEndOfFlow = true,
                    message = rpcResult.message ?: "🌊 You've reached calm waters!"
                )
            }
            
            val room = Room(
                id = rpcResult.room_id ?: 0,
                seq = rpcResult.room_seq ?: 0,
                name = rpcResult.room_name ?: "Unnamed Room",
                userCount = rpcResult.user_count ?: 0,
                softCap = rpcResult.soft_cap ?: 8,
                hardCap = rpcResult.hard_cap ?: 12,
                isNewRoom = rpcResult.new_room ?: false
            )
            
            RoomNavigationResult(
                room = room,
                isEndOfFlow = false,
                message = null
            )
        } catch (e: Exception) {
            println("❌ Error in getNextRoom RPC: ${e.message}")
            // Return error instead of fake fallback room
            RoomNavigationResult(
                room = null,
                isEndOfFlow = true,
                message = "Database error: ${e.message}"
            )
        }
    }
    
    override suspend fun joinRoom(roomId: Long): Result<Room> {
        return try {
            val postgrestPlugin = client.pluginManager.getPlugin(Postgrest)
            val response = postgrestPlugin.rpc(
                function = "rpc_join_room",
                parameters = mapOf("p_room_id" to roomId)
            )
            
            val rpcResult = Json.decodeFromString<RoomRpcResponse>(response.data)
            
            if (rpcResult.error != null) {
                return Result.failure(Exception(rpcResult.error))
            }
            
            if (rpcResult.success == true) {
                val room = Room(
                    id = roomId,
                    seq = rpcResult.room_seq ?: 0,
                    name = rpcResult.room_name ?: "Unnamed Room",
                    userCount = rpcResult.user_count ?: 1,
                    softCap = rpcResult.soft_cap ?: 8,
                    hardCap = rpcResult.hard_cap ?: 12,
                    isNewRoom = rpcResult.new_room ?: false
                )
                Result.success(room)
            } else {
                Result.failure(Exception("Failed to join room"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun leaveRoom(roomId: Long): Result<Unit> {
        return try {
            val postgrestPlugin = client.pluginManager.getPlugin(Postgrest)
            val response = postgrestPlugin.rpc(
                function = "rpc_leave_room",
                parameters = mapOf("p_room_id" to roomId)
            )
            
            val rpcResult = Json.decodeFromString<RoomRpcResponse>(response.data)
            
            if (rpcResult.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(rpcResult.error ?: "Failed to leave room"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentRoom(): Room? {
        return try {
            val postgrestPlugin = client.pluginManager.getPlugin(Postgrest)
            val response = postgrestPlugin.rpc("rpc_get_user_current_room")
            val rpcResult = Json.decodeFromString<RoomRpcResponse>(response.data)
            
            if (rpcResult.error != null) {
                return null
            }
            
            Room(
                id = rpcResult.room_id ?: 0,
                seq = rpcResult.room_seq ?: 0,
                name = rpcResult.room_name ?: "Unnamed Room",
                userCount = rpcResult.user_count ?: 0,
                softCap = rpcResult.soft_cap ?: 8,
                hardCap = rpcResult.hard_cap ?: 12
            )
        } catch (e: Exception) {
            null
        }
    }
    
    // Real presence methods - no mock data
    override suspend fun getRoomOccupants(roomId: Long): List<RoomOccupant> {
        // With pure Realtime architecture, we don't query database for presence
        // This would come from Realtime WebSocket channels in a real implementation
        return emptyList()
    }

    override fun getRealTimeOccupants(roomId: Long): Flow<List<RoomOccupant>> = flow {
        // This should be a real Realtime subscription to presence channels
        // For now, emit empty until Realtime presence is properly implemented
        emit(emptyList())
    }

    override suspend fun joinRoomWithPresence(roomId: Long): Result<Room> {
        return joinRoom(roomId)
    }

    override suspend fun leaveRoomWithPresence(roomId: Long): Result<Unit> {
        return leaveRoom(roomId)
    }
}