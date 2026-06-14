package com.example.whereami.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.data.repository.SupabaseGameRepository
import com.example.whereami.data.repository.SupabaseUserRepository
import com.example.whereami.domain.repository.UserRepository
import com.example.whereami.domain.model.User
import com.example.whereami.domain.repository.GameRepository
import com.example.whereami.domain.usecase.user.DeleteAccountUseCase
import com.example.whereami.domain.usecase.user.UpdateProfileUseCase
import com.example.whereami.util.AppError
import com.example.whereami.util.toAppError
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val currentUserData: User? = null,
    val successMessage: String? = null,
    val isDeleted: Boolean = false,
    val error: AppError? = null
)

class AccountViewModel(
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    fun initialize(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = userRepository.getUser(userId)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUserData = user
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.toAppError() ?: AppError.Unknown("Failed to load user")
                )
            }
        }
    }

    fun updateProfile(username: String, firstName: String, lastName: String, phoneNumber: String) {
        val user = _uiState.value.currentUserData ?: return
        if (username.isBlank()) {
            _uiState.value = _uiState.value.copy(error = AppError.Unknown("Username cannot be empty"))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, successMessage = null)
            
            val updatedUser = user.copy(
                username = username,
                firstName = firstName.takeIf { it.isNotBlank() },
                lastName = lastName.takeIf { it.isNotBlank() },
                phoneNumber = phoneNumber.takeIf { it.isNotBlank() }
            )
            
            val result = updateProfileUseCase(updatedUser)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    currentUserData = updatedUser,
                    successMessage = "Profile updated successfully!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = result.exceptionOrNull()?.toAppError() ?: AppError.Unknown("Failed to update profile")
                )
            }
        }
    }

    fun deleteAccount() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            val gamesResult = gameRepository.getActiveGamesForUser(userId)
            if (!gamesResult.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = gamesResult.exceptionOrNull()?.toAppError() ?: AppError.Unknown("Failed to fetch games")
                )
            } else {
                if (gamesResult.getOrDefault(emptyList()).isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = AppError.Unknown("Account deletion is impossible as your are participating in an active game")
                    )
                } else {
                    val result = deleteAccountUseCase(userId)
                    if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(isSaving = false, isDeleted = true)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = result.exceptionOrNull()?.toAppError() ?: AppError.Unknown("Failed to delete account")
                        )
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val client = SupabaseProvider.client
                    val userRepo = SupabaseUserRepository(client)
                    val gameRepo = SupabaseGameRepository(client)
                    val updateProfileUseCase = UpdateProfileUseCase(userRepo)
                    val deleteUseCase = DeleteAccountUseCase(userRepo, client)
                    return AccountViewModel(userRepo, gameRepo, updateProfileUseCase, deleteUseCase) as T
                }
            }
        }
    }
}
