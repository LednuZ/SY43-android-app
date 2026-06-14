package com.example.whereami.domain.usecase.round

import com.example.whereami.domain.model.Game
import com.example.whereami.domain.repository.GameRepository

class CatchUpExpiredRoundsUseCase(
    private val gameRepository: GameRepository,
    private val advanceRoundUseCase: AdvanceRoundUseCase
) {
    suspend operator fun invoke(game: Game): Game {
        var updatedGame = game

        while (updatedGame.currentRoundIndex < updatedGame.settings.nbRound) {
            val currentRound = updatedGame.rounds.firstOrNull { it.index == updatedGame.currentRoundIndex } ?: break
            
            if (currentRound.endTime < kotlin.time.Clock.System.now() && currentRound.status != com.example.whereami.domain.model.RoundStatus.FINISHED) {
                val advanceResult = advanceRoundUseCase(updatedGame, currentRound)
                if (advanceResult.isSuccess) {
                    updatedGame = advanceResult.getOrThrow()
                } else {
                    break
                }
            } else {
                break
            }
        }

        return updatedGame
    }
}
