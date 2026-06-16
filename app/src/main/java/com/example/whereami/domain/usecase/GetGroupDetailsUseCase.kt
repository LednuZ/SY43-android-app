package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.Group
import com.example.whereami.domain.model.User
import com.example.whereami.domain.model.Game
import com.example.whereami.domain.repository.GroupRepository
import com.example.whereami.domain.repository.UserRepository
import com.example.whereami.domain.repository.GameRepository

data class GroupDetails(
    val group: Group,
    val members: List<User>,
    val activeGame: Game?
)

class GetGroupDetailsUseCase(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(groupId: String): Result<GroupDetails> {
        return runCatching {
            val group = groupRepository.getGroup(groupId).getOrThrow() ?: throw Exception("Group not found")
            val members = userRepository.getUsers(group.memberIds).getOrNull() ?: emptyList()
            val activeGame = gameRepository.getActiveGame(groupId).getOrNull()
            GroupDetails(group, members, activeGame)
        }
    }
}
