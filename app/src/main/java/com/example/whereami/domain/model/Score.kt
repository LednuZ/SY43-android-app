package com.example.whereami.domain.model

import com.google.firebase.Timestamp

data class Score(
    val userId: String,
    val totalScore: Int,
    val lastUpdated: Timestamp
)