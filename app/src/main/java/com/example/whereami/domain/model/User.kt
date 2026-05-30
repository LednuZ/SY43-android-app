package com.example.whereami.domain.model

import kotlin.time.Instant

data class User(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val createdAt: Instant,
    val guessCount: Int,
    val profilePicture: String?
)
