package com.example.whereami.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupDto(
    val id: String? = null,
    val name: String,
    val created_at: String? = null,
    val created_by: String? = null
)

@Serializable
data class GroupMemberDto(
    val group_id: String,
    val user_id: String,
    val joined_at: String? = null
)
