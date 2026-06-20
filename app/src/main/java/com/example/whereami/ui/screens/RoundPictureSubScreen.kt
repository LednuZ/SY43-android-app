package com.example.whereami.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.whereami.domain.model.PlayerBox
import com.example.whereami.ui.viewmodel.RoundUiState
import java.io.File
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException

@Composable
fun RoundPictureSubScreen(
    uiState: RoundUiState,
    selectedBox: PlayerBox,
    onNavigateToMap: () -> Unit,
    onPictureCaptured: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCapturing by remember { mutableStateOf(false) }
    
    var permissionsGranted by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            permissionsGranted = permissions.values.all { it }
        }
    )

    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
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
                val cameraController = remember {
                    LifecycleCameraController(context).apply {
                        bindToLifecycle(lifecycleOwner)
                        imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                    }
                }
                
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            controller = cameraController
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (uiState.isUploadingPicture || isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    )
                } else {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Clic sur capture", Toast.LENGTH_SHORT).show()
                            isCapturing = true
                            val file = File(context.cacheDir, "round_${System.currentTimeMillis()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                            
                            cameraController.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        isCapturing = false
                                        Toast.makeText(context, "Photo capturée !", Toast.LENGTH_SHORT).show()
                                        onPictureCaptured(Uri.fromFile(file))
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                        Toast.makeText(context, "Erreur Camera : ${exception.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                            .size(80.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .border(4.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Permissions requises (Caméra & Localisation)")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            launcher.launch(arrayOf(
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
