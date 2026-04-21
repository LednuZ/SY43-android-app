package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.*
import com.example.whereami.domain.repository.GameRepository
import com.example.whereami.domain.repository.GroupRepository
import com.google.firebase.Timestamp

class CreateGameUseCase(private val gameRepository: GameRepository, private val groupRepository: GroupRepository) {
    suspend operator fun invoke(groupId: String, settings: GameSettings): CreateGameResult {
        val group = groupRepository.getGroup(groupId).getOrThrow() ?: return CreateGameResult.GroupNotFound

        val activeGame = gameRepository.getActiveGame(groupId).getOrThrow()
        if (activeGame != null) {
            return CreateGameResult.ActiveGameExists
        }

        val gameId = "game_${System.currentTimeMillis()}"
        val game = Game(
            id = gameId,
            groupId = groupId,
            settings = settings,
            currentRound = null,
            status = GameStatus.CREATED,
            scoreSheets = group.memberIds.map { memberId -> Score(memberId, 0, Timestamp.now()) }
        )

        gameRepository.createGame(game).getOrThrow()
        return CreateGameResult.GameCreated(gameId)
    }
}

sealed class CreateGameResult {
    data class GameCreated(val gameId: String) : CreateGameResult()
    object ActiveGameExists : CreateGameResult()
    object GroupNotFound : CreateGameResult()
}