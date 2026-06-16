package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.Group
import com.example.whereami.domain.repository.GroupRepository

class GetGroupsUseCase(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(userId: String): Result<List<Group>> {
        return groupRepository.getGroupsForUser(userId)
    }
}
