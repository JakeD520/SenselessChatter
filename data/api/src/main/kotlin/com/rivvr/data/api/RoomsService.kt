package com.rivvr.data.api

import kotlinx.coroutines.flow.Flow

data class Room(
    val id: Long,
    val seq: Int,
    val name: String,
    val userCount: Int,
    val softCap: Int,
    val hardCap: Int,
    val isEndOfFlow: Boolean = false,
    val isNewRoom: Boolean = false,
    val message: String? = null
)

data class RoomNavigationResult(
    val room: Room?,
    val isEndOfFlow: Boolean,
    val message: String?
)

interface RoomsService {
    suspend fun getNextRoom(fromSeq: Int = 0): RoomNavigationResult
    suspend fun joinRoom(roomId: Long): Result<Room>
    suspend fun leaveRoom(roomId: Long): Result<Unit>
    suspend fun getCurrentRoom(): Room?
    suspend fun getRoomMessages(roomId: Long): Flow<List<Message>>
    suspend fun sendMessage(roomId: Long, text: String): Result<Unit>
}

data class Message(
    val id: Long,
    val roomId: Long,
    val senderUserId: String,
    val text: String,
    val createdAt: String
)
