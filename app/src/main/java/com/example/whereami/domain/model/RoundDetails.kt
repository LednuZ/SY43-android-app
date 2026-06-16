package com.example.whereami.domain.model

import com.example.whereami.domain.model.util.LatLng

data class GuessInfo(val user: User, val guessLocation: LatLng)

data class PlayerBox(
    val user: User,
    val hasUploaded: Boolean,
    val picture: Picture?,
    val guessers: List<User>,
    val guesses: List<GuessInfo>,
    val currentUserHasGuessed: Boolean,
    val isRevealed: Boolean
)

data class RoundDetails(
    val game: Game,
    val round: Round,
    val playerBoxes: List<PlayerBox>,
    val currentUserHasUploaded: Boolean,
    val allExpectedGuessed: Boolean,
    val timeIsUp: Boolean
)
