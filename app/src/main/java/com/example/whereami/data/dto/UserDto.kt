package com.example.whereami.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val first_name: String? = null,
    val last_name: String? = null,
    val phone_number: String? = null,
    val created_at: String,
    val games_played: Int = 0
)
