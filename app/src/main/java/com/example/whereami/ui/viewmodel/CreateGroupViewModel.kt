package com.example.whereami.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.data.repository.SupabaseFriendRepository
import com.example.whereami.data.repository.SupabaseGroupRepository
import com.example.whereami.data.repository.SupabaseUserRepository
import com.example.whereami.domain.model.User
import com.example.whereami.domain.usecase.CreateGroupUseCase
import com.example.whereami.domain.usecase.GetFriendsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateGroupUiState(
    val isLoading: Boolean = false,
    val friends: List<User> = emptyList(),
    val selectedFriendIds: Set<String> = emptySet(),
    val groupName: String = "",
    val error: String? = null,
    val isSuccess: Boolean = false
)

class CreateGroupViewModel(
    private val getFriendsUseCase: GetFriendsUseCase,
    private val createGroupUseCase: CreateGroupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    fun initialize(userId: String) {
        if (currentUserId == userId) return
        currentUserId = userId
        _uiState.value = _uiState.value.copy(
            selectedFriendIds = setOf(userId)
        )
        fetchFriends()
    }

    private fun fetchFriends() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = getFriendsUseCase(userId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    friends = result.getOrThrow().friends
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load friends"
                )
            }
        }
    }

    fun updateGroupName(name: String) {
        _uiState.value = _uiState.value.copy(groupName = name)
    }

    fun toggleFriendSelection(friendId: String) {
        val currentSelection = _uiState.value.selectedFriendIds.toMutableSet()
        if (currentSelection.contains(friendId)) {
            if (friendId != currentUserId) {
                currentSelection.remove(friendId)
            }
        } else {
            currentSelection.add(friendId)
        }
        _uiState.value = _uiState.value.copy(selectedFriendIds = currentSelection)
    }

    fun createGroup() {
        val state = _uiState.value
        if (state.groupName.isBlank()) {
            _uiState.value = state.copy(error = "Group name cannot be blank")
            return
        }
        if (state.selectedFriendIds.size < 2) {
            _uiState.value = state.copy(error = "You must select at least one friend to play with!")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = createGroupUseCase(state.groupName, state.selectedFriendIds.toList())
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to create group"
                )
            }
        }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val groupRepo = SupabaseGroupRepository(SupabaseProvider.client)
                val friendRepo = SupabaseFriendRepository(SupabaseProvider.client)
                val userRepo = SupabaseUserRepository(SupabaseProvider.client)
                return CreateGroupViewModel(
                    getFriendsUseCase = GetFriendsUseCase(friendRepo, userRepo),
                    createGroupUseCase = CreateGroupUseCase(groupRepo)
                ) as T
            }
        }
    }
}
