package com.example.whereami.domain.usecase.user

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdatePasswordUseCase(private val client: SupabaseClient) {
    suspend operator fun invoke(newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (newPassword.length < 6) {
                    return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
                }
                client.auth.updateUser {
                    password = newPassword
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
