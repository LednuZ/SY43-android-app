package com.example.whereami.domain.model

import kotlinx.datetime.Instant
import com.example.whereami.domain.model.util.LatLng

data class Guess(
    val id: String,
    val roundId: String,
    val playerId: String,
    val pictureId: String,
    val guessedLocation: LatLng,
    val guessedAt: Instant,
    val distanceMeters: Double
)