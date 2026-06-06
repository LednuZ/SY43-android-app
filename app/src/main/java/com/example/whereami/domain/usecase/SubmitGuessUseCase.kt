package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.Guess
import com.example.whereami.domain.model.Picture
import com.example.whereami.domain.repository.GameRepository
import kotlin.time.Clock

class SubmitGuessUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(
        roundId: String,
        playerId: String,
        picture: Picture,
        guessLatitude: Double,
        guessLongitude: Double
    ): Result<Guess> {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            picture.location.latitude, picture.location.longitude,
            guessLatitude, guessLongitude,
            results
        )
        val distanceInMeters = results[0].toDouble()
        val guessScore = kotlin.math.max(0, (5000.0 * kotlin.math.exp(-distanceInMeters / 2000.0)).toInt())
        
        val guess = Guess(
            id = java.util.UUID.randomUUID().toString(),
            roundId = roundId,
            playerId = playerId,
            pictureId = picture.id,
            guessedLocation = com.example.whereami.domain.model.util.LatLng(guessLatitude, guessLongitude),
            guessedAt = Clock.System.now(),
            distanceMeters = distanceInMeters,
            guessScore = guessScore
        )
        
        val result = gameRepository.submitGuess(guess)
        return if (result.isSuccess) {
            Result.success(guess)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to submit guess"))
        }
    }
}