package com.example.whereami.domain.model

import kotlin.time.Instant

enum class GameStatus { CREATED, PLAYING, FINISHED }
data class Game(
    val id: String = "",
    val groupId: String,
    val settings: GameSettings,
    val currentRoundIndex: Int = 0,
    val rounds: List<Round> = emptyList(),
    val playerIds: List<String> = emptyList(),
    val scoreSheets: List<Score> = emptyList(),
    val status: GameStatus
)

fun Game.getCurrentRound(): Round? {
    return this.rounds.firstOrNull { round -> round.index == this.currentRoundIndex }
}

fun Game.updateRoundInGame(round: Round): Game {
    return this.copy(
        rounds = this.rounds.map {
            if (it.id == round.id) round else it
        }
    )
}

data class GameSettings(
    val nbRound: Int,
    val roundDurationMinutes: Long,
    val dateBegin: Instant,
    val dateEnd: Instant
)