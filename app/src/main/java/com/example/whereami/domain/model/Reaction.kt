package com.example.whereami.domain.model

import kotlinx.datetime.Instant

data class Reaction(
    val id: String,
    val userId: String,
    val pictureId: String? = null,
    val messageId: String? = null,
    val emoji: String,
    val createdAt: Instant
)