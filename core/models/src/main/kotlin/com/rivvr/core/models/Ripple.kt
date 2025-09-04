package com.rivvr.core.models


data class Ripple(
    val id: Long,
    val flowId: Long,
    val senderUserId: String,
    val text: String? = null,
    val mediaUrl: String? = null,
    val createdAtEpochMs: Long
)