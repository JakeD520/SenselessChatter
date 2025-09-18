package com.rivvr.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseFactory {

    fun create(url: String, anonKey: String): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = anonKey
        ) {
            install(Auth) {
                // Enable automatic session handling
                autoSaveToStorage = true
                autoLoadFromStorage = true
            }
            install(Postgrest)
            install(Realtime)
        }
}
