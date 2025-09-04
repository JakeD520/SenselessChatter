package com.rivvr.data.implstub


import com.rivvr.core.models.Flow
import com.rivvr.core.models.Ripple
import com.rivvr.data.api.FlowRepository


class StubFlowRepository : FlowRepository {
    private val flows = listOf(
        Flow(1L, "Friday Night Flow", createdByUserId = "system", createdAtEpochMs = 1_725_000_000_000),
        Flow(2L, "Philosophy Corner", createdByUserId = "system", createdAtEpochMs = 1_725_000_100_000)
    )


    private val ripples = mutableMapOf(
        "1" to mutableListOf(
            Ripple(101L, 1L, senderUserId = "Maple Otter", text = "What are we riffing tonight?", createdAtEpochMs = 1_725_000_200_000),
            Ripple(102L, 1L, senderUserId = "Silver Heron", text = "Drop your spiciest takes.", createdAtEpochMs = 1_725_000_300_000)
        ),
        "2" to mutableListOf(
            Ripple(103L, 2L, senderUserId = "Neon Dingo", text = "Consciousness = jazz?", createdAtEpochMs = 1_725_000_400_000)
        )
    )


    override suspend fun listFlows(): List<Flow> = flows


    override suspend fun listRipples(flowId: String, limit: Int): List<Ripple> =
        ripples[flowId]?.takeLast(limit) ?: emptyList()


    override suspend fun sendRipple(flowId: String, alias: String, text: String): Ripple {
        val flowIdLong = flowId.toLongOrNull() ?: 1L
        val r = Ripple(System.nanoTime(), flowIdLong, senderUserId = alias, text = text, createdAtEpochMs = System.currentTimeMillis())
        ripples.getOrPut(flowId) { mutableListOf() }.add(r)
        return r
    }
}