package com.rivvr.core.models

data class FlowRoom(
    val flow: Flow,
    val lastRipple: Ripple?,
    val unreadCount: Int = 0
)