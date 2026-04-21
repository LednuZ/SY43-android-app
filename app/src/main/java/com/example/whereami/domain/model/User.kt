package com.example.whereami.domain.model

import com.google.firebase.Timestamp

data class User(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val createdAt: Timestamp,
    val gamesPlayed: Int
)
