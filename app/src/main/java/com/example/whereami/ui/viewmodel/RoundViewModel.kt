package com.example.whereami.ui.viewmodel

import com.example.whereami.util.AppError
import com.example.whereami.util.toAppError

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whereami.domain.usecase.AdvanceRoundUseCase
import com.example.whereami.domain.usecase.SubmitGuessUseCase
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.data.repository.SupabaseGameRepository
import com.example.whereami.data.repository.SupabaseUserRepository
import com.example.whereami.domain.model.Game
import com.example.whereami.domain.model.Guess
import com.example.whereami.domain.model.Picture
import com.example.whereami.domain.model.Round
import com.example.whereami.domain.model.User
import com.example.whereami.domain.model.util.LatLng
import com.example.whereami.domain.repository.GameRepository
import com.example.whereami.domain.repository.UserRepository
import com.example.whereami.domain.usecase.GetRoundDetailsUseCase
import com.example.whereami.domain.usecase.UploadPictureUseCase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock

import com.example.whereami.domain.model.PlayerBox
import com.example.whereami.domain.model.GuessInfo

data class RoundUiState(
    val isLoading: Boolean = true,
    val isUploadingPicture: Boolean = false,
    val isSubmittingGuess: Boolean = false,
    val game: Game? = null,
    val round: Round? = null,
    val playerBoxes: List<PlayerBox> = emptyList(),
    val currentUserHasUploaded: Boolean = false,
    val currentUserId: String? = null,
    val error: AppError? = null
)

class RoundViewModel(
    private val getRoundDetailsUseCase: GetRoundDetailsUseCase,
    private val advanceRoundUseCase: AdvanceRoundUseCase,
    private val uploadPictureUseCase: UploadPictureUseCase,
    private val submitGuessUseCase: SubmitGuessUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoundUiState())
    val uiState: StateFlow<RoundUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null
    private var initializedGameId: String? = null
    private var initializedRoundId: String? = null
    private var isAdvancingRound: Boolean = false

    fun initialize(gameId: String, roundId: String) {
        if (initializedGameId == gameId && initializedRoundId == roundId) return
        initializedGameId = gameId
        initializedRoundId = roundId
        currentUserId = SupabaseProvider.client.auth.currentUserOrNull()?.id
        fetchRoundData(gameId, roundId)
    }

    private fun fetchRoundData(gameId: String, roundId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = getRoundDetailsUseCase(gameId, roundId, userId)
            if (result.isSuccess) {
                val details = result.getOrThrow()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    game = details.game,
                    round = details.round,
                    playerBoxes = details.playerBoxes,
                    currentUserHasUploaded = details.currentUserHasUploaded,
                    currentUserId = currentUserId
                )
                
                if ((details.allExpectedGuessed || details.timeIsUp) && details.round.status != com.example.whereami.domain.model.RoundStatus.FINISHED && !isAdvancingRound && details.round.index == details.game.currentRoundIndex) {
                    // For AdvanceRoundUseCase, we need the guesses for the round
                    // To avoid a circular dependency or complicating the UseCase further, we extract the raw guesses from playerBoxes
                    val roundGuesses = details.playerBoxes.flatMap { pb -> 
                        pb.guesses.map { g -> 
                            Guess("", details.round.id, g.user.id, pb.picture?.id ?: "", g.guessLocation, Clock.System.now(), 0.0, 0) 
                        } 
                    }
                    advanceRound(details.game, details.round, roundGuesses)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.toAppError() ?: AppError.Unknown("Failed to fetch round")
                )
            }
        }
    }
    
    private fun advanceRound(game: Game, round: Round, guesses: List<Guess>) {
        isAdvancingRound = true
        viewModelScope.launch {
            advanceRoundUseCase(game, round, guesses)
            isAdvancingRound = false
            
            initializedGameId?.let { gid -> initializedRoundId?.let { rid -> fetchRoundData(gid, rid) } }
        }
    }

    fun uploadPicture(location: LatLng, imageBytes: ByteArray) {
        val roundId = _uiState.value.round?.id ?: return
        val userId = currentUserId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingPicture = true)
            val result = uploadPictureUseCase(roundId, userId, location, imageBytes)
            _uiState.value = _uiState.value.copy(isUploadingPicture = false)
            if (result.isSuccess) {
                initializedGameId?.let { gid -> initializedRoundId?.let { rid -> fetchRoundData(gid, rid) } }
            } else {
                val ex = result.exceptionOrNull()
                _uiState.value = _uiState.value.copy(error = ex?.toAppError() ?: AppError.Unknown("Failed to upload picture"))
                ex?.printStackTrace()
            }
        }
    }

    fun submitGuess(pictureId: String, location: LatLng) {
        val roundId = _uiState.value.round?.id ?: return
        val round = _uiState.value.round ?: return
        val userId = currentUserId ?: return
        val picture = round.posts.find { it.id == pictureId } ?: return
        
        val playerBox = _uiState.value.playerBoxes.find { it.picture?.id == pictureId }
        if (playerBox?.currentUserHasGuessed == true) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingGuess = true)
            
            val result = submitGuessUseCase(
                roundId = round.id,
                playerId = userId,
                picture = picture,
                guessLatitude = location.latitude,
                guessLongitude = location.longitude
            )
            
            _uiState.value = _uiState.value.copy(isSubmittingGuess = false)
            
            if (result.isSuccess) {
                initializedGameId?.let { gid -> initializedRoundId?.let { rid -> fetchRoundData(gid, rid) } }
            } else {
                val ex = result.exceptionOrNull()
                _uiState.value = _uiState.value.copy(error = ex?.toAppError() ?: AppError.Unknown("Failed to submit guess"))
                ex?.printStackTrace()
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(error = AppError.Unknown(message))
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val gameRepo = SupabaseGameRepository(SupabaseProvider.client)
                val userRepo = SupabaseUserRepository(SupabaseProvider.client)
                val advanceRoundUseCase = AdvanceRoundUseCase(gameRepo)
                val getRoundDetailsUseCase = GetRoundDetailsUseCase(gameRepo, userRepo)
                val uploadPictureUseCase = UploadPictureUseCase(gameRepo)
                val submitGuessUseCase = SubmitGuessUseCase(gameRepo)
                return RoundViewModel(getRoundDetailsUseCase, advanceRoundUseCase, uploadPictureUseCase, submitGuessUseCase) as T
            }
        }
    }
}
