package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.*
import com.example.whereami.domain.repository.GameRepository

class CreateRoundUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(gameId: String): CreateRoundResult {
        val game = gameRepository.getGame(gameId).getOrThrow() ?: return CreateRoundResult.GameNotFound

        if (game.status != GameStatus.CREATED && game.status != GameStatus.PLAYING) {
            return CreateRoundResult.GameNotActive
        }

        if (game.currentRoundIndex >= game.settings.nbRound) {
            return CreateRoundResult.MaxRoundsReached
        }

        val previousRoundNumber = game.currentRoundIndex - 1

        if (previousRoundNumber > 0) {
            val previousRound = game.listRounds[previousRoundNumber]
            if (previousRound.status != RoundStatus.FINISHED) {
                return CreateRoundResult.PreviousRoundNotFinished
            }
        }

        val roundId = "round_${gameId}_${System.currentTimeMillis()}"
        val nextRoundIndex = game.currentRoundIndex + 1
        val round = Round(
            id = roundId,
            gameId = gameId,
            index = nextRoundIndex,
            status = RoundStatus.CREATED
        )

        val updatedGame = game.copy(
            currentRoundIndex = nextRoundIndex,
            listRounds = (game.listRounds + round).toMutableList()
        )

        gameRepository.saveGame(updatedGame).getOrThrow()
        return CreateRoundResult.Success
    }
}

sealed class CreateRoundResult {
    object Success : CreateRoundResult()
    object GameNotFound : CreateRoundResult()
    object GameNotActive : CreateRoundResult()
    object PreviousRoundNotFinished : CreateRoundResult()
    object MaxRoundsReached : CreateRoundResult()
}