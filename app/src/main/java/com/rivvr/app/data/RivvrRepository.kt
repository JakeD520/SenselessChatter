package com.rivvr.app.data

import com.rivvr.core.models.FlowRoom
import com.rivvr.core.models.Profile
import com.rivvr.core.models.Ripple
import com.rivvr.data.api.AuthService
import com.rivvr.data.api.FlowsService

class RivvrRepository(
    private val auth: AuthService,
    private val flows: FlowsService
) {
    suspend fun signIn(email: String, password: String): Profile = auth.signIn(email, password)
    suspend fun signUp(email: String, password: String): Profile = auth.signUp(email, password)
    suspend fun signOut() = auth.signOut()
    suspend fun currentProfile(): Profile? = auth.currentProfile()

    suspend fun myRooms(userId: String): List<FlowRoom> = flows.myRooms(userId)
    suspend fun latestRipples(flowId: String): List<Ripple> = flows.latestRipples(flowId)
}
