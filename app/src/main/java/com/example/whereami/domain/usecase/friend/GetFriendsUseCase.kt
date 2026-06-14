package com.example.whereami.domain.usecase.friend

import com.example.whereami.domain.model.User
import com.example.whereami.domain.repository.FriendRepository
import com.example.whereami.domain.repository.UserRepository

data class FriendsData(
    val friends: List<User>,
    val pendingRequests: List<User>,
    val sentRequests: List<User>
)

class GetFriendsUseCase(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<FriendsData> {
        return runCatching {
            val friendIds = friendRepository.getFriends(userId).getOrThrow()
            val pendingIds = friendRepository.getPendingFriendRequests(userId).getOrThrow()
            val sentIds = friendRepository.getSentFriendRequests(userId).getOrThrow()

            val friends = userRepository.getUsers(friendIds.toList()).getOrNull() ?: emptyList()
            val pending = userRepository.getUsers(pendingIds.toList()).getOrNull() ?: emptyList()
            val sent = userRepository.getUsers(sentIds.toList()).getOrNull() ?: emptyList()

            FriendsData(friends, pending, sent)
        }
    }
}
