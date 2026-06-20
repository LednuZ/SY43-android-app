package com.example.whereami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onNavigateToMap: () -> Unit,
    onTakePhotoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (selectedBox.picture != null) {
                AsyncImage(
                    model = selectedBox.picture.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (uiState.isUploadingPicture) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = onTakePhotoClick,
                            modifier = Modifier.size(width = 200.dp, height = 56.dp)
                        ) {
                            Text("Take a photo")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        if (selectedBox.isRevealed) {
            Text(
                text = "All guesses submitted! Locations revealed on map.",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        val isOwnBox = selectedBox.user.id == uiState.currentUserId
        
        if (!isOwnBox || selectedBox.isRevealed) {
            Button(
                onClick = onNavigateToMap,
                modifier = Modifier.fillMaxWidth()
            ) {
                val buttonText = when {
                    selectedBox.isRevealed -> "View Results on Map"
                    selectedBox.currentUserHasGuessed -> "View Guess"
                    else -> "Guess Location"
                }
                Text(buttonText)
            }
        }
    }
}
