package com.rivvr.data.conventional

import kotlinx.serialization.Serializable

// Simple data classes matching database schema exactly
@Serializable
data class Message(
    val id: String,
    val user_id: String,
    val room_id: String, // Keep as String - will work with both TEXT and BIGINT
    val content: String,
    val created_at: String
)

@Serializable  
data class Profile(
    val id: String,
    val display_name: String,
    val created_at: String
)

@Serializable
data class Room(
    val id: String, // Keep as String - will work with both TEXT and BIGINT  
    val name: String,
    val soft_cap: Int,
    val hard_cap: Int,
    val created_at: String
)