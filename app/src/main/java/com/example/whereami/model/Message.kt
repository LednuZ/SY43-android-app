package com.example.whereami.model

import com.google.firebase.Timestamp

data class Message(
    val id: String,
    val groupId: String,
    val senderId: String,
    val text: String,
    val createdAt: Timestamp
)