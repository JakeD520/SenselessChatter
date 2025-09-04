package com.rivvr.data.api

import com.rivvr.core.models.FlowRoom
import com.rivvr.core.models.Ripple

interface FlowsService {
    suspend fun myRooms(userId: String, limit: Int = 20): List<FlowRoom>
    suspend fun latestRipples(flowId: String, limit: Int = 50): List<Ripple>
}
