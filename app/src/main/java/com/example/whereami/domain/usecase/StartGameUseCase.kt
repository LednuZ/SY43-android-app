package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.*
import com.example.whereami.domain.repository.GameRepository
import com.example.whereami.domain.repository.GroupRepository
import kotlin.String

class StartGameUseCase(
    private val gameRepository: GameRepository,
    private val createRoundUseCase: CreateRoundUseCase
) {
    suspend operator fun invoke(gameId: String): StartGameResult {
        val game = gameRepository.getGame(gameId).getOrThrow() ?: return StartGameResult.GameNotFound

        if (game.status != GameStatus.CREATED) {
            return StartGameResult.InvalidGameState
        }

        val updatedGame = game.copy(
            status = GameStatus.PLAYING,
            currentRoundIndex = 1
        )

        gameRepository.saveGame(updatedGame).getOrThrow()

        return when (val result = createRoundUseCase(gameId)) {
            is CreateRoundResult.Success -> StartGameResult.Success
            else -> StartGameResult.FailedToStartRound(result)
        }
    }
}

sealed class StartGameResult {
    object Success : StartGameResult()
    object GameNotFound : StartGameResult()
    object InvalidGameState : StartGameResult()
    data class FailedToStartRound(val result: CreateRoundResult) : StartGameResult()
}