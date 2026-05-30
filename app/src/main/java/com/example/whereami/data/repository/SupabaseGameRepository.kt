package com.example.whereami.data.repository

import com.example.whereami.data.dto.*
import com.example.whereami.domain.model.*
import com.example.whereami.domain.model.util.LatLng
import com.example.whereami.domain.repository.GameRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.datetime.Instant

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
            
            // Upsert scores
            val scoreDtos = game.scoreSheets.map { it.toDto(game.id) }
            if (scoreDtos.isNotEmpty()) {
                client.from("game_scores").upsert(scoreDtos)
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

    override suspend fun createGame(game: Game): Result<String> {
        return runCatching {
            val dto = game.toDto()
            val result = client.from("games").insert(dto) {
                select()
            }.decodeSingle<GameDto>()
            
            val gameId = result.id ?: throw Exception("Failed to get created game id")
            
            val scoreDtos = game.scoreSheets.map { it.toDto(gameId) }
            if (scoreDtos.isNotEmpty()) {
                client.from("game_scores").insert(scoreDtos)
            }
            
            gameId
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
            listRounds = rounds.toMutableList(),
            listPlayers = scores.map { it.userId }.toMutableList(),
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
            status = status.name
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
            posts = pictures.toMutableList()
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
            userId = user_id,
            totalScore = total_score,
            lastUpdated = Instant.fromEpochMilliseconds(0) // We can add an updated_at to the schema later
        )
    }
    
    private fun Score.toDto(gameId: String): GameScoreDto {
        return GameScoreDto(
            game_id = gameId,
            user_id = userId,
            total_score = totalScore
        )
    }
}
