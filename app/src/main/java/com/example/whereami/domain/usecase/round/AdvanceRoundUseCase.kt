package com.example.whereami.domain.usecase.round

import com.example.whereami.domain.model.Game
import com.example.whereami.domain.model.Guess
import com.example.whereami.domain.model.Round
import com.example.whereami.domain.model.Score
import com.example.whereami.domain.repository.GameRepository
import kotlin.time.Clock
import kotlin.time.Instant

class AdvanceRoundUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(game: Game, finishedRound: Round): Result<Game> {
        return try {
            val guessesResult = gameRepository.getGuessesForRound(finishedRound.id)
            val guesses = guessesResult.getOrDefault(emptyList())

            val updatedScores = game.scoreSheets.map { score ->
                val userGuesses = guesses.filter { it.playerId == score.playerId }
                val totalPoints = userGuesses.sumOf { it.guessScore }
                score.copy(score = score.score + totalPoints, lastUpdated = Clock.System.now())
            }

            val nextIndex = game.currentRoundIndex + 1
            val newStatus = if (nextIndex >= game.settings.nbRound) com.example.whereami.domain.model.GameStatus.FINISHED else com.example.whereami.domain.model.GameStatus.CREATED

            val durationMillis = game.settings.roundDurationMinutes * 60 * 1000
            val newStartTime = Clock.System.now()
            val newEndTime = Instant.fromEpochMilliseconds(newStartTime.toEpochMilliseconds() + durationMillis)

            // Calculate the round-specific scores
            val roundScores = game.playerIds.map { playerId ->
                val userGuesses = guesses.filter { it.playerId == playerId }
                val totalPoints = userGuesses.sumOf { it.guessScore }
                Score(gameId = game.id, playerId = playerId, score = totalPoints, lastUpdated = Clock.System.now())
            }

            val updatedFinishedRound = finishedRound.copy(
                status = com.example.whereami.domain.model.RoundStatus.FINISHED,
                scoreSheets = roundScores
            )

            val nextRound = if (newStatus != com.example.whereami.domain.model.GameStatus.FINISHED) {
                Round(
                    id = java.util.UUID.randomUUID().toString(),
                    gameId = game.id,
                    index = nextIndex,
                    status = com.example.whereami.domain.model.RoundStatus.CREATED,
                    startTime = newStartTime,
                    endTime = newEndTime
                )
            } else {
                null
            }

            val updatedRounds = game.rounds.map { r ->
                if (r.id == finishedRound.id) {
                    updatedFinishedRound
                } else {
                    r
                }
            }.toMutableList()

            if (nextRound != null) {
                updatedRounds.add(nextRound)
            }

            val updatedGame = game.copy(
                scoreSheets = updatedScores,
                currentRoundIndex = nextIndex,
                status = newStatus,
                rounds = updatedRounds
            )

            val saveResult = gameRepository.saveGame(updatedGame)
            if (saveResult.isSuccess) {
                Result.success(updatedGame)
            } else {
                Result.failure(saveResult.exceptionOrNull() ?: Exception("Failed to save game"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
