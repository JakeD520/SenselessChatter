package com.rivvr.core.models


data class Flow(
    val id: Long,
    val name: String,
    val createdByUserId: String,
    val createdAtEpochMs: Long
)