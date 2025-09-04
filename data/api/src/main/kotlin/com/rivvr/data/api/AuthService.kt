package com.rivvr.data.api

import com.rivvr.core.models.Profile

interface AuthService {
    suspend fun signIn(email: String, password: String): Profile
    suspend fun signUp(email: String, password: String): Profile
    suspend fun signOut()
    suspend fun currentProfile(): Profile?
}
