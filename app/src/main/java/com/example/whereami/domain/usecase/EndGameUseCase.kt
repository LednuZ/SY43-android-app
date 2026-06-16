package com.example.whereami.domain.usecase

class EndGameUseCase {
    suspend operator fun invoke(gameId: String): EndGameResult {
        return EndGameResult.Success
    }
}

sealed class EndGameResult {
    object Success : EndGameResult()
}