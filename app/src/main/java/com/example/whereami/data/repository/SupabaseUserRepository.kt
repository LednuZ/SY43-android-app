package com.example.whereami.data.repository

import com.example.whereami.data.dto.UserDto
import com.example.whereami.domain.model.User
import com.example.whereami.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import com.example.whereami.data.dto.GameDto
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
            client.from("users").update(
                {
                    set("username", user.username)
                    set("first_name", user.firstName)
                    set("last_name", user.lastName)
                    set("phone_number", user.phoneNumber)
                }
            ) {
                filter { eq("id", user.id) }
            }
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
            }.decodeList<UserDto>()
                .map { it.toDomain() }
                .filter { !it.username.startsWith("Deleted User ") }
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

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return runCatching {
            // 1. Anonymize user
            client.from("users").update(
                {
                    set("username", "Deleted User ${userId.take(4)}")
                    set("email", "deleted_$userId@whereami.com")
                    set("first_name", null as String?)
                    set("last_name", null as String?)
                    set("phone_number", null as String?)
                    set("profile_picture", null as String?)
                }
            ) {
                filter { eq("id", userId) }
            }

            // 2. Remove from groups
            client.from("group_members").delete {
                filter { eq("user_id", userId) }
            }

            // 3. Remove from active games
            val activeGameIds = client.from("games").select {
                filter { neq("status", "FINISHED") }
            }.decodeList<GameDto>().mapNotNull { it.id }

            if (activeGameIds.isNotEmpty()) {
                client.from("game_scores").delete {
                    filter {
                        eq("player_id", userId)
                        isIn("game_id", activeGameIds)
                    }
                }
            }
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
