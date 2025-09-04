package com.rivvr.core.models


data class Profile(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val createdAtEpochMs: Long? = null
)