package com.example.whereami.domain.model

import com.google.firebase.Timestamp

enum class GameStatus { CREATED, PLAYING, FINISHED }
data class Game(
    val id: String,
    val groupId: String,
    val settings: GameSettings,
    val currentRound: Int?,
    val status: GameStatus,
    val scoreSheets: List<Score?>
)

data class GameSettings(
    val nbRound: Int,
    val roundDurationMinutes: Long,
    val dateBegin: Timestamp,
    val dateEnd: Timestamp
)