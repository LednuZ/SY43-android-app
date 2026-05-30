package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.User
import com.example.whereami.domain.repository.UserRepository

class SearchUsersUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(query: String): Result<List<User>> {
        if (query.isBlank()) return Result.success(emptyList())
        return userRepository.searchUsers(query)
    }
}
