package com.example.whereami.domain.model

import com.google.firebase.Timestamp

data class GroupMember(
    val userId: String,
    val groupId: String,
    val joinedAt: Timestamp,
    val notificationsEnabled: Boolean,
)