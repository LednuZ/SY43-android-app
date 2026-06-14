package com.example.whereami.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GameDto(
    val id: String? = null,
    val group_id: String,
    val nb_rounds: Int,
    val round_duration_minutes: Long,
    val date_begin: String,
    val date_end: String,
    val current_round_index: Int = 0,
    val status: String,
    val created_by: String? = null,
    val created_at: String? = null
)

@Serializable
data class GameScoreDto(
    val id: String? = null,
    val game_id: String,
    val player_id: String,
    val score: Int,
    val rank: Int? = null,
    val date_last_update: String? = null
)



@Serializable
data class RoundDto(
    val id: String? = null,
    val game_id: String,
    val index: Int,
    val status: String,
    val start_time: String,
    val end_time: String
)

@Serializable
data class PictureDto(
    val id: String? = null,
    val round_id: String,
    val publisher_id: String,
    val image_url: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    val created_at: String,
    val revealed_at: String? = null
)

@Serializable
data class GuessDto(
    val id: String? = null,
    val round_id: String,
    val player_id: String,
    val picture_id: String,
    val latitude: Double,
    val longitude: Double,
    val guessed_at: String,
    val distance_meters: Double,
    val guess_score: Int
)
