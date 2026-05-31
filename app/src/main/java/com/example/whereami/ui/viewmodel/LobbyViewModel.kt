package com.example.whereami.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.data.repository.SupabaseGroupRepository
import com.example.whereami.data.repository.SupabaseUserRepository
import com.example.whereami.data.repository.SupabaseGameRepository
import com.example.whereami.domain.model.Group
import com.example.whereami.domain.model.User
import com.example.whereami.domain.model.Game
import com.example.whereami.domain.usecase.GetGroupDetailsUseCase
import com.example.whereami.domain.usecase.GetFriendsUseCase
import com.example.whereami.domain.repository.GroupRepository
import com.example.whereami.data.repository.SupabaseFriendRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

data class LobbyUiState(
    val isLoading: Boolean = true,
    val isStartingGame: Boolean = false,
    val gameStartedId: String? = null,
    val group: Group? = null,
    val members: List<User> = emptyList(),
    val activeGame: Game? = null,
    val error: String? = null,
    val isAddMemberDialogVisible: Boolean = false,
    val availableFriendsToAdd: List<User> = emptyList(),
    val isAddingMember: Boolean = false
)

class LobbyViewModel(
    private val getGroupDetailsUseCase: GetGroupDetailsUseCase,
    private val getFriendsUseCase: GetFriendsUseCase,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    private var initializedGroupId: String? = null

    fun initialize(groupId: String) {
        initializedGroupId = groupId
        fetchGroupDetails(groupId)
    }

    private fun fetchGroupDetails(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = getGroupDetailsUseCase(groupId)
            
            if (result.isSuccess) {
                val data = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    group = data.group,
                    members = data.members,
                    activeGame = data.activeGame
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load lobby"
                )
            }
        }
    }

    fun showAddMemberDialog() {
        val currentUserId = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddMemberDialogVisible = true, isAddingMember = true)
            val friendsResult = getFriendsUseCase(currentUserId)
            if (friendsResult.isSuccess) {
                val currentMemberIds = _uiState.value.members.map { it.id }.toSet()
                val friendsNotInGroup = friendsResult.getOrThrow().friends.filter { it.id !in currentMemberIds }
                _uiState.value = _uiState.value.copy(
                    isAddingMember = false,
                    availableFriendsToAdd = friendsNotInGroup
                )
            } else {
                _uiState.value = _uiState.value.copy(isAddingMember = false)
            }
        }
    }

    fun hideAddMemberDialog() {
        _uiState.value = _uiState.value.copy(isAddMemberDialogVisible = false)
    }

    fun addMember(friendId: String) {
        val groupId = initializedGroupId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingMember = true)
            val result = groupRepository.addMember(groupId, friendId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isAddMemberDialogVisible = false)
                fetchGroupDetails(groupId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isAddingMember = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to add member"
                )
            }
        }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val groupRepo = SupabaseGroupRepository(SupabaseProvider.client)
                val userRepo = SupabaseUserRepository(SupabaseProvider.client)
                val gameRepo = SupabaseGameRepository(SupabaseProvider.client)
                val friendRepo = SupabaseFriendRepository(SupabaseProvider.client)
                return LobbyViewModel(
                    getGroupDetailsUseCase = GetGroupDetailsUseCase(groupRepo, userRepo, gameRepo),
                    getFriendsUseCase = GetFriendsUseCase(friendRepo, userRepo),
                    groupRepository = groupRepo
                ) as T
            }
        }
    }
}
