package com.example.whereami.ui.screens

import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import com.example.whereami.util.toAppError
import com.example.whereami.util.formatTimeLeft

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.whereami.domain.model.util.LatLng
import com.example.whereami.navigation.NavigationDestination
import com.example.whereami.ui.viewmodel.RoundViewModel
import com.example.whereami.domain.model.PlayerBox
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

object RoundDestination : NavigationDestination {
    override val route = "round/{gameId}/{roundId}"
    fun createRoute(gameId: String, roundId: String) = "round/$gameId/$roundId"
}

enum class RoundSubScreen { BOXES, PICTURE_VIEW, MAP_VIEW }

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun RoundScreen(
    gameId: String,
    roundId: String,
    onNavigateUp: () -> Unit,
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
    LaunchedEffect(uiState.round?.endTime) {
        val endTime = uiState.round?.endTime ?: return@LaunchedEffect
        while (true) {
            val now = kotlin.time.Clock.System.now()
            val diff = endTime - now
            timeLeft = formatTimeLeft(diff)
            if (!diff.isPositive()) break
            kotlinx.coroutines.delay(1000)
        }
    }
    
    var currentSubScreen by remember { mutableStateOf(RoundSubScreen.BOXES) }
    var selectedBox by remember { mutableStateOf<PlayerBox?>(null) }
    var currentPinLocation by remember { mutableStateOf<LatLng?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.readBytes()
            if (bytes != null) {
                val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                
                if (hasPermission) {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { loc: android.location.Location? ->
                        if (loc != null) {
                            viewModel.uploadPicture(LatLng(loc.latitude, loc.longitude), bytes)
                        } else {
                            viewModel.showError("Could not retrieve location. Please ensure Location Services (GPS) is turned on and try again.")
                        }
                    }.addOnFailureListener {
                        viewModel.showError("Failed to get location: ${it.message}")
                    }
                } else {
                    viewModel.showError("Location permission is required to upload a picture.")
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            imagePickerLauncher.launch("image/*")
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
                    }
                    Text(titleText) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (currentSubScreen) {
                            RoundSubScreen.BOXES -> onNavigateUp()
                            RoundSubScreen.PICTURE_VIEW -> currentSubScreen = RoundSubScreen.BOXES
                            RoundSubScreen.MAP_VIEW -> currentSubScreen = RoundSubScreen.PICTURE_VIEW
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                when (currentSubScreen) {
                    RoundSubScreen.BOXES -> {
                        RoundPlayerStatusSubScreen(
                            uiState = uiState,
                            onBoxSelected = { box ->
                                selectedBox = box
                                currentSubScreen = RoundSubScreen.PICTURE_VIEW
                            },
                            imagePickerLauncher = imagePickerLauncher,
                            permissionLauncher = permissionLauncher
                        )
                    }
                    RoundSubScreen.PICTURE_VIEW -> {
                        if (selectedBox != null) {
                            RoundPictureSubScreen(
                                uiState = uiState,
                                selectedBox = selectedBox!!,
                                onNavigateToMap = { currentSubScreen = RoundSubScreen.MAP_VIEW }
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
                }
            }
        }
    }
}
