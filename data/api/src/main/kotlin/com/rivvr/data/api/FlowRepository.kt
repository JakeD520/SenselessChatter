package com.rivvr.data.api


import com.rivvr.core.models.Flow
import com.rivvr.core.models.Ripple


interface FlowRepository {
    suspend fun listFlows(): List<Flow>
    suspend fun listRipples(flowId: String, limit: Int = 50): List<Ripple>
    suspend fun sendRipple(flowId: String, alias: String, text: String): Ripple
}