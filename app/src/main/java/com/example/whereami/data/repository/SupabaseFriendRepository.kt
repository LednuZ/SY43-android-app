package com.example.whereami.data.repository

import com.example.whereami.data.dto.FriendDto
import com.example.whereami.domain.repository.FriendRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class SupabaseFriendRepository(private val client: SupabaseClient) : FriendRepository {

    override suspend fun sendFriendRequest(from: String, to: String): Result<Unit> {
        return runCatching {
            val dto = FriendDto(
                user_id_1 = from,
                user_id_2 = to,
                status = "PENDING"
            )
            client.from("friendships").insert(dto)
        }
    }

    override suspend fun acceptFriendRequest(from: String, to: String): Result<Unit> {
        return runCatching {
            client.from("friendships").update({
                set("status", "ACCEPTED")
            }) {
                filter {
                    eq("user_id_1", from)
                    eq("user_id_2", to)
                }
            }
        }
    }

    override suspend fun getFriends(userId: String): Result<Set<String>> {
        return runCatching {
            val friends1 = client.from("friendships").select {
                filter {
                    eq("user_id_1", userId)
                    eq("status", "ACCEPTED")
                }
            }.decodeList<FriendDto>().map { it.user_id_2 }

            val friends2 = client.from("friendships").select {
                filter {
                    eq("user_id_2", userId)
                    eq("status", "ACCEPTED")
                }
            }.decodeList<FriendDto>().map { it.user_id_1 }

            (friends1 + friends2).toSet()
        }
    }
}
