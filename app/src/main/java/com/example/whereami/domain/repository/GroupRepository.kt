package com.example.whereami.domain.repository

import com.example.whereami.domain.model.*
interface GroupRepository {
    suspend fun getGroup(groupId: String): Result<Group?>
    suspend fun createGroup(group: Group): Result<String>
    suspend fun addMember(groupId: String, userId: String): Result<Unit>
    suspend fun removeMember(groupId: String, userId: String): Result<Unit>
    suspend fun getGroupsForUser(userId: String): Result<List<Group>>
}