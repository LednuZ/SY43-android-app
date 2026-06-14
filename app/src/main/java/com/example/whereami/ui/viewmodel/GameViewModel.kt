package com.example.whereami.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whereami.domain.usecase.round.AdvanceRoundUseCase
import com.example.whereami.domain.usecase.round.CatchUpExpiredRoundsUseCase
import com.example.whereami.domain.usecase.game.GetGameDetailsUseCase
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.data.repository.SupabaseGameRepository
import com.example.whereami.data.repository.SupabaseUserRepository
import com.example.whereami.domain.model.Game
import com.example.whereami.domain.model.Round
import com.example.whereami.domain.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.whereami.util.AppError
import com.example.whereami.util.toAppError

data class GameDetailsUiState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val currentRounds: List<Round> = emptyList(),
    val pastRounds: List<Round> = emptyList(),
    val playerUsernames: Map<String, String> = emptyMap(),
    val error: AppError? = null
)

class GameViewModel(
    private val getGameDetailsUseCase: GetGameDetailsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailsUiState())
    val uiState: StateFlow<GameDetailsUiState> = _uiState.asStateFlow()

    fun initialize(gameId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = getGameDetailsUseCase(gameId)
            if (result.isSuccess) {
                val details = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    game = details.game,
                    currentRounds = details.currentRounds,
                    pastRounds = details.pastRounds,
                    playerUsernames = details.playerUsernames
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.toAppError() ?: AppError.Unknown("Failed to fetch game")
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val gameRepo = SupabaseGameRepository(SupabaseProvider.client)
                val userRepo = SupabaseUserRepository(SupabaseProvider.client)
                val advanceRoundUseCase = AdvanceRoundUseCase(gameRepo)
                val catchUpExpiredRoundsUseCase = CatchUpExpiredRoundsUseCase(gameRepo, advanceRoundUseCase)
                val getGameDetailsUseCase = GetGameDetailsUseCase(gameRepo, userRepo, catchUpExpiredRoundsUseCase)
                return GameViewModel(getGameDetailsUseCase) as T
            }
        }
    }
}
