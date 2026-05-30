package com.example.whereami.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class FriendDto(
    val id: String? = null,
    val player_1_id: String,
    val player_2_id: String,
    val status: String,
    val created_at: String? = null
)
