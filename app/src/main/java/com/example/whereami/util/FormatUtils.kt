package com.example.whereami.util

import kotlin.time.Duration

fun formatTimeLeft(diff: Duration): String {
    if (!diff.isPositive()) return "Round Over"
    
    val days = diff.inWholeDays
    val hours = diff.inWholeHours % 24
    val minutes = diff.inWholeMinutes % 60
    val seconds = diff.inWholeSeconds % 60
    
    return when {
        days > 0 -> "$days days ${hours}h"
        diff.inWholeHours > 0 -> "$hours h ${minutes}m"
        else -> String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
    }
}
