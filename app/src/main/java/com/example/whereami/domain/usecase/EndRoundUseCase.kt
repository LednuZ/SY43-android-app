package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.Game
import com.example.whereami.domain.model.GameStatus
import com.example.whereami.domain.model.Round
import com.example.whereami.domain.model.*
import com.example.whereami.domain.repository.GameRepository

class EndRoundUseCase(
    private val gameRepository: GameRepository,
    private val createRoundUseCase: CreateRoundUseCase,
    private val endGameUseCase: EndGameUseCase,
    private val calculateRoundScoresUseCase: CalculateRoundScoresUseCase
) {
    suspend operator fun invoke(gameId: String): EndRoundResult {
        val game = gameRepository.getGame(gameId).getOrThrow() ?: return EndRoundResult.GameNotFound

        if (game.status != GameStatus.PLAYING) { return EndRoundResult.GameNotActive }

        val currentRound = game.getCurrentRound() ?: return EndRoundResult.RoundNotFound

        if (currentRound.status != RoundStatus.REVEALED) { return EndRoundResult.RoundNotActive }

        val updatedRound = currentRound.copy(status = RoundStatus.FINISHED)

        if (currentRound.index < game.settings.nbRound) {
            val nextRoundResult = createRoundUseCase(gameId)
            if (nextRoundResult != CreateRoundResult.Success) {
                return EndRoundResult.NextRoundFailed(nextRoundResult)
            }
        } else {
            val endGameResult = endGameUseCase(gameId)
            if (endGameResult != EndGameResult.Success) {
                return EndRoundResult.EndGameFailed(endGameResult)
            }
        }

        val updatedGame = game.updateRoundInGame(updatedRound).copy(

        )
        gameRepository.saveGame(updatedGame).getOrThrow()
        return EndRoundResult.Success

    }


}

sealed class EndRoundResult{
    data class NextRoundStarted(val round: Round) : EndRoundResult()
    data class NextRoundFailed(val result: CreateRoundResult) : EndRoundResult()
    data class EndGameFailed(val result: EndGameResult) : EndRoundResult()
    object Success : EndRoundResult()
    object GameNotFound : EndRoundResult()
    object GameNotActive : EndRoundResult()
    object RoundNotFound : EndRoundResult()
    object RoundNotActive : EndRoundResult()
}