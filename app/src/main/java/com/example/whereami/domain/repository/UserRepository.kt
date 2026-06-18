package com.example.whereami.domain.repository

import com.example.whereami.domain.model.User

interface UserRepository {
    suspend fun getUser(userId: String): Result<User?>
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun searchUsers(query: String): Result<List<User>>
    suspend fun getUsers(userIds: List<String>): Result<List<User>>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun uploadProfilePicture(userId: String, imageBytes: ByteArray): Result<String>
}
