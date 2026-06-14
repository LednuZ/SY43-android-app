package com.example.whereami.domain.usecase.friend

import com.example.whereami.domain.repository.FriendRepository

class DeleteFriendUseCase(private val friendRepository: FriendRepository) {
    suspend operator fun invoke(user1: String, user2: String): Result<Unit> {
        return friendRepository.deleteFriend(user1, user2)
    }
}
