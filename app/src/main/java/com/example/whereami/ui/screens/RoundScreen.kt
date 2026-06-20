package com.example.whereami.ui.screens

import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import com.example.whereami.util.formatTimeLeft
import com.google.android.gms.location.Priority
import android.Manifest
import android.location.Location
import android.location.LocationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whereami.domain.model.util.LatLng
import com.example.whereami.navigation.NavigationDestination
import com.example.whereami.ui.viewmodel.RoundViewModel
import com.example.whereami.domain.model.PlayerBox
import org.osmdroid.config.Configuration

object RoundDestination : NavigationDestination {
    override val route = "round/{gameId}/{roundId}"
    fun createRoute(gameId: String, roundId: String) = "round/$gameId/$roundId"
}

enum class RoundSubScreen { BOXES, PICTURE_VIEW, MAP_VIEW, SCORES }

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun RoundScreen(
    gameId: String,
    roundId: String,
    onNavigateUp: () -> Unit,
    onNavigateToRound: (String, String) -> Unit,
    viewModel: RoundViewModel = viewModel(factory = RoundViewModel.provideFactory())
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    LaunchedEffect(gameId, roundId) {
        viewModel.initialize(gameId, roundId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it.toUserMessage())
            viewModel.dismissError()
        }
    }
    
    var timeLeft by remember { mutableStateOf("") }
    LaunchedEffect(uiState.round?.endTime, uiState.round?.status) {
        val round = uiState.round ?: return@LaunchedEffect
        if (round.status == com.example.whereami.domain.model.RoundStatus.FINISHED) {
            timeLeft = "Finished"
            return@LaunchedEffect
        }
        val endTime = round.endTime
        while (true) {
            val now = kotlin.time.Clock.System.now()
            val diff = endTime - now
            timeLeft = formatTimeLeft(diff)
            if (!diff.isPositive()) break
            kotlinx.coroutines.delay(1000L)
        }
    }
    
    var currentSubScreen by remember { mutableStateOf(RoundSubScreen.BOXES) }
    
    LaunchedEffect(uiState.round?.status) {
        if (uiState.round?.status == com.example.whereami.domain.model.RoundStatus.FINISHED) {
            currentSubScreen = RoundSubScreen.SCORES
        }
    }
    
    var selectedBox by remember { mutableStateOf<PlayerBox?>(null) }
    var currentPinLocation by remember { mutableStateOf<LatLng?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { uri ->
                val bytes = try {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } catch (_: Exception) {
                    null
                }

                if (bytes != null) {
                    val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener { loc ->
                                // If location is null, we use a default one to at least allow the upload
                                val locationToUse = loc ?: Location("default").apply {
                                    latitude = 0.0
                                    longitude = 0.0
                                }
                                viewModel.uploadPicture(LatLng(locationToUse.latitude, locationToUse.longitude), bytes)
                                currentSubScreen = RoundSubScreen.BOXES
                            }
                            .addOnFailureListener { e ->
                                // Even if GPS fails, we try to upload with 0,0 or show error
                                viewModel.showError("Location failed, using default: ${e.message}")
                                viewModel.uploadPicture(LatLng(0.0, 0.0), bytes)
                                currentSubScreen = RoundSubScreen.BOXES
                            }
                    } else {
                        viewModel.showError("GPS permission required.")
                    }
                } else {
                    viewModel.showError("Failed to read captured image.")
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        
        if (cameraGranted && locationGranted) {
            val file = File(context.cacheDir, "images").apply { mkdirs() }
            val imageFile = File(file, "round_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            tempUri = uri
            cameraLauncher.launch(uri)
        } else {
            viewModel.showError("Camera and Location permissions are required to take a photo.")
        }
    }

    val onTakePhotoClick = {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            viewModel.showError("Location services are disabled. Please enable GPS to take a photo.")
        } else {
            val hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (hasCameraPermission && hasLocationPermission) {
                val file = File(context.cacheDir, "images").apply { mkdirs() }
                val imageFile = File(file, "round_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
                tempUri = uri
                cameraLauncher.launch(uri)
            } else {
                permissionLauncher.launch(arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    val titleText = when (currentSubScreen) {
                        RoundSubScreen.BOXES -> "Round ${uiState.round?.index?.plus(1) ?: ""} - $timeLeft"
                        RoundSubScreen.PICTURE_VIEW -> "${selectedBox?.user?.username}'s Picture"
                        RoundSubScreen.MAP_VIEW -> "Guess Location"
                        RoundSubScreen.SCORES -> "Round Scores"
                    }
                    Text(titleText) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (currentSubScreen) {
                            RoundSubScreen.BOXES -> onNavigateUp()
                            RoundSubScreen.PICTURE_VIEW -> currentSubScreen = RoundSubScreen.BOXES
                            RoundSubScreen.MAP_VIEW -> currentSubScreen = RoundSubScreen.PICTURE_VIEW
                            RoundSubScreen.SCORES -> currentSubScreen = RoundSubScreen.BOXES
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (currentSubScreen == RoundSubScreen.BOXES || currentSubScreen == RoundSubScreen.SCORES) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentSubScreen == RoundSubScreen.BOXES,
                        onClick = { currentSubScreen = RoundSubScreen.BOXES },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Pictures") },
                        label = { Text("Pictures") }
                    )
                    NavigationBarItem(
                        selected = currentSubScreen == RoundSubScreen.SCORES,
                        onClick = { currentSubScreen = RoundSubScreen.SCORES },
                        icon = { Icon(Icons.Filled.Star, contentDescription = "Scores") },
                        label = { Text("Scores") }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Crossfade(
                targetState = if (uiState.isLoading) "loading" else "content",
                label = "RoundScreenState",
                modifier = Modifier.fillMaxSize()
            ) { state ->
                if (state == "loading") {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    when (currentSubScreen) {
                        RoundSubScreen.BOXES -> {
                            RoundPlayerStatusSubScreen(
                                uiState = uiState,
                                onBoxSelected = { box ->
                                    selectedBox = box
                                    currentSubScreen = RoundSubScreen.PICTURE_VIEW
                                },
                                onTakePhotoClick = onTakePhotoClick
                            )
                        }
                        RoundSubScreen.PICTURE_VIEW -> {
                            if (selectedBox != null) {
                                RoundPictureSubScreen(
                                    uiState = uiState,
                                    selectedBox = selectedBox!!,
                                    onNavigateToMap = { currentSubScreen = RoundSubScreen.MAP_VIEW },
                                    onTakePhotoClick = onTakePhotoClick
                                )
                            }
                        }
                        RoundSubScreen.MAP_VIEW -> {
                            if (selectedBox != null) {
                                RoundMapSubScreen(
                                    uiState = uiState,
                                    selectedBox = selectedBox!!,
                                    currentPinLocation = currentPinLocation,
                                    onPinLocationChanged = { currentPinLocation = it },
                                    onSubmitGuess = {
                                        currentPinLocation?.let { pin ->
                                            viewModel.submitGuess(selectedBox!!.picture!!.id, pin)
                                            currentSubScreen = RoundSubScreen.BOXES
                                        }
                                    }
                                )
                            }
                        }
                        RoundSubScreen.SCORES -> {
                            if (uiState.round?.status == com.example.whereami.domain.model.RoundStatus.FINISHED) {
                                val playerScores = uiState.round?.scoreSheets?.map { score ->
                                    val userBox = uiState.playerBoxes.find { it.user.id == score.playerId }
                                    val username = userBox?.user?.username ?: "Unknown"
                                    val avatarUrl = userBox?.user?.profilePicture
                                    com.example.whereami.ui.components.PlayerScoreDisplay(username, score.score, avatarUrl)
                                } ?: emptyList()
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        com.example.whereami.ui.components.ScoresPodiumList(playerScores = playerScores)
                                    }
                                    
                                    val nextRound = uiState.game?.rounds?.find { it.index == (uiState.round?.index ?: -1) + 1 }
                                    if (nextRound != null) {
                                        Button(
                                            onClick = {
                                                onNavigateToRound(gameId, nextRound.id)
                                            },
                                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                                        ) {
                                            Text("Next Round")
                                        }
                                    } else {
                                        Button(
                                            onClick = onNavigateUp,
                                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                                        ) {
                                            Text("Back to Game Details")
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("The round is not finished yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
