package com.example.whereami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.whereami.domain.model.PlayerBox
import com.example.whereami.ui.viewmodel.RoundUiState

@Composable
fun RoundPictureSubScreen(
    uiState: RoundUiState,
    selectedBox: PlayerBox,
    onNavigateToMap: () -> Unit
) {
    selectedBox.picture?.let { picture ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = picture.imageUrl,
                contentDescription = null,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (selectedBox.isRevealed) {
                Text("All guesses submitted! Locations revealed on map.", style = MaterialTheme.typography.titleMedium)
            }
            
            val isOwnBox = selectedBox.user.id == uiState.currentUserId
            if (!isOwnBox) {
                Button(
                    onClick = onNavigateToMap,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (selectedBox.isRevealed) Text("View Results on Map")
                    else if (selectedBox.currentUserHasGuessed) Text("View Guess")
                    else Text("Guess Location")
                }
            } else if (selectedBox.isRevealed) {
                Button(
                    onClick = onNavigateToMap,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Results on Map")
                }
            }
        }
    }
}
