package com.rivvr.data.conventional

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

// Single file setup - exactly as consultant report shows
object SupabaseClientConventional {
    
    // Note: In real implementation, inject these from BuildConfig
    // For now, we'll use the existing provider pattern
    fun createClient(supabaseUrl: String, supabaseKey: String): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Auth)
            install(Postgrest) 
            install(Realtime)
        }
    }
}