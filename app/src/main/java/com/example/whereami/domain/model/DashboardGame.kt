package com.example.whereami.domain.model

import kotlin.time.Instant

data class DashboardGame(
    val gameId: String,
    val groupId: String,
    val groupName: String,
    val currentRoundId: String,
    val roundEndTime: Instant,
    val needsUpload: Boolean,
    val picturesToGuessCount: Int
)
