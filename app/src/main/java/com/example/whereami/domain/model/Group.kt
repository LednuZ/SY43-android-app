package com.example.whereami.domain.model

import kotlin.time.Instant

data class Group(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val memberIds: MutableList<String> = mutableListOf()
)