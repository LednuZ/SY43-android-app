package com.example.whereami.domain.model

import kotlinx.datetime.Instant
import com.example.whereami.domain.model.util.LatLng

data class Picture(
    val id: String,
    val roundId: String,
    val publisherId: String,
    val imageUrl: String,
    val location : LatLng,
    val description: String? = null,
    val createdAt: Instant,
    val reaveledAt : Instant? = null
)