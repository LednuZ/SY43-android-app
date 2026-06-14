package com.example.whereami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                    val exactIcon = ContextCompat.getDrawable(mapView.context, org.osmdroid.library.R.drawable.marker_default)?.mutate()
                    exactIcon?.setTint(android.graphics.Color.RED)
                    val exactMarker = Marker(mapView)
                    exactMarker.position = GeoPoint(selectedBox.picture!!.location.latitude, selectedBox.picture!!.location.longitude)
                    exactMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    exactMarker.title = "Exact Location"
                    exactMarker.icon = exactIcon
                    mapView.overlays.add(exactMarker)
                    exactMarker.showInfoWindow()

                    selectedBox.guesses.forEach { guessInfo ->
                        val guessIcon = ContextCompat.getDrawable(mapView.context, org.osmdroid.library.R.drawable.marker_default)?.mutate()
                        guessIcon?.setTint(android.graphics.Color.BLUE)
                        val guessMarker = Marker(mapView)
                        guessMarker.position = GeoPoint(guessInfo.guessLocation.latitude, guessInfo.guessLocation.longitude)
                        guessMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        guessMarker.title = guessInfo.user.username
                        guessMarker.icon = guessIcon
                        mapView.overlays.add(guessMarker)
                        guessMarker.showInfoWindow()
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
                            val guessIcon = ContextCompat.getDrawable(mapView.context, org.osmdroid.library.R.drawable.marker_default)?.mutate()
                            guessIcon?.setTint(android.graphics.Color.BLUE)
                            val guessMarker = Marker(mapView)
                            guessMarker.position = GeoPoint(userGuess.guessLocation.latitude, userGuess.guessLocation.longitude)
                            guessMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            guessMarker.title = "Your Guess"
                            guessMarker.icon = guessIcon
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
