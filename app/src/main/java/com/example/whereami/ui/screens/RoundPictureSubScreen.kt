package com.example.whereami.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.whereami.domain.model.PlayerBox
import com.example.whereami.ui.viewmodel.RoundUiState
import java.io.File

@Composable
fun RoundPictureSubScreen(
    uiState: RoundUiState,
    selectedBox: PlayerBox,
    onNavigateToMap: () -> Unit,
    onPictureCaptured: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempUri?.let { onPictureCaptured(it) }
            }
        }
    )

    var permissionsGranted by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            permissionsGranted = permissions.values.all { it }
        }
    )

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

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
            } else if (permissionsGranted) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (uiState.isUploadingPicture) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                val file = File(context.cacheDir, "images").apply { mkdirs() }
                                val imageFile = File(file, "round_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    imageFile
                                )
                                tempUri = uri
                                cameraLauncher.launch(uri)
                            },
                            modifier = Modifier.size(width = 200.dp, height = 56.dp)
                        ) {
                            Text("Prendre une photo")
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Permissions requises")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            permissionLauncher.launch(arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        }) {
                            Text("Accorder les permissions")
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
