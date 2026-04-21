package com.example.whereami.domain.repository

import com.example.whereami.domain.model.*

interface GameRepository {
    suspend fun getGame(gameId: String): Result<Game?>
    suspend fun saveGame(game: Game): Result<Unit>
    suspend fun getActiveGame(groupId: String): Result<Game?>
    suspend fun createGame(game: Game): Result<String>
    suspend fun createRound(round: Round): Result<String>
    suspend fun getCurrentRound(game: Game): Result<Round?>
}