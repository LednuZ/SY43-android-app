package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.*
import com.example.whereami.domain.repository.GameRepository
import com.example.whereami.domain.repository.UserRepository
import kotlin.time.Clock

class GetRoundDetailsUseCase(
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(gameId: String, roundId: String, currentUserId: String): Result<RoundDetails> {
        return try {
            val gameResult = gameRepository.getGame(gameId)
            if (gameResult.isFailure) return Result.failure(gameResult.exceptionOrNull() ?: Exception("Failed to fetch game"))
            
            val game = gameResult.getOrThrow() ?: return Result.failure(Exception("Game not found"))
            val round = game.rounds.firstOrNull { it.id == roundId } ?: return Result.failure(Exception("Round not found"))
            
            val usersResult = userRepository.getUsers(game.playerIds)
            val users = if (usersResult.isSuccess) usersResult.getOrThrow() else emptyList()
            
            val guessesResult = gameRepository.getGuessesForRound(round.id)
            val guesses = if (guessesResult.isSuccess) guessesResult.getOrThrow() else emptyList()
            
            val pictures = round.posts
            val currentUserHasUploaded = pictures.any { it.publisherId == currentUserId }
            
            val playerBoxes = users.map { user ->
                val userPicture = pictures.find { it.publisherId == user.id }
                val userPictureGuesses = guesses.filter { it.pictureId == userPicture?.id }
                    .sortedByDescending { it.guessedAt }
                    .distinctBy { it.playerId }
                val guesserIds = userPictureGuesses.map { it.playerId }
                val guessers = users.filter { it.id in guesserIds }
                val currentUserHasGuessed = currentUserId in guesserIds
                
                val guessesInfo = userPictureGuesses.mapNotNull { guess ->
                    val guessUser = users.find { it.id == guess.playerId }
                    if (guessUser != null) GuessInfo(guessUser, guess.guessedLocation) else null
                }
                
                val totalExpectedGuesses = if (game.playerIds.size > 1) game.playerIds.size - 1 else 1
                val isRevealed = userPictureGuesses.size >= totalExpectedGuesses
                
                PlayerBox(
                    user = user,
                    hasUploaded = userPicture != null,
                    picture = userPicture,
                    guessers = guessers,
                    guesses = guessesInfo,
                    currentUserHasGuessed = currentUserHasGuessed,
                    isRevealed = isRevealed
                )
            }
            
            val allExpectedGuessed = playerBoxes.all { it.isRevealed }
            val timeIsUp = round.endTime < Clock.System.now()
            
            Result.success(
                RoundDetails(
                    game = game,
                    round = round,
                    playerBoxes = playerBoxes,
                    currentUserHasUploaded = currentUserHasUploaded,
                    allExpectedGuessed = allExpectedGuessed,
                    timeIsUp = timeIsUp
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
