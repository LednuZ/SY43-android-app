package com.example.whereami.domain.model

import kotlinx.datetime.Instant

data class Score(
    val userId: String,
    val totalScore: Int,
    val lastUpdated: Instant
)