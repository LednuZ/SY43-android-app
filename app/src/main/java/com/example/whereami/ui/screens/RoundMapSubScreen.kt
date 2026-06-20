package com.example.whereami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.osmdroid.util.BoundingBox
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.whereami.domain.model.util.LatLng
import com.example.whereami.domain.model.PlayerBox
import com.example.whereami.ui.viewmodel.RoundUiState
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun RoundMapSubScreen(
    uiState: RoundUiState,
    selectedBox: PlayerBox,
    currentPinLocation: LatLng?,
    onPinLocationChanged: (LatLng) -> Unit,
    onSubmitGuess: () -> Unit
) {
    val loadedIcons = remember { mutableStateMapOf<String, android.graphics.drawable.Drawable>() }
    var hasZoomed by remember(selectedBox.isRevealed, selectedBox.user.id, uiState.round?.id) {
        mutableStateOf(false)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(3.0)
                    controller.setCenter(GeoPoint(48.8566, 2.3522))
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapView.overlays.removeAll { it is Marker || it is MapEventsOverlay }
                
                if (selectedBox.isRevealed) {
                    val picture = selectedBox.picture ?: return@AndroidView
                    val points = mutableListOf<GeoPoint>()
                    val exactGeoPoint = GeoPoint(picture.location.latitude, picture.location.longitude)
                    points.add(exactGeoPoint)

                    val exactIcon = ContextCompat.getDrawable(mapView.context, org.osmdroid.library.R.drawable.marker_default)?.mutate()
                    exactIcon?.setTint(android.graphics.Color.RED)
                    val exactMarker = Marker(mapView)
                    exactMarker.position = exactGeoPoint
                    exactMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    exactMarker.title = "Exact Location"
                    exactMarker.icon = exactIcon
                    mapView.overlays.add(exactMarker)
                    exactMarker.showInfoWindow()

                    selectedBox.guesses.forEach { guessInfo ->
                        val guessGeoPoint = GeoPoint(guessInfo.guessLocation.latitude, guessInfo.guessLocation.longitude)
                        points.add(guessGeoPoint)

                        val guessMarker = Marker(mapView)
                        guessMarker.position = guessGeoPoint
                        guessMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        guessMarker.title = guessInfo.user.username
                        
                        val avatarUrl = guessInfo.user.profilePicture
                        val key = avatarUrl ?: "initials_${guessInfo.user.username}"
                        
                        if (loadedIcons.containsKey(key)) {
                            guessMarker.icon = loadedIcons[key]
                        } else {
                            val guessIcon = ContextCompat.getDrawable(mapView.context, org.osmdroid.library.R.drawable.marker_default)?.mutate()
                            guessIcon?.setTint(android.graphics.Color.BLUE)
                            guessMarker.icon = guessIcon
                            
                            if (!avatarUrl.isNullOrBlank()) {
                                loadAvatarIcon(mapView.context, avatarUrl, guessInfo.user.username) { drawable ->
                                    loadedIcons[key] = drawable
                                }
                            } else {
                                val initialsDrawable = getInitialsMarkerDrawable(mapView.context, guessInfo.user.username)
                                loadedIcons[key] = initialsDrawable
                            }
                        }
                        
                        mapView.overlays.add(guessMarker)
                        guessMarker.showInfoWindow()
                    }

                    if (!hasZoomed && points.isNotEmpty()) {
                        mapView.post {
                            val boundingBox = BoundingBox.fromGeoPoints(points)
                            mapView.zoomToBoundingBox(boundingBox, true, 100, 18.0, 2000L)
                        }
                        hasZoomed = true
                    }
                } else {
                    if (!selectedBox.currentUserHasGuessed) {
                        val mReceive = MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                if (p != null) {
                                    onPinLocationChanged(LatLng(p.latitude, p.longitude))
                                    return true
                                }
                                return false
                            }
                            override fun longPressHelper(p: GeoPoint?): Boolean = false
                        })
                        mapView.overlays.add(mReceive)
                    }

                    // Show user's own guess if they have guessed
                    if (selectedBox.currentUserHasGuessed) {
                        val userGuess = selectedBox.guesses.find { it.user.id == uiState.currentUserId }
                        if (userGuess != null) {
                            val guessMarker = Marker(mapView)
                            guessMarker.position = GeoPoint(userGuess.guessLocation.latitude, userGuess.guessLocation.longitude)
                            guessMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            guessMarker.title = "Your Guess"
                            
                            val currentUser = uiState.playerBoxes.find { it.user.id == uiState.currentUserId }?.user
                            val avatarUrl = currentUser?.profilePicture
                            val username = currentUser?.username ?: "You"
                            val key = avatarUrl ?: "initials_$username"
                            
                            if (loadedIcons.containsKey(key)) {
                                guessMarker.icon = loadedIcons[key]
                            } else {
                                val guessIcon = ContextCompat.getDrawable(mapView.context, org.osmdroid.library.R.drawable.marker_default)?.mutate()
                                guessIcon?.setTint(android.graphics.Color.BLUE)
                                guessMarker.icon = guessIcon
                                
                                if (!avatarUrl.isNullOrBlank()) {
                                    loadAvatarIcon(mapView.context, avatarUrl, username) { drawable ->
                                        loadedIcons[key] = drawable
                                    }
                                } else {
                                    val initialsDrawable = getInitialsMarkerDrawable(mapView.context, username)
                                    loadedIcons[key] = initialsDrawable
                                }
                            }
                            mapView.overlays.add(guessMarker)
                        }
                    } else {
                        currentPinLocation?.let { pin ->
                            val pinMarker = Marker(mapView)
                            pinMarker.position = GeoPoint(pin.latitude, pin.longitude)
                            pinMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            mapView.overlays.add(pinMarker)
                        }
                    }
                }
                
                mapView.invalidate()
            }
        )
        
        if (!selectedBox.isRevealed && !selectedBox.currentUserHasGuessed) {
            Button(
                onClick = onSubmitGuess,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                enabled = currentPinLocation != null && !uiState.isSubmittingGuess
            ) {
                if (uiState.isSubmittingGuess) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Guess")
                }
            }
        }
    }
}

private fun loadAvatarIcon(
    context: android.content.Context,
    url: String,
    username: String,
    onLoaded: (android.graphics.drawable.Drawable) -> Unit
) {
    val imageLoader = coil.ImageLoader(context)
    val request = coil.request.ImageRequest.Builder(context)
        .data(url)
        .allowHardware(false)
        .target { result ->
            val bitmap = (result as android.graphics.drawable.BitmapDrawable).bitmap
            val circularDrawable = getCircularMarkerDrawable(context, bitmap)
            onLoaded(circularDrawable)
        }
        .listener(onError = { _, _ ->
            val initialsDrawable = getInitialsMarkerDrawable(context, username)
            onLoaded(initialsDrawable)
        })
        .build()
    imageLoader.enqueue(request)
}

private fun getCircularMarkerDrawable(context: android.content.Context, bitmap: android.graphics.Bitmap): android.graphics.drawable.Drawable {
    val size = 120
    val output = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)
    
    val borderPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, borderPaint)
    
    val avatarRadius = (size / 2f) - 6
    val targetSize = avatarRadius * 2f
    
    val avatarPaint = android.graphics.Paint().apply {
        isAntiAlias = true
    }
    
    val shader = android.graphics.BitmapShader(
        bitmap,
        android.graphics.Shader.TileMode.CLAMP,
        android.graphics.Shader.TileMode.CLAMP
    )
    
    val scale = kotlin.math.max(targetSize / bitmap.width.toFloat(), targetSize / bitmap.height.toFloat())
    val dx = (targetSize - bitmap.width * scale) / 2f
    val dy = (targetSize - bitmap.height * scale) / 2f
    
    val matrix = android.graphics.Matrix()
    matrix.setScale(scale, scale)
    matrix.postTranslate(dx + 6f, dy + 6f)
    shader.setLocalMatrix(matrix)
    
    avatarPaint.shader = shader
    
    canvas.drawCircle(size / 2f, size / 2f, avatarRadius, avatarPaint)
    
    return android.graphics.drawable.BitmapDrawable(context.resources, output)
}

private fun getInitialsMarkerDrawable(context: android.content.Context, username: String): android.graphics.drawable.Drawable {
    val size = 120
    val output = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)
    
    val paint = android.graphics.Paint()
    paint.isAntiAlias = true
    
    val colorHash = username.hashCode()
    val colors = listOf(
        0xFFE91E63.toInt(), 0xFF9C27B0.toInt(), 0xFF673AB7.toInt(),
        0xFF3F51B5.toInt(), 0xFF2196F3.toInt(), 0xFF009688.toInt(),
        0xFF4CAF50.toInt(), 0xFFFF9800.toInt(), 0xFFFF5722.toInt()
    )
    val backgroundColor = colors[kotlin.math.abs(colorHash) % colors.size]
    
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    
    paint.color = backgroundColor
    canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 6, paint)
    
    val textPaint = android.graphics.Paint()
    textPaint.color = android.graphics.Color.WHITE
    textPaint.textSize = 48f
    textPaint.isFakeBoldText = true
    textPaint.isAntiAlias = true
    textPaint.textAlign = android.graphics.Paint.Align.CENTER
    
    val firstLetter = username.take(1).uppercase()
    val yPos = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(firstLetter, size / 2f, yPos, textPaint)
    
    return android.graphics.drawable.BitmapDrawable(context.resources, output)
}
