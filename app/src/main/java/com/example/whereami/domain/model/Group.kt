package com.example.whereami.domain.model

import kotlinx.datetime.Instant

data class Group(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val memberIds: MutableList<String> = mutableListOf()
)