package com.example.whereami.domain.model

import kotlin.time.Instant

enum class GameStatus { CREATED, PLAYING, FINISHED }
data class Game(
    val id: String = "",
    val groupId: String,
    val settings: GameSettings,
    val currentRoundIndex: Int = 0,
    val listRounds: MutableList<Round> = mutableListOf(),
    val listPlayers: MutableList<String> = mutableListOf(),
    val status: GameStatus,
    val scoreSheets: List<Score> = mutableListOf()
)

fun Game.getCurrentRound(): Round? {
    return this.listRounds.firstOrNull { round -> round.index == this.currentRoundIndex }
}

fun Game.updateRoundInGame(round: Round): Game {
    return this.copy(
        listRounds = this.listRounds.map {
            if (it.id == round.id) {
                round
            } else {
                it
            }
        }.toMutableList()
    )
}

data class GameSettings(
    val nbRound: Int,
    val roundDurationMinutes: Long,
    val dateBegin: Instant,
    val dateEnd: Instant
)