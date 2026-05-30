package com.example.whereami.domain.model

import kotlin.time.Instant

data class Score(
    val gameId: String,
    val playerId: String,
    val score: Int,
    val lastUpdated: Instant
)