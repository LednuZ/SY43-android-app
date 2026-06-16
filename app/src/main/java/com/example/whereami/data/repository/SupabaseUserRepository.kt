package com.example.whereami.data.repository

import com.example.whereami.data.dto.UserDto
import com.example.whereami.domain.model.User
import com.example.whereami.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlin.time.Instant

class SupabaseUserRepository(private val client: SupabaseClient) : UserRepository {

    override suspend fun getUser(userId: String): Result<User?> {
        return runCatching {
            val dto = client.from("users").select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingleOrNull<UserDto>()
            dto?.toDomain()
        }
    }

    override suspend fun saveUser(user: User): Result<Unit> {
        return runCatching {
            val dto = user.toDto()
            client.from("users").upsert(dto)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return runCatching {
            client.from("users").select {
                filter {
                    or {
                        ilike("username", "%$query%")
                    }
                }
            }.decodeList<UserDto>().map { it.toDomain() }
        }
    }

    override suspend fun getUsers(userIds: List<String>): Result<List<User>> {
        return runCatching {
            if (userIds.isEmpty()) return@runCatching emptyList()
            
            client.from("users").select {
                filter {
                    isIn("id", userIds)
                }
            }.decodeList<UserDto>().map { it.toDomain() }
        }
    }

    private fun UserDto.toDomain(): User {
        return User(
            id = id,
            username = username,
            email = email,
            firstName = first_name,
            lastName = last_name,
            phoneNumber = phone_number,
            createdAt = Instant.parse(created_at),
            guessCount = guess_count,
            profilePicture = profile_picture
        )
    }

    private fun User.toDto(): UserDto {
        return UserDto(
            id = id,
            username = username,
            email = email,
            first_name = firstName,
            last_name = lastName,
            phone_number = phoneNumber,
            created_at = createdAt.toString(),
            guess_count = guessCount,
            profile_picture = profilePicture
        )
    }
}
