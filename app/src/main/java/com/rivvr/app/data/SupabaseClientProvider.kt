package com.rivvr.app.data

import com.rivvr.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.functions.Functions

object SupabaseClientProvider {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // Let Supabase auto-detect the best engine
            // Removed explicit engine configuration to avoid HttpTimeout issues
            
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Functions)
        }
    }
}
