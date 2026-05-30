package com.example.whereami.domain.model

data class RoundScore(
    val id: String,
    val roundId: String,
    val playerId: String,
    val score: Int
)

data class PlayerStats(
    val playerId: String,
    val totalScore: Int,
    val gamesPlayed: Int,
    val wins: Int
)
