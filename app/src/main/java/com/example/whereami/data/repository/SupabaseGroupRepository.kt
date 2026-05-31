package com.example.whereami.data.repository

import com.example.whereami.data.dto.GroupDto
import com.example.whereami.data.dto.GroupMemberDto
import com.example.whereami.domain.model.Group
import com.example.whereami.domain.repository.GroupRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.SupabaseClient
import kotlin.time.Instant

class SupabaseGroupRepository(private val client: SupabaseClient) : GroupRepository {

    override suspend fun getGroup(groupId: String): Result<Group?> {
        return runCatching {
            val dto = client.from("groups").select {
                filter {
                    eq("id", groupId)
                }
            }.decodeSingleOrNull<GroupDto>() ?: return@runCatching null

            val members = client.from("group_members").select {
                filter {
                    eq("group_id", groupId)
                }
            }.decodeList<GroupMemberDto>()

            dto.toDomain(members.map { it.user_id })
        }
    }

    override suspend fun createGroup(group: Group): Result<String> {
        return runCatching {
            val groupId = java.util.UUID.randomUUID().toString()
            val dto = GroupDto(
                id = groupId,
                name = group.name
            )
            client.from("groups").insert(dto)

            val members = group.memberIds.map { userId ->
                GroupMemberDto(group_id = groupId, user_id = userId, role = "MEMBER")
            }
            if (members.isNotEmpty()) {
                client.from("group_members").insert(members)
            }
            
            groupId
        }
    }

    override suspend fun addMember(groupId: String, userId: String): Result<Unit> {
        return runCatching {
            val dto = GroupMemberDto(group_id = groupId, user_id = userId, role = "MEMBER")
            client.from("group_members").insert(dto)
        }
    }

    override suspend fun removeMember(groupId: String, userId: String): Result<Unit> {
        return runCatching {
            client.from("group_members").delete {
                filter {
                    eq("group_id", groupId)
                    eq("user_id", userId)
                }
            }
        }
    }

    override suspend fun getGroupsForUser(userId: String): Result<List<Group>> {
        return runCatching {
            val memberRows = client.from("group_members").select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<GroupMemberDto>()
            
            if (memberRows.isEmpty()) return@runCatching emptyList()
            
            val groupIds = memberRows.map { it.group_id }
            
            val groups = client.from("groups").select {
                filter {
                    isIn("id", groupIds)
                }
            }.decodeList<GroupDto>()

            val allMembers = client.from("group_members").select {
                filter {
                    isIn("group_id", groupIds)
                }
            }.decodeList<GroupMemberDto>()
            
            val membersMap = allMembers.groupBy { it.group_id }
            
            groups.map { groupDto -> 
                groupDto.toDomain(membersMap[groupDto.id]?.map { it.user_id } ?: emptyList())
            }
        }
    }

    private fun GroupDto.toDomain(memberIds: List<String>): Group {
        return Group(
            id = id ?: "",
            name = name,
            createdAt = created_at?.let { Instant.parse(it) } ?: Instant.fromEpochMilliseconds(0),
            memberIds = memberIds.toMutableList()
        )
    }
}
