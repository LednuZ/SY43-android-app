package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.*
import com.example.whereami.domain.repository.GameRepository
import com.example.whereami.domain.repository.GroupRepository
import kotlin.String

class StartGameUseCase(private val gameRepository: GameRepository, private val groupRepository: GroupRepository) {
    suspend operator fun invoke(gameId: String): StartGameResult {
        val game = gameRepository.getGame(gameId).getOrThrow() ?: return StartGameResult.GameNotFound

        if (game.status != GameStatus.CREATED) {
            return StartGameResult.InvalidGameState
        }

        val roundId = "round_${gameId}_${System.currentTimeMillis()}"

        val round = Round(
            id = roundId,
            gameId = gameId,
            index = 1,
            isFinished = false
        )

        val updatedGame = game.copy(
            status = GameStatus.PLAYING,
            currentRound = 1
        )

        gameRepository.createRound(round).getOrThrow()
        gameRepository.saveGame(updatedGame).getOrThrow()
        return StartGameResult.Success
    }
}

sealed class StartGameResult {
    object Success : StartGameResult()
    object GameNotFound : StartGameResult()
    object InvalidGameState : StartGameResult()
}