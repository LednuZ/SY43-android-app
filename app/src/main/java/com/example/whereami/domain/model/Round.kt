package com.example.whereami.domain.model

// possible refactor for round status : deleted CREATED status
enum class RoundStatus { CREATED, PLAYING, REVEALED, FINISHED }
data class Round(
    val id: String,
    val gameId: String,
    val posts: MutableList<Picture> = mutableListOf(),
    val index: Int,
    val status: RoundStatus
)