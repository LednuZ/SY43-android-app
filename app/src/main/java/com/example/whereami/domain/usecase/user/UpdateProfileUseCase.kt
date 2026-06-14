package com.example.whereami.domain.usecase.user

import com.example.whereami.domain.model.User
import com.example.whereami.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateProfileUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(user: User): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (user.username.isBlank()) {
                    return@withContext Result.failure(IllegalArgumentException("Username cannot be empty"))
                }
                userRepository.saveUser(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
