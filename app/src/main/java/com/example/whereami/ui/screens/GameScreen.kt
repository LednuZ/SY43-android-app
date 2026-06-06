package com.example.whereami.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whereami.domain.model.RoundStatus
import com.example.whereami.navigation.NavigationDestination
import com.example.whereami.ui.viewmodel.GameViewModel

object GameDestination : NavigationDestination {
    override val route = "game/{gameId}"
    fun createRoute(gameId: String) = "game/$gameId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameId: String,
    onNavigateUp: () -> Unit,
    onNavigateToRound: (String, String) -> Unit,
    viewModel: GameViewModel = viewModel(factory = GameViewModel.provideFactory())
) {
    LaunchedEffect(gameId) {
        viewModel.initialize(gameId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it.toUserMessage())
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Game Rounds") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                if (uiState.game != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Game Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Total Rounds: ${uiState.game!!.settings.nbRound}", style = MaterialTheme.typography.bodyMedium)
                                val durationHours = uiState.game!!.settings.roundDurationMinutes / 60
                                Text("Round Duration: $durationHours hours", style = MaterialTheme.typography.bodyMedium)
                                Text("Status: ${uiState.game!!.status.name}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                item {
                    Text("Current Rounds", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (uiState.currentRounds.isEmpty()) {
                    item { Text("No active rounds") }
                } else {
                    items(uiState.currentRounds) { round ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onNavigateToRound(gameId, round.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Round ${round.index + 1}", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                val actionText = when (round.status) {
                                    RoundStatus.CREATED -> "Action Required: Play Now"
                                    RoundStatus.FINISHED -> "Finished"
                                    else -> "Waiting"
                                }
                                Text(actionText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Past Rounds", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (uiState.pastRounds.isEmpty()) {
                    item { Text("No past rounds") }
                } else {
                    items(uiState.pastRounds) { round ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onNavigateToRound(gameId, round.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Round ${round.index + 1}", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val displayStatus = if (round.index < (uiState.game?.currentRoundIndex ?: 0) && round.status.name == "CREATED") {
                                    "FINISHED"
                                } else {
                                    round.status.name
                                }
                                Text("Status: $displayStatus", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
