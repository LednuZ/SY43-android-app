package com.example.whereami.domain.usecase

import com.example.whereami.domain.repository.FriendRepository
import com.example.whereami.domain.repository.UserRepository

class SendFriendRequestUseCase(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(fromId: String, toId: String): SendRequestResult {
        if (fromId == toId) {
            return SendRequestResult.Error("You cannot send a friend request to yourself.")
        }

        val targetUser = userRepository.getUser(toId).getOrNull()
        if (targetUser == null) {
            return SendRequestResult.Error("The user you are trying to add does not exist.")
        }

        val currentFriends = friendRepository.getFriends(fromId).getOrNull() ?: emptySet()
        if (currentFriends.contains(toId)) {
            return SendRequestResult.Error("You are already friends with this user.")
        }

        val sentRequests = friendRepository.getSentFriendRequests(fromId).getOrNull() ?: emptySet()
        if (sentRequests.contains(toId)) {
            return SendRequestResult.Error("You already sent a request to this user.")
        }

        // if the other user has sent a request to us, accept it
        val incomingRequests = friendRepository.getPendingFriendRequests(fromId).getOrNull() ?: emptySet()
        if (incomingRequests.contains(toId)) {
            friendRepository.acceptFriendRequest(from = toId, to = fromId)
            return SendRequestResult.AutoAccepted
        }

        val result = friendRepository.sendFriendRequest(fromId, toId)

        return if (result.isSuccess) {
            SendRequestResult.Success
        } else {
            SendRequestResult.Error("Database failed to process the request.")
        }
    }
}
sealed class SendRequestResult {
    data object Success : SendRequestResult()
    data object AutoAccepted : SendRequestResult()
    data class Error(val message: String) : SendRequestResult()
}