package com.rivvr.data.implstub


import com.rivvr.core.models.Profile
import com.rivvr.data.api.AuthService


class StubAuthService : AuthService {
    private var profile: Profile? = null


    override suspend fun currentProfile(): Profile? = profile


    override suspend fun signIn(email: String, password: String): Profile {
        profile = Profile(id = "p-stub-signin", email = email, displayName = email.substringBefore("@"))
        return profile!!
    }


    override suspend fun signUp(email: String, password: String): Profile {
        profile = Profile(id = "p-stub-signup", email = email, displayName = email.substringBefore("@"))
        return profile!!
    }


    override suspend fun signOut() { profile = null }

    override suspend fun updateProfile(displayName: String?): Profile? {
        profile = profile?.copy(displayName = displayName)
        return profile
    }
}