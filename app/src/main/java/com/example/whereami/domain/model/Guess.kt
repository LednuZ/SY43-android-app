package com.example.whereami.domain.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.Timestamp

data class Guess(
    val id: String,
    val roundId: String,
    val playerId: String,
    val pictureId: String,
    val guessedLocation: GeoPoint,
    val guessedAt: Timestamp,
    val distanceMeters: Double
)