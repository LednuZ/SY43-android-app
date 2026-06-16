package com.example.whereami.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.data.repository.SupabaseGameRepository
import com.example.whereami.data.repository.SupabaseGroupRepository
import com.example.whereami.domain.model.GameSettings
import com.example.whereami.domain.usecase.CreateGameResult
import com.example.whereami.domain.usecase.CreateGameUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

data class CreateGameUiState(
    val nbRoundsText: String = "3",
    val durationHoursText: String = "24",
    val isCreating: Boolean = false,
    val createdGameId: String? = null,
    val error: String? = null
)

class CreateGameViewModel(
    private val createGameUseCase: CreateGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGameUiState())
    val uiState: StateFlow<CreateGameUiState> = _uiState.asStateFlow()

    private var groupId: String? = null

    fun initialize(id: String) {
        groupId = id
    }

    fun updateNbRounds(text: String) {
        _uiState.value = _uiState.value.copy(nbRoundsText = text)
    }

    fun updateDurationHours(text: String) {
        _uiState.value = _uiState.value.copy(durationHoursText = text)
    }

    fun createGame() {
        val id = groupId ?: return
        
        val nbRounds = _uiState.value.nbRoundsText.toIntOrNull()
        val durationHours = _uiState.value.durationHoursText.toLongOrNull()
        
        if (nbRounds == null || nbRounds <= 0) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid number of rounds.")
            return
        }
        
        if (durationHours == null || durationHours <= 0) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid duration in hours.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            
            val settings = GameSettings(
                nbRound = nbRounds,
                roundDurationMinutes = durationHours * 60,
                dateBegin = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                dateEnd = Instant.fromEpochMilliseconds(System.currentTimeMillis()).plus((durationHours * nbRounds / 24).toInt().days)
            )
            
            val result = createGameUseCase(id, settings)
            
            when (result) {
                is CreateGameResult.GameCreated -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        createdGameId = result.gameId
                    )
                }
                is CreateGameResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = result.message
                    )
                }
                is CreateGameResult.ActiveGameExists -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = "An active game already exists for this group."
                    )
                }
                is CreateGameResult.GroupNotFound -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = "Group not found."
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val groupRepo = SupabaseGroupRepository(SupabaseProvider.client)
                val gameRepo = SupabaseGameRepository(SupabaseProvider.client)
                return CreateGameViewModel(
                    createGameUseCase = CreateGameUseCase(gameRepo, groupRepo)
                ) as T
            }
        }
    }
}
