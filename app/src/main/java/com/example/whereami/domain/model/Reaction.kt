package com.example.whereami.domain.model

import com.google.firebase.Timestamp

data class Reaction(
    val id: String,
    val userId: String,
    val pictureId: String? = null,
    val messageId: String? = null,
    val emoji: String,
    val createdAt: Timestamp
)