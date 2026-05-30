package com.example.whereami.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class FriendDto(
    val id: String? = null,
    val user_id_1: String,
    val user_id_2: String,
    val status: String,
    val created_at: String? = null
)
