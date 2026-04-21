package com.example.whereami.model

import com.google.firebase.Timestamp

data class Game(
    val id: String,
    val groupId: String,
    val dateBegin: Timestamp,
    val dateEnd: Timestamp,
    val nbRound: Int,
    val roundDuration: Long,
    val isFinished: Boolean = false
)