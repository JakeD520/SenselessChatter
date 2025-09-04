package com.rivvr.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseFactory {

    fun create(url: String, anonKey: String): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = anonKey
        ) {
            // Keep config blocks empty for now to avoid type inference hassles
            install(Auth)
            install(Postgrest)
        }
}
