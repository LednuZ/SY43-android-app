package com.example.whereami.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Picture(
    val id: String,
    val roundId: String,
    val publisherId: String,
    val imageUrl: String,
    val location : GeoPoint,
    val description: String? = null,
    val createdAt: Timestamp
)