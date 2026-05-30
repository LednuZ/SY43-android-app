package com.example.whereami.data.repository

import com.example.whereami.data.dto.FriendDto
import com.example.whereami.domain.repository.FriendRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class SupabaseFriendRepository(private val client: SupabaseClient) : FriendRepository {

    override suspend fun sendFriendRequest(from: String, to: String): Result<Unit> {
        return runCatching {
            val dto = FriendDto(
                player_1_id = from,
                player_2_id = to,
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
                    eq("player_1_id", from)
                    eq("player_2_id", to)
                }
            }
        }
    }

    override suspend fun getFriends(userId: String): Result<Set<String>> {
        return runCatching {
            val friends1 = client.from("friendships").select {
                filter {
                    eq("player_1_id", userId)
                    eq("status", "ACCEPTED")
                }
            }.decodeList<FriendDto>().map { it.player_2_id }

            val friends2 = client.from("friendships").select {
                filter {
                    eq("player_2_id", userId)
                    eq("status", "ACCEPTED")
                }
            }.decodeList<FriendDto>().map { it.player_1_id }

            (friends1 + friends2).toSet()
        }
    }
}
