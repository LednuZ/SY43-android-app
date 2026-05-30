package com.example.whereami.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupDto(
    val id: String? = null,
    val name: String,
    val created_at: String? = null
)

@Serializable
data class GroupMemberDto(
    val group_id: String,
    val user_id: String,
    val role: String = "MEMBER",
    val settings_notification: Boolean = true,
    val joined_at: String? = null
)
