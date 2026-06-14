package com.example.whereami.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whereami.domain.model.Game
import com.example.whereami.domain.usecase.game.GetPastGamesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PastGamesUiState(
    val isLoading: Boolean = false,
    val pastGames: List<Game> = emptyList(),
    val error: String? = null
)

class PastGamesViewModel(
    private val getPastGamesUseCase: GetPastGamesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PastGamesUiState())
    val uiState: StateFlow<PastGamesUiState> = _uiState

    fun loadPastGamesForGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = getPastGamesUseCase(groupId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pastGames = result.getOrThrow()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load past games"
                )
            }
        }
    }

    companion object {
        fun provideFactory(getPastGamesUseCase: GetPastGamesUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PastGamesViewModel(getPastGamesUseCase) as T
                }
            }
    }
}
