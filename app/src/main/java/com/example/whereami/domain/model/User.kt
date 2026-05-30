package com.example.whereami.domain.model

import kotlinx.datetime.Instant

data class User(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val createdAt: Instant,
    val gamesPlayed: Int
)
