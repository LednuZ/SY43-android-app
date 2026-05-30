package com.example.whereami.domain.model

import kotlinx.datetime.Instant

data class GroupMember(
    val userId: String,
    val groupId: String,
    val joinedAt: Instant,
    val notificationsEnabled: Boolean,
)