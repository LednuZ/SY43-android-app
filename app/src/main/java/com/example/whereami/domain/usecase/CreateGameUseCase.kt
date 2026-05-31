package com.example.whereami.domain.usecase

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

        val game = Game(
            id = "",
            groupId = groupId,
            settings = settings,
            currentRoundIndex = 0,
            status = GameStatus.CREATED,
            scoreSheets = group.memberIds.map { memberId -> 
                Score(
                    gameId = "", 
                    playerId = memberId, 
                    score = 0, 
                    lastUpdated = Instant.fromEpochMilliseconds(System.currentTimeMillis())
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