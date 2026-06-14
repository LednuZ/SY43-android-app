package com.example.whereami.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PlayerScoreDisplay(
    val username: String,
    val score: Int
)

@Composable
fun ScoresPodiumList(
    playerScores: List<PlayerScoreDisplay>,
    modifier: Modifier = Modifier
) {
    val sortedScores = playerScores.sortedByDescending { it.score }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (sortedScores.isEmpty()) {
            Text("No scores available yet", color = MaterialTheme.colorScheme.secondary)
            return@Column
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            if (sortedScores.size >= 2) {
                PodiumColumn(
                    player = sortedScores[1],
                    rank = 2,
                    height = 120,
                    color = Color(0xFFC0C0C0), // Silver
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (sortedScores.isNotEmpty()) {
                PodiumColumn(
                    player = sortedScores[0],
                    rank = 1,
                    height = 160,
                    color = Color(0xFFFFD700), // Gold
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (sortedScores.size >= 3) {
                PodiumColumn(
                    player = sortedScores[2],
                    rank = 3,
                    height = 90,
                    color = Color(0xFFCD7F32), // Bronze
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (sortedScores.size > 3) {
            val remainingPlayers = sortedScores.drop(3)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(remainingPlayers) { index, player ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${index + 4}",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(32.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = player.username)
                            }
                            Text(text = "${player.score} pts", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    player: PlayerScoreDisplay,
    rank: Int,
    height: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = player.username,
            maxLines = 1,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "${player.score}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}
