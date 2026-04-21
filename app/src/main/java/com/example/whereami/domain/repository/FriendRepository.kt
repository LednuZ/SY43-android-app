package com.example.whereami.domain.repository

import com.example.whereami.domain.model.*

interface FriendRepository {
    suspend fun sendFriendRequest(from: String, to: String): Result<Unit>
    suspend fun acceptFriendRequest(from: String, to: String): Result<Unit>
    suspend fun getFriends(userId: String): Result<Set<String>>
}