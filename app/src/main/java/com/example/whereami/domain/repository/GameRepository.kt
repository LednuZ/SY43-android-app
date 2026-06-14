package com.example.whereami.domain.repository

import com.example.whereami.domain.model.*
import com.example.whereami.domain.model.util.LatLng

interface GameRepository {
    suspend fun getGame(gameId: String): Result<Game?>
    suspend fun saveGame(game: Game): Result<Unit>
    suspend fun getActiveGame(groupId: String): Result<Game?>
    suspend fun getActiveGamesForUser(userId: String): Result<List<Game>>
    suspend fun getPastGamesForGroup(groupId: String): Result<List<Game>>
    suspend fun createGame(game: Game): Result<String>
    suspend fun submitGuess(guess: Guess): Result<Unit>
    suspend fun getGuessesForRound(roundId: String): Result<List<Guess>>
    suspend fun uploadPicture(roundId: String, publisherId: String, location: LatLng, imageBytes: ByteArray): Result<Unit>
}