package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.*
import com.example.whereami.domain.model.util.LatLng
import com.example.whereami.domain.repository.GameRepository

import kotlin.time.Instant

class PostPictureUseCase(private val gameRepository: GameRepository) {
    suspend operator fun invoke(
        gameId: String,
        userId: String,
        imageUrl: String,
        location: LatLng,
        description: String?
    ): PostPictureResult {

        val game = gameRepository.getGame(gameId).getOrThrow() ?: return PostPictureResult.GameNotFound

        if (game.status != GameStatus.PLAYING) {
            return PostPictureResult.GameNotActive
        }

        val round = game.getCurrentRound() ?: return PostPictureResult.RoundNotFound
        if (round.status != RoundStatus.PLAYING) {
            return PostPictureResult.RoundNotAcceptingPosts
        }

        if (!game.playerIds.contains(userId)) {
            return PostPictureResult.UserNotMember
        }

        if (round.posts.filter { it.publisherId == userId } != emptyList<Picture>() ) {
            return PostPictureResult.AlreadyPosted
        }

        val pictureId = "post_${java.lang.System.currentTimeMillis()}_${userId}"
        val post = Picture(
            id = pictureId,
            roundId = round.id,
            publisherId = userId,
            imageUrl = imageUrl,
            location = location,
            description = description,
            createdAt = kotlin.time.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        )

        val updatedRound = round.copy(
            posts = (round.posts + post).toMutableList()
        )

        val updatedGame = game.updateRoundInGame(updatedRound)
        gameRepository.saveGame(updatedGame).getOrThrow()

        return PostPictureResult.PostCreated(pictureId)
    }
}

sealed class PostPictureResult {
    object Success : PostPictureResult()
    data class PostCreated(val postId: String) : PostPictureResult()
    object GameNotFound : PostPictureResult()
    object UserNotMember : PostPictureResult()
    object RoundNotFound : PostPictureResult()
    object GameNotActive : PostPictureResult()
    object RoundNotAcceptingPosts : PostPictureResult()
    object AlreadyPosted : PostPictureResult()
}