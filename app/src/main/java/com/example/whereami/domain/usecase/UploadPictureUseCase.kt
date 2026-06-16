package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.util.LatLng
import com.example.whereami.domain.repository.GameRepository

class UploadPictureUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(roundId: String, publisherId: String, location: LatLng, imageBytes: ByteArray): Result<Unit> {
        return gameRepository.uploadPicture(roundId, publisherId, location, imageBytes)
    }
}
