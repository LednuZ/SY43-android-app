package com.example.whereami.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.data.repository.SupabaseFriendRepository
import com.example.whereami.data.repository.SupabaseUserRepository
import com.example.whereami.domain.model.User
import com.example.whereami.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendsUiState(
    val isLoading: Boolean = false,
    val friends: List<User> = emptyList(),
    val pendingRequests: List<User> = emptyList(),
    val sentRequests: List<User> = emptyList(),
    val searchResults: List<User> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class FriendsViewModel(
    private val getFriendsUseCase: GetFriendsUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val sendFriendRequestUseCase: SendFriendRequestUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    fun initialize(userId: String) {
        currentUserId = userId
        fetchFriends()
    }

    fun fetchFriends() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = getFriendsUseCase(userId)
            if (result.isSuccess) {
                val data = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    friends = data.friends,
                    pendingRequests = data.pendingRequests,
                    sentRequests = data.sentRequests
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to fetch friends"
                )
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = searchUsersUseCase(query)
            if (result.isSuccess) {
                val existingIds = buildSet {
                    currentUserId?.let { add(it) }
                    addAll(_uiState.value.friends.map { it.id })
                    addAll(_uiState.value.pendingRequests.map { it.id })
                    addAll(_uiState.value.sentRequests.map { it.id })
                }
                val results = result.getOrThrow().filter { it.id !in existingIds }
                _uiState.value = _uiState.value.copy(isLoading = false, searchResults = results)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Search failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun sendFriendRequest(toUserId: String) {
        val fromUserId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = sendFriendRequestUseCase(fromUserId, toUserId)) {
                is SendRequestResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Friend request sent!")
                    fetchFriends()
                }
                is SendRequestResult.AutoAccepted -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "They already invited you! You are now friends!")
                    fetchFriends()
                }
                is SendRequestResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun acceptFriendRequest(fromUserId: String) {
        val toUserId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = acceptFriendRequestUseCase(fromUserId, toUserId)) {
                is AcceptRequestResult.Success -> {
                    fetchFriends()
                }
                is AcceptRequestResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val client = SupabaseProvider.client
                val friendRepo = SupabaseFriendRepository(client)
                val userRepo = SupabaseUserRepository(client)

                return FriendsViewModel(
                    getFriendsUseCase = GetFriendsUseCase(friendRepo, userRepo),
                    searchUsersUseCase = SearchUsersUseCase(userRepo),
                    sendFriendRequestUseCase = SendFriendRequestUseCase(friendRepo, userRepo),
                    acceptFriendRequestUseCase = AcceptFriendRequestUseCase(friendRepo)
                ) as T
            }
        }
    }
}
