package com.example.whereami.model

data class Round(
    val id: String,
    val gameId: String,
    val index: Int,
    val isFinished: Boolean
)