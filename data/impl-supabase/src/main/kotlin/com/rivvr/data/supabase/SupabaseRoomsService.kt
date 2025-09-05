package com.rivvr.data.supabase

import com.rivvr.data.api.RoomsService
import com.rivvr.data.api.Room
import com.rivvr.data.api.RoomNavigationResult
import com.rivvr.data.api.Message
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

@Serializable
data class RippleRow(
    val id: Long,
    val room_id: Long,
    val sender_user_id: String,
    val text: String?,
    val created_at: String
)

class SupabaseRoomsService(private val client: SupabaseClient) : RoomsService {
    
    // Enhanced quirky room names for dynamic rotation - keeping these for fallback
    private val quirkySuffixes = listOf(
        "Mysterious", "Whimsical", "Enchanted", "Peculiar", "Delightful",
        "Bewildering", "Marvelous", "Curious", "Fantastic", "Amusing",
        "Extraordinary", "Captivating", "Intriguing"
    )
    
    private val quirkyPrefixes = listOf(
        "Crystal", "Velvet", "Golden", "Silver", "Cosmic", "Ancient",
        "Secret", "Hidden", "Floating", "Shimmering", "Glowing"
    )
    
    private val quirkyNouns = listOf(
        "Chamber", "Sanctuary", "Haven", "Alcove", "Quarters", "Retreat",
        "Parlor", "Study", "Lounge", "Gallery", "Observatory", "Atrium"
    )

    private fun generateQuirkyRoomName(): String {
        val prefix = quirkyPrefixes.random()
        val suffix = quirkySuffixes.random()
        val noun = quirkyNouns.random()
        return "$prefix $suffix $noun"
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
                name = rpcResult.room_name ?: generateQuirkyRoomName(),
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
            // Fallback to enhanced simulation if RPC fails
            RoomNavigationResult(
                room = createRealStyleRoom(fromSeq + 1),
                isEndOfFlow = false,
                message = "RPC Error: ${e.message}"
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
                    name = rpcResult.room_name ?: generateQuirkyRoomName(),
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
                name = rpcResult.room_name ?: generateQuirkyRoomName(),
                userCount = rpcResult.user_count ?: 0,
                softCap = rpcResult.soft_cap ?: 8,
                hardCap = rpcResult.hard_cap ?: 12
            )
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getRoomMessages(roomId: Long): Flow<List<Message>> = flow {
        // Enhanced simulation with dynamic messages
        val sampleMessages = listOf(
            Message(1, roomId, "user1", "Welcome to this ${quirkySuffixes.random().lowercase()} space!", "2024-01-01T10:00:00Z"),
            Message(2, roomId, "user2", "The atmosphere here is quite ${quirkySuffixes.random().lowercase()}!", "2024-01-01T10:01:00Z"),
            Message(3, roomId, "user3", "I love the ${quirkyNouns.random().lowercase()} vibe of this room", "2024-01-01T10:02:00Z")
        )
        
        while (true) {
            emit(sampleMessages.shuffled().take((1..3).random()))
            delay(5000) // Update every 5 seconds
        }
    }
    
    override suspend fun sendMessage(roomId: Long, text: String): Result<Unit> {
        return try {
            client.from("ripples").insert(
                mapOf(
                    "room_id" to roomId,
                    "text" to text
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createRealStyleRoom(id: Int): Room {
        return Room(
            id = id.toLong(),
            seq = id,
            name = generateQuirkyRoomName(),
            userCount = (1..8).random(),
            softCap = 8,
            hardCap = 12,
            isEndOfFlow = false,
            isNewRoom = true,
            message = null
        )
    }
}
