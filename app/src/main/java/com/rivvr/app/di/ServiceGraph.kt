package com.rivvr.app.di

import com.rivvr.data.api.AuthService
import com.rivvr.data.api.FlowsService
import com.rivvr.data.api.RoomsService
import com.rivvr.data.supabase.SupabaseAuthService
import com.rivvr.data.supabase.SupabaseFlowsService
import com.rivvr.data.supabase.SupabaseRoomsService
import com.rivvr.app.data.SupabaseClientProvider

object ServiceGraph {
    val auth: AuthService by lazy { SupabaseAuthService(SupabaseClientProvider.client) }
    val flows: FlowsService by lazy { SupabaseFlowsService(SupabaseClientProvider.client) }
    val rooms: RoomsService by lazy { SupabaseRoomsService(SupabaseClientProvider.client) }
}
