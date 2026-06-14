package com.example.whereami.domain.usecase.friend

import com.example.whereami.domain.repository.FriendRepository
import com.example.whereami.domain.repository.UserRepository

class AcceptFriendRequestUseCase(
    private val friendRepository: FriendRepository,
) {
    suspend operator fun invoke(fromId: String, toId: String): AcceptRequestResult {
        if (fromId == toId) {
            return AcceptRequestResult.Error("Invalid request.")
        }

        val currentFriends = friendRepository.getFriends(toId).getOrNull() ?: emptySet()
        if (currentFriends.contains(fromId)) {
            return AcceptRequestResult.Error("You are already friends.")
        }

        val incomingRequests = friendRepository.getPendingFriendRequests(toId).getOrNull() ?: emptySet()
        if (!incomingRequests.contains(fromId)) {
            return AcceptRequestResult.Error("Friend request no longer exists.")
        }

        val result = friendRepository.acceptFriendRequest(fromId, toId)

        return if (result.isSuccess) {
            AcceptRequestResult.Success
        } else {
            AcceptRequestResult.Error("Database failed to process the request.")
        }
    }
}

sealed class AcceptRequestResult {
    data object Success : AcceptRequestResult()
    data class Error(val message: String) : AcceptRequestResult()
}
