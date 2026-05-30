package com.example.whereami.domain.model

import kotlinx.datetime.Instant

data class Message(
    val id: String,
    val groupId: String,
    val senderId: String,
    val text: String,
    val createdAt: Instant
)