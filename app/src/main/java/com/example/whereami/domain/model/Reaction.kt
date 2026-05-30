package com.example.whereami.domain.model

import kotlin.time.Instant

data class Reaction(
    val id: String,
    val userId: String,
    val pictureId: String? = null,
    val messageId: String? = null,
    val emoji: String,
    val createdAt: Instant
)