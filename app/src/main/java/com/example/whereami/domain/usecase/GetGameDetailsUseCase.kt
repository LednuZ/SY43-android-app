package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.Game
import com.example.whereami.domain.model.Round
import com.example.whereami.domain.repository.GameRepository

data class GameDetails(
    val game: Game,
    val currentRounds: List<Round>,
    val pastRounds: List<Round>
)

class GetGameDetailsUseCase(
    private val gameRepository: GameRepository,
    private val catchUpExpiredRoundsUseCase: CatchUpExpiredRoundsUseCase
) {
    suspend operator fun invoke(gameId: String): Result<GameDetails> {
        return try {
            val result = gameRepository.getGame(gameId)
            if (result.isFailure) return Result.failure(result.exceptionOrNull() ?: Exception("Failed to fetch game"))
            
            val game = result.getOrThrow() ?: return Result.failure(Exception("Game not found"))
            
            val updatedGame = catchUpExpiredRoundsUseCase(game)
            val currentRounds = updatedGame.rounds.filter { it.index == updatedGame.currentRoundIndex }
            val pastRounds = updatedGame.rounds.filter { it.index < updatedGame.currentRoundIndex }
            
            Result.success(GameDetails(updatedGame, currentRounds, pastRounds))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
