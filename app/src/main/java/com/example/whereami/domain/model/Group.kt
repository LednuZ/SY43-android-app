package com.example.whereami.domain.model

import com.google.firebase.Timestamp

data class Group(
    val id: String,
    val name: String,
    val createdAt: Timestamp,
    val memberIds: List<String>
)