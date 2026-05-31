package com.example.whereami.domain.usecase

import com.example.whereami.domain.model.Group
import com.example.whereami.domain.repository.GroupRepository

class CreateGroupUseCase(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(name: String, memberIds: List<String>): Result<String> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Group name cannot be blank"))
        if (memberIds.size < 2) return Result.failure(IllegalArgumentException("Group must have at least one other member"))

        val group = Group(
            name = name,
            memberIds = memberIds.toMutableList()
        )
        return groupRepository.createGroup(group)
    }
}
