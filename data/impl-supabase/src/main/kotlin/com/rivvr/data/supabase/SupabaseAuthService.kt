package com.rivvr.data.supabase

import com.rivvr.data.api.AuthService
import com.rivvr.core.models.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseAuthService(
    private val client: SupabaseClient
) : AuthService {

    override suspend fun signIn(email: String, password: String): Profile {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return currentProfile() ?: throw IllegalStateException("Failed to get profile after sign in")
    }

    override suspend fun signUp(email: String, password: String): Profile {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        return currentProfile() ?: throw IllegalStateException("Failed to get profile after sign up")
    }

    override suspend fun signOut() {
        client.auth.signOut()
    }

    override suspend fun updateProfile(displayName: String?): Profile? {
        val userId = client.auth.currentUserOrNull()?.id ?: return null

        // Update the profile in the database
        client.from("profiles").update({
            if (displayName != null) {
                set("display_name", displayName)
            }
        }) {
            filter {
                eq("id", userId)
            }
        }

        // Return the updated profile
        return currentProfile()
    }

    override suspend fun currentProfile(): Profile? {
        println("🔐 SupabaseAuthService: Getting current profile...")
        val user = client.auth.currentUserOrNull()
        
        if (user == null) {
            println("❌ SupabaseAuthService: No current user in auth session")
            return null
        }
        
        println("✅ SupabaseAuthService: Found auth session for user ID: ${user.id}")
        val userId = user.id

        val rows = client.from("profiles").select {
            filter {
                eq("id", userId)
            }
        }.decodeList<ProfileRow>()

        val profile = rows.firstOrNull()?.toModel()
        
        if (profile != null) {
            println("✅ SupabaseAuthService: Successfully retrieved profile from database")
        } else {
            println("❌ SupabaseAuthService: User in auth session but no profile in database")
        }
        
        return profile
    }

    @Serializable
    private data class ProfileRow(
        val id: String,
        val email: String,
        @SerialName("display_name") val displayName: String? = null,
        @SerialName("avatar_url") val avatarUrl: String? = null,
        @SerialName("created_at") val createdAt: String? = null
    )

    private fun ProfileRow.toModel() = Profile(
        id = id,
        email = email,
        displayName = displayName,
        avatarUrl = avatarUrl,
        createdAtEpochMs = null
    )
}
