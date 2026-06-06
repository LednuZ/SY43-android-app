package com.example.whereami.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.data.repository.SupabaseGameRepository
import com.example.whereami.data.repository.SupabaseGroupRepository
import com.example.whereami.domain.usecase.GetDashboardGamesUseCase
import com.example.whereami.domain.usecase.AdvanceRoundUseCase
import com.example.whereami.domain.usecase.CatchUpExpiredRoundsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.whereami.domain.model.DashboardGame

data class HomeUiState(
    val isLoading: Boolean = true,
    val activeGames: List<DashboardGame> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val getDashboardGamesUseCase: GetDashboardGamesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun fetchActiveGames(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val gamesResult = getDashboardGamesUseCase(userId)
            if (gamesResult.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activeGames = gamesResult.getOrThrow()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = gamesResult.exceptionOrNull()?.message ?: "Failed to fetch games"
                )
            }
        }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val gameRepo = SupabaseGameRepository(SupabaseProvider.client)
                val groupRepo = SupabaseGroupRepository(SupabaseProvider.client)
                val advanceRoundUseCase = AdvanceRoundUseCase(gameRepo)
                val catchUpExpiredRoundsUseCase = CatchUpExpiredRoundsUseCase(gameRepo, advanceRoundUseCase)
                val getDashboardGamesUseCase = GetDashboardGamesUseCase(gameRepo, groupRepo, catchUpExpiredRoundsUseCase)
                return HomeViewModel(getDashboardGamesUseCase) as T
            }
        }
    }
}
