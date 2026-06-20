package com.example.whereami.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import com.example.whereami.domain.model.PlayerBox
import com.example.whereami.ui.viewmodel.RoundUiState

@Composable
fun RoundPlayerStatusSubScreen(
    uiState: RoundUiState,
    onBoxSelected: (PlayerBox) -> Unit,
    imagePickerLauncher: ManagedActivityResultLauncher<String, android.net.Uri?>,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (!uiState.currentUserHasUploaded) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("You must upload a picture before guessing!", color = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val myBox = uiState.playerBoxes.find { it.user.id == uiState.currentUserId }
                            if (myBox != null) {
                                onBoxSelected(myBox)
                            }
                        },
                        enabled = !uiState.isUploadingPicture
                    ) {
                        if (uiState.isUploadingPicture) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Take / Upload Picture")
                        }
                    }
                }
            }
        }
        
        val myBox = uiState.playerBoxes.find { it.user.id == uiState.currentUserId }
        val otherBoxes = uiState.playerBoxes.filter { it.user.id != uiState.currentUserId }

        if (myBox != null) {
            Text("My Status", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable {
                        onBoxSelected(myBox)
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("You: ${myBox.user.username}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (myBox.hasUploaded) {
                        Text("Picture Uploaded", color = MaterialTheme.colorScheme.primary)
                        if (myBox.guessers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Guessed by: ${myBox.guessers.joinToString { it.username }}", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Text("Waiting for you to upload...", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        Text("Other Players", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(otherBoxes) { box ->
                val isClickable = uiState.currentUserHasUploaded && box.hasUploaded
                val needsGuess = isClickable && !box.currentUserHasGuessed
                val cardColor = when {
                    box.currentUserHasGuessed -> if (box.isRevealed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.tertiaryContainer
                    needsGuess -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isClickable) {
                            onBoxSelected(box)
                        },
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(box.user.username, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (box.hasUploaded) {
                            val statusText = when {
                                needsGuess -> "Make a guess!"
                                box.currentUserHasGuessed -> "You Guessed It"
                                else -> "Uploaded"
                            }
                            val statusColor = when {
                                needsGuess -> MaterialTheme.colorScheme.onErrorContainer
                                box.currentUserHasGuessed -> if (box.isRevealed) Color.White else MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Text(statusText, color = statusColor, style = MaterialTheme.typography.bodyMedium)
                            if (box.guessers.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Guessed by:", style = MaterialTheme.typography.labelSmall)
                                box.guessers.forEach { guesser ->
                                    Text(guesser.username, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        } else {
                            Text("Waiting...", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}
