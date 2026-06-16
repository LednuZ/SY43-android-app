package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.DashboardGame
import com.example.whereami.domain.repository.GameRepository
import com.example.whereami.domain.repository.GroupRepository

class GetDashboardGamesUseCase(
    private val gameRepository: GameRepository,
    private val groupRepository: GroupRepository,
    private val catchUpExpiredRoundsUseCase: CatchUpExpiredRoundsUseCase
) {
    suspend operator fun invoke(userId: String): Result<List<DashboardGame>> {
        return try {
            val gamesResult = gameRepository.getActiveGamesForUser(userId)
            if (gamesResult.isFailure) return Result.failure(gamesResult.exceptionOrNull() ?: Exception("Failed to fetch games"))
            
            val games = gamesResult.getOrThrow()
            
            val dashboardGames = games.mapNotNull { game ->
                val updatedGame = catchUpExpiredRoundsUseCase(game)
                
                val groupResult = groupRepository.getGroup(updatedGame.groupId)
                val groupName = if (groupResult.isSuccess) groupResult.getOrThrow()?.name ?: "Unknown Group" else "Unknown Group"
                
                val currentRound = updatedGame.rounds.firstOrNull { it.index == updatedGame.currentRoundIndex }
                if (currentRound == null) return@mapNotNull null
                
                val guessesResult = gameRepository.getGuessesForRound(currentRound.id)
                val guesses = if (guessesResult.isSuccess) guessesResult.getOrThrow() else emptyList()
                
                val userUploadedPicture = currentRound.posts.any { it.publisherId == userId }
                val needsUpload = !userUploadedPicture
                
                val picturesToGuessCount = currentRound.posts.count { post ->
                    post.publisherId != userId && !guesses.any { it.playerId == userId && it.pictureId == post.id }
                }
                
                DashboardGame(
                    gameId = updatedGame.id,
                    groupId = updatedGame.groupId,
                    groupName = groupName,
                    currentRoundId = currentRound.id,
                    roundEndTime = currentRound.endTime,
                    needsUpload = needsUpload,
                    picturesToGuessCount = picturesToGuessCount
                )
            }
            Result.success(dashboardGames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
