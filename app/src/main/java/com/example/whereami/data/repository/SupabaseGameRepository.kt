package com.example.whereami.data.repository

import com.example.whereami.data.dto.*
import com.example.whereami.domain.model.*
import com.example.whereami.domain.model.util.LatLng
import com.example.whereami.domain.repository.GameRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlin.time.Instant
import java.util.UUID

class SupabaseGameRepository(private val client: SupabaseClient) : GameRepository {

    override suspend fun getGame(gameId: String): Result<Game?> {
        return runCatching {
            val gameDto = client.from("games").select {
                filter { eq("id", gameId) }
            }.decodeSingleOrNull<GameDto>() ?: return@runCatching null

            val rounds = getRoundsForGame(gameId)
            val scores = getScoresForGame(gameId)
            gameDto.toDomain(rounds, scores)
        }
    }

    override suspend fun saveGame(game: Game): Result<Unit> {
        return runCatching {
            val dto = game.toDto()
            client.from("games").update(dto) {
                filter { eq("id", game.id) }
            }

            val scoreDtos = game.scoreSheets.map { it.toDto(game.id) }
            if (scoreDtos.isNotEmpty()) {
                client.from("game_scores").upsert(scoreDtos)
            }

            val roundDtos = game.rounds.map { it.toDto() }
            if (roundDtos.isNotEmpty()) {
                client.from("rounds").upsert(roundDtos)
            }
        }
    }

    override suspend fun getActiveGame(groupId: String): Result<Game?> {
        return runCatching {
            val gameDto = client.from("games").select {
                filter {
                    eq("group_id", groupId)
                    neq("status", GameStatus.FINISHED.name)
                }
            }.decodeSingleOrNull<GameDto>() ?: return@runCatching null

            val rounds = getRoundsForGame(gameDto.id!!)
            val scores = getScoresForGame(gameDto.id)
            gameDto.toDomain(rounds, scores)
        }
    }

    override suspend fun getActiveGamesForUser(userId: String): Result<List<Game>> {
        return runCatching {
            val scoresDtos = client.from("game_scores").select {
                filter {
                    eq("player_id", userId)
                }
            }.decodeList<GameScoreDto>()
            
            val gameIds = scoresDtos.map { it.game_id }.distinct()
            if (gameIds.isEmpty()) return@runCatching emptyList()

            val gamesDtos = client.from("games").select {
                filter {
                    isIn("id", gameIds)
                    neq("status", GameStatus.FINISHED.name)
                }
            }.decodeList<GameDto>()

            gamesDtos.map { dto ->
                val rounds = getRoundsForGame(dto.id!!)
                val scores = getScoresForGame(dto.id)
                dto.toDomain(rounds, scores)
            }
        }
    }

    override suspend fun createGame(game: Game): Result<String> {
        return runCatching {
            val dto = game.toDto()
            client.from("games").insert(dto)
            
            val roundDtos = game.rounds.map { it.toDto() }
            if (roundDtos.isNotEmpty()) {
                client.from("rounds").insert(roundDtos)
            }
            
            val scoreDtos = game.scoreSheets.map { it.toDto(game.id) }
            if (scoreDtos.isNotEmpty()) {
                client.from("game_scores").insert(scoreDtos)
            }
            
            game.id
        }
    }

    override suspend fun submitGuess(guess: Guess): Result<Unit> {
        return runCatching {
            val dto = GuessDto(
                id = if (guess.id.isEmpty()) UUID.randomUUID().toString() else guess.id,
                round_id = guess.roundId,
                player_id = guess.playerId,
                picture_id = guess.pictureId,
                latitude = guess.guessedLocation.latitude,
                longitude = guess.guessedLocation.longitude,
                guessed_at = guess.guessedAt.toString(),
                distance_meters = guess.distanceMeters,
                guess_score = guess.guessScore
            )
            client.from("guesses").insert(dto)
        }
    }

    override suspend fun getGuessesForRound(roundId: String): Result<List<Guess>> {
        return runCatching {
            client.from("guesses").select {
                filter { eq("round_id", roundId) }
            }.decodeList<GuessDto>().map { it.toDomain() }
        }
    }

    override suspend fun uploadPicture(roundId: String, publisherId: String, location: LatLng, imageBytes: ByteArray): Result<Unit> {
        return runCatching {
            val fileName = "${UUID.randomUUID()}.jpg"
            val bucket = client.storage.from("pictures")
            bucket.upload(fileName, imageBytes)
            val publicUrl = bucket.publicUrl(fileName)
            
            val pictureDto = PictureDto(
                id = UUID.randomUUID().toString(),
                round_id = roundId,
                publisher_id = publisherId,
                image_url = publicUrl,
                latitude = location.latitude,
                longitude = location.longitude,
                created_at = io.ktor.util.date.getTimeMillis().let { Instant.fromEpochMilliseconds(it).toString() }
            )
            client.from("pictures").insert(pictureDto)
        }
    }

    private suspend fun getRoundsForGame(gameId: String): List<Round> {
        val roundDtos = client.from("rounds").select {
            filter { eq("game_id", gameId) }
        }.decodeList<RoundDto>()

        return roundDtos.map { roundDto ->
            val pictures = getPicturesForRound(roundDto.id!!)
            roundDto.toDomain(pictures)
        }
    }

    private suspend fun getPicturesForRound(roundId: String): List<Picture> {
        return client.from("pictures").select {
            filter { eq("round_id", roundId) }
        }.decodeList<PictureDto>().map { it.toDomain() }
    }
    
    private suspend fun getScoresForGame(gameId: String): List<Score> {
        return client.from("game_scores").select {
            filter { eq("game_id", gameId) }
        }.decodeList<GameScoreDto>().map { it.toDomain() }
    }

    private fun GameDto.toDomain(rounds: List<Round>, scores: List<Score>): Game {
        return Game(
            id = id ?: "",
            groupId = group_id,
            settings = GameSettings(
                nbRound = nb_rounds,
                roundDurationMinutes = round_duration_minutes,
                dateBegin = Instant.parse(date_begin),
                dateEnd = Instant.parse(date_end)
            ),
            currentRoundIndex = current_round_index,
            rounds = rounds,
            playerIds = scores.map { it.playerId },
            status = GameStatus.valueOf(status),
            scoreSheets = scores
        )
    }

    private fun Game.toDto(): GameDto {
        return GameDto(
            id = if (id.isEmpty()) null else id,
            group_id = groupId,
            nb_rounds = settings.nbRound,
            round_duration_minutes = settings.roundDurationMinutes,
            date_begin = settings.dateBegin.toString(),
            date_end = settings.dateEnd.toString(),
            current_round_index = currentRoundIndex,
            status = status.name
        )
    }

    private fun Round.toDto(): RoundDto {
        return RoundDto(
            id = if (id.isEmpty()) null else id,
            game_id = gameId,
            index = index,
            status = status.name,
            start_time = startTime.toString(),
            end_time = endTime.toString()
        )
    }

    private fun Picture.toDto(): PictureDto {
        return PictureDto(
            id = if (id.isEmpty()) null else id,
            round_id = roundId,
            publisher_id = publisherId,
            image_url = imageUrl,
            latitude = location.latitude,
            longitude = location.longitude,
            description = description,
            created_at = createdAt.toString(),
            revealed_at = reaveledAt?.toString()
        )
    }

    private fun RoundDto.toDomain(pictures: List<Picture>): Round {
        return Round(
            id = id ?: "",
            gameId = game_id,
            index = index,
            status = RoundStatus.valueOf(status),
            posts = pictures.toMutableList(),
            startTime = Instant.parse(start_time),
            endTime = Instant.parse(end_time)
        )
    }

    private fun PictureDto.toDomain(): Picture {
        return Picture(
            id = id ?: "",
            roundId = round_id,
            publisherId = publisher_id,
            imageUrl = image_url,
            location = LatLng(latitude, longitude),
            description = description,
            createdAt = Instant.parse(created_at),
            reaveledAt = revealed_at?.let { Instant.parse(it) }
        )
    }
    
    private fun GameScoreDto.toDomain(): Score {
        return Score(
            gameId = game_id,
            playerId = player_id,
            score = score,
            lastUpdated = date_last_update?.let { Instant.parse(it) } ?: Instant.fromEpochMilliseconds(0)
        )
    }
    
    private fun Score.toDto(gameId: String): GameScoreDto {
        return GameScoreDto(
            game_id = gameId,
            player_id = playerId,
            score = score,
            date_last_update = lastUpdated.toString()
        )
    }

    private fun GuessDto.toDomain(): Guess {
        return Guess(
            id = id ?: "",
            roundId = round_id,
            playerId = player_id,
            pictureId = picture_id,
            guessedLocation = LatLng(latitude, longitude),
            guessedAt = Instant.parse(guessed_at),
            distanceMeters = distance_meters,
            guessScore = guess_score
        )
    }
}
