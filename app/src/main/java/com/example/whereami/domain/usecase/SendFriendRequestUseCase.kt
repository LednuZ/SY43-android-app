package com.example.whereami.domain.usecase

import com.example.whereami.domain.repository.FriendRepository

class SendFriendRequestUseCase(private val friendRepository: FriendRepository) {
    suspend operator fun invoke(from: String, to: String): Result<Unit> {
        if (from == to) return Result.failure(Exception("Cannot send friend request to yourself"))
        return friendRepository.sendFriendRequest(from, to)
    }
}
