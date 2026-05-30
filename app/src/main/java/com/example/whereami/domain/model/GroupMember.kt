package com.example.whereami.domain.model

import kotlin.time.Instant

data class GroupMember(
    val userId: String,
    val groupId: String,
    val joinedAt: Instant,
    val notificationsEnabled: Boolean,
    val role: String
)