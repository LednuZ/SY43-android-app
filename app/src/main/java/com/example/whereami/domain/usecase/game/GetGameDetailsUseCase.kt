package com.example.whereami.domain.usecase.game

import com.example.whereami.domain.model.Game
import com.example.whereami.domain.model.Round
import com.example.whereami.domain.repository.GameRepository
import com.example.whereami.domain.repository.UserRepository
import com.example.whereami.domain.usecase.round.CatchUpExpiredRoundsUseCase

data class GameDetails(
    val game: Game,
    val currentRounds: List<Round>,
    val pastRounds: List<Round>,
    val playerUsernames: Map<String, String>
)

class GetGameDetailsUseCase(
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
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
            
            val userIds = updatedGame.scoreSheets.map { it.playerId }.distinct()
            val players = if (userIds.isNotEmpty()) {
                userRepository.getUsers(userIds).getOrDefault(emptyList())
            } else {
                emptyList()
            }
            val playerUsernames = players.associate { it.id to it.username }
            
            Result.success(GameDetails(updatedGame, currentRounds, pastRounds, playerUsernames))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
