package com.example.whereami

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.whereami.domain.model.*
import com.example.whereami.domain.model.util.LatLng
import com.example.whereami.domain.repository.GameRepository
import com.example.whereami.domain.repository.GroupRepository
import com.example.whereami.domain.usecase.game.CreateGameUseCase
import com.example.whereami.domain.usecase.game.CreateGameResult
import com.example.whereami.domain.usecase.round.AdvanceRoundUseCase
import com.example.whereami.domain.usecase.round.SubmitGuessUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Clock
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
class GameLogicTest {

    private class MockGroupRepository : GroupRepository {
        val groups = mutableMapOf<String, Group>()

        override suspend fun getGroup(groupId: String): Result<Group?> {
            return Result.success(groups[groupId])
        }

        override suspend fun createGroup(group: Group): Result<String> {
            groups[group.id] = group
            return Result.success(group.id)
        }

        override suspend fun addMember(groupId: String, userId: String): Result<Unit> {
            groups[groupId]?.memberIds?.add(userId)
            return Result.success(Unit)
        }

        override suspend fun removeMember(groupId: String, userId: String): Result<Unit> {
            groups[groupId]?.memberIds?.remove(userId)
            return Result.success(Unit)
        }

        override suspend fun getGroupsForUser(userId: String): Result<List<Group>> {
            return Result.success(groups.values.filter { userId in it.memberIds })
        }
    }

    private class MockGameRepository : GameRepository {
        var activeGame: Game? = null
        val guesses = mutableListOf<Guess>()

        override suspend fun getGame(gameId: String): Result<Game?> {
            return Result.success(activeGame)
        }

        override suspend fun saveGame(game: Game): Result<Unit> {
            activeGame = game
            return Result.success(Unit)
        }

        override suspend fun getActiveGame(groupId: String): Result<Game?> {
            return Result.success(activeGame)
        }

        override suspend fun getActiveGamesForUser(userId: String): Result<List<Game>> {
            return Result.success(activeGame?.let { listOf(it) } ?: emptyList())
        }

        override suspend fun getPastGamesForGroup(groupId: String): Result<List<Game>> {
            return Result.success(emptyList())
        }

        override suspend fun createGame(game: Game): Result<String> {
            activeGame = game
            return Result.success(game.id)
        }

        override suspend fun submitGuess(guess: Guess): Result<Unit> {
            guesses.add(guess)
            return Result.success(Unit)
        }

        override suspend fun getGuessesForRound(roundId: String): Result<List<Guess>> {
            return Result.success(guesses.filter { it.roundId == roundId })
        }

        override suspend fun uploadPicture(
            roundId: String,
            publisherId: String,
            location: LatLng,
            imageBytes: ByteArray
        ): Result<Unit> {
            val game = activeGame ?: return Result.failure(Exception("No active game"))
            val roundIndex = game.rounds.indexOfFirst { it.id == roundId }
            if (roundIndex != -1) {
                val round = game.rounds[roundIndex]
                val newPicture = Picture(
                    id = java.util.UUID.randomUUID().toString(),
                    roundId = roundId,
                    publisherId = publisherId,
                    imageUrl = "http://mockurl.com/img.jpg",
                    location = location,
                    description = "Mock description",
                    createdAt = Clock.System.now()
                )
                round.posts.add(newPicture)
            }
            return Result.success(Unit)
        }
    }

    @Test
    fun testWholeGameLogicFlow() = runBlocking {
        val groupRepo = MockGroupRepository()
        val gameRepo = MockGameRepository()

        val groupId = "test-group-1"
        val playerA = "player-a"
        val playerB = "player-b"
        val group = Group(
            id = groupId,
            name = "Test Group",
            memberIds = mutableListOf(playerA, playerB)
        )
        groupRepo.createGroup(group)

        val createGameUseCase = CreateGameUseCase(gameRepo, groupRepo)
        val submitGuessUseCase = SubmitGuessUseCase(gameRepo)
        val advanceRoundUseCase = AdvanceRoundUseCase(gameRepo)

        // 1. Create Game
        val settings = GameSettings(
            nbRound = 2,
            roundDurationMinutes = 120,
            dateBegin = Clock.System.now(),
            dateEnd = Clock.System.now()
        )
        val createResult = createGameUseCase(groupId, settings)
        assertTrue(createResult is CreateGameResult.GameCreated)
        val gameId = (createResult as CreateGameResult.GameCreated).gameId

        val game = gameRepo.activeGame
        assertNotNull(game)
        assertEquals(1, game!!.rounds.size) // Only 1 round created initially
        val round0 = game.rounds[0]
        assertEquals(0, round0.index)
        assertEquals(RoundStatus.CREATED, round0.status)

        // Verify start and end time of Round 0 (duration is 120 minutes)
        val durationMillis = 120L * 60 * 1000
        val expectedEndTime = round0.startTime.toEpochMilliseconds() + durationMillis
        assertEquals(expectedEndTime, round0.endTime.toEpochMilliseconds())

        // 2. Upload pictures for Round 0
        val locationA = LatLng(48.8584, 2.2945) // Eiffel Tower
        val locationB = LatLng(48.8606, 2.3376) // Louvre Museum
        gameRepo.uploadPicture(round0.id, playerA, locationA, ByteArray(0))
        gameRepo.uploadPicture(round0.id, playerB, locationB, ByteArray(0))

        assertEquals(2, round0.posts.size)
        val picA = round0.posts.find { it.publisherId == playerA }!!
        val picB = round0.posts.find { it.publisherId == playerB }!!

        // 3. Submit guesses (Player A guesses B's picture, Player B guesses A's picture)
        // Let's guess close for A: Eiffel Tower (exact location) -> 5000 points
        val guessAResult = submitGuessUseCase(round0.id, playerA, picB, picB.location.latitude, picB.location.longitude)
        assertTrue(guessAResult.isSuccess)
        val guessA = guessAResult.getOrThrow()
        assertEquals(5000, guessA.guessScore)

        // Guess slightly far for B: guess is 10km away
        val latShift = 48.8584 + 0.09
        val guessBResult = submitGuessUseCase(round0.id, playerB, picA, latShift, picA.location.longitude)
        assertTrue(guessBResult.isSuccess)
        val guessB = guessBResult.getOrThrow()
        val expectedScore = (5000.0 * kotlin.math.exp(-guessB.distanceMeters / 100000.0)).toInt()
        assertEquals(expectedScore, guessB.guessScore)
        // Check if score is over 4000 (distance is ~10km, so it should be around 4500 points)
        assertTrue(guessB.guessScore > 4000)

        // 4. Advance Round
        val advanceResult = advanceRoundUseCase(game, round0)
        assertTrue(advanceResult.isSuccess)
        val updatedGame = advanceResult.getOrThrow()

        // Check that Round 0 is marked finished and next index is 1
        assertEquals(1, updatedGame.currentRoundIndex)
        val finishedRound0 = updatedGame.rounds.find { it.index == 0 }!!
        assertEquals(RoundStatus.FINISHED, finishedRound0.status)

        // Check round score sheets are populated
        assertEquals(2, finishedRound0.scoreSheets.size)
        val scoreSheetA = finishedRound0.scoreSheets.find { it.playerId == playerA }!!
        assertEquals(5000, scoreSheetA.score)
        val scoreSheetB = finishedRound0.scoreSheets.find { it.playerId == playerB }!!
        assertEquals(guessB.guessScore, scoreSheetB.score)

        // Check that game scores are updated
        val gameScoreSheetA = updatedGame.scoreSheets.find { it.playerId == playerA }!!
        assertEquals(5000, gameScoreSheetA.score)
        val gameScoreSheetB = updatedGame.scoreSheets.find { it.playerId == playerB }!!
        assertEquals(guessB.guessScore, gameScoreSheetB.score)

        // Check next round (Round 1) was dynamically created
        assertEquals(2, updatedGame.rounds.size)
        val round1 = updatedGame.rounds.find { it.index == 1 }!!
        assertEquals(RoundStatus.CREATED, round1.status)
        val expectedRound1EndTime = round1.startTime.toEpochMilliseconds() + durationMillis
        assertEquals(expectedRound1EndTime, round1.endTime.toEpochMilliseconds())
    }
}
