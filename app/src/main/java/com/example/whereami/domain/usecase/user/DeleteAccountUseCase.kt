package com.example.whereami.domain.usecase.user

import com.example.whereami.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth

class DeleteAccountUseCase(
    private val userRepository: UserRepository,
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return try {
            val deleteResult = userRepository.deleteUser(userId)
            if (deleteResult.isFailure) return Result.failure(deleteResult.exceptionOrNull() ?: Exception("Failed to delete user"))

            supabaseClient.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
