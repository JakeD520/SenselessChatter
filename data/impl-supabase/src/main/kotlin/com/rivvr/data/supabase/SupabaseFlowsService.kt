package com.rivvr.data.supabase

import com.rivvr.data.api.FlowsService
import com.rivvr.core.models.Flow
import com.rivvr.core.models.FlowRoom
import com.rivvr.core.models.Ripple
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseFlowsService(
    private val client: SupabaseClient
) : FlowsService {

    override suspend fun myRooms(userId: String, limit: Int): List<FlowRoom> {
        val flows = client.from("flows").select {
            filter {
                eq("created_by", userId)
            }
            order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            limit(limit.toLong())
        }.decodeList<FlowRow>()
            .map { it.toModel() }

        return flows.map { flow ->
            FlowRoom(flow = flow, lastRipple = null, unreadCount = 0)
        }
    }

    override suspend fun latestRipples(flowId: String, limit: Int): List<Ripple> {
        val rows = client.from("ripples").select {
            filter {
                eq("flow_id", flowId)
            }
            order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            limit(limit.toLong())
        }.decodeList<RippleRow>()

        return rows.map { it.toModel() }
    }

    // --- DTOs -> Models ---

    @Serializable
    private data class FlowRow(
        val id: String,
        val name: String,
        @SerialName("created_by") val createdBy: String,
        @SerialName("created_at") val createdAt: String? = null
    )

    private fun FlowRow.toModel() = Flow(
        id = id.toLongOrNull() ?: 0L,
        name = name,
        createdByUserId = createdBy,
        createdAtEpochMs = 0L
    )

    @Serializable
    private data class RippleRow(
        val id: String,
        @SerialName("flow_id") val flowId: String,
        @SerialName("sender_user_id") val senderUserId: String,
        val text: String? = null,
        @SerialName("media_url") val mediaUrl: String? = null,
        @SerialName("created_at") val createdAt: String? = null
    )

    private fun RippleRow.toModel() = Ripple(
        id = id.toLongOrNull() ?: 0L,
        flowId = flowId.toLongOrNull() ?: 0L,
        senderUserId = senderUserId,
        text = text,
        mediaUrl = mediaUrl,
        createdAtEpochMs = 0L
    )
}
