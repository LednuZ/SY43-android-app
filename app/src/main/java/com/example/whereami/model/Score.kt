package com.example.whereami.model

import com.google.firebase.Timestamp

data class Score(
    val gameId: String,
    val userId: String,
    val totalScore: Int,
    val lastUpdated: Timestamp
)