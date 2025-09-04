package com.rivvr.core.models

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val email: String? = null,
    val alias: String? = null
)

@Serializable
data class FlowRoom(
    val id: String,
    val user_id: String,
    val name: String? = null,
    val created_at: String
)

@Serializable
data class Ripple(
    val id: String,
    val flow_id: String,
    val author_id: String,
    val body: String,
    val created_at: String
)
