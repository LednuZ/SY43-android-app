package com.example.whereami.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.data.repository.SupabaseGroupRepository
import com.example.whereami.domain.model.Group
import com.example.whereami.domain.usecase.GetGroupsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GroupsUiState(
    val isLoading: Boolean = false,
    val groups: List<Group> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class GroupsViewModel(
    private val getGroupsUseCase: GetGroupsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    fun initialize(userId: String) {
        currentUserId = userId
        fetchGroups()
    }

    fun fetchGroups() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = getGroupsUseCase(userId)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    groups = result.getOrThrow()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to fetch groups"
                )
            }
        }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = SupabaseGroupRepository(SupabaseProvider.client)
                return GroupsViewModel(
                    getGroupsUseCase = GetGroupsUseCase(repository)
                ) as T
            }
        }
    }
}