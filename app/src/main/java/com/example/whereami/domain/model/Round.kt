package com.example.whereami.domain.model

enum class RoundStatus { CREATED, FINISHED }
data class Round(
    val id: String,
    val gameId: String,
    val posts: MutableList<Picture> = mutableListOf(),
    val index: Int,
    val status: RoundStatus,
    val startTime: kotlin.time.Instant,
    val endTime: kotlin.time.Instant
)