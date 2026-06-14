package com.example.whereami.domain.usecase.game

import com.example.whereami.domain.model.Game
import com.example.whereami.domain.repository.GameRepository

class GetPastGamesUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(groupId: String): Result<List<Game>> {
        return gameRepository.getPastGamesForGroup(groupId)
    }
}
