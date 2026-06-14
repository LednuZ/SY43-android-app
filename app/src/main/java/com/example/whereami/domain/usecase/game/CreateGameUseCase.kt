package com.example.whereami.domain.usecase.game

import com.example.whereami.domain.model.*
import com.example.whereami.domain.repository.GameRepository
import com.example.whereami.domain.repository.GroupRepository
import kotlin.time.Instant

class CreateGameUseCase(
    private val gameRepository: GameRepository,
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: String, settings: GameSettings): CreateGameResult {
        val group = groupRepository.getGroup(groupId).getOrNull() ?: return CreateGameResult.GroupNotFound

        val activeGame = gameRepository.getActiveGame(groupId).getOrNull()
        if (activeGame != null) {
            return CreateGameResult.ActiveGameExists
        }

        val gameId = java.util.UUID.randomUUID().toString()
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())

        val roundDurationMillis = settings.roundDurationMinutes * 60 * 1000
        val roundEndTime = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() + roundDurationMillis)

        val firstRound = Round(
            id = java.util.UUID.randomUUID().toString(),
            gameId = gameId,
            index = 0,
            status = RoundStatus.CREATED,
            startTime = now,
            endTime = roundEndTime
        )
        val rounds = listOf(firstRound)

        val game = Game(
            id = gameId,
            groupId = groupId,
            settings = settings,
            currentRoundIndex = 0,
            status = GameStatus.CREATED,
            rounds = rounds,
            playerIds = group.memberIds,
            scoreSheets = group.memberIds.map { memberId -> 
                Score(
                    gameId = gameId, 
                    playerId = memberId, 
                    score = 0, 
                    lastUpdated = now
                ) 
            }
        )

        val result = gameRepository.createGame(game)
        
        return if (result.isSuccess) {
            CreateGameResult.GameCreated(result.getOrThrow())
        } else {
            CreateGameResult.Error(result.exceptionOrNull()?.message ?: "Unknown database error")
        }
    }
}

sealed class CreateGameResult {
    data class GameCreated(val gameId: String) : CreateGameResult()
    data class Error(val message: String) : CreateGameResult()
    data object ActiveGameExists : CreateGameResult()
    data object GroupNotFound : CreateGameResult()
}
