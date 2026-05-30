package com.example.whereami.domain.usecase

import com.example.whereami.domain.repository.FriendRepository

class AcceptFriendRequestUseCase(private val friendRepository: FriendRepository) {
    suspend operator fun invoke(from: String, to: String): Result<Unit> {
        return friendRepository.acceptFriendRequest(from, to)
    }
}
