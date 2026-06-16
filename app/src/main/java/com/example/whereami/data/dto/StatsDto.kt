package com.example.whereami.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoundScoreDto(
    val id: String? = null,
    val round_id: String,
    val player_id: String,
    val score: Int
)

@Serializable
data class PlayerStatsDto(
    val player_id: String,
    val total_score: Int,
    val games_played: Int,
    val wins: Int
)
