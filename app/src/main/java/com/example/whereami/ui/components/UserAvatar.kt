package com.example.whereami.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun UserAvatar(
    profileUrl: String?,
    username: String,
    modifier: Modifier = Modifier
) {
    if (!profileUrl.isNullOrBlank()) {
        AsyncImage(
            model = profileUrl,
            contentDescription = "$username's avatar",
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        val firstLetter = username.take(1).uppercase()
        val colorHash = username.hashCode()
        val colors = listOf(
            Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
            Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF009688),
            Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFFFF5722)
        )
        val backgroundColor = colors[kotlin.math.abs(colorHash) % colors.size]
        
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = firstLetter,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
