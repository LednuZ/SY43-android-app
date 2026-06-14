package com.example.whereami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.data.repository.SupabaseGameRepository
import com.example.whereami.domain.usecase.game.GetPastGamesUseCase
import com.example.whereami.navigation.NavigationDestination
import com.example.whereami.ui.viewmodel.PastGamesViewModel
import io.github.jan.supabase.auth.auth

object PastGamesDestination : NavigationDestination {
    override val route = "past_games/{groupId}"
    fun createRoute(groupId: String) = "past_games/$groupId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastGamesScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    onNavigateToGame: (String) -> Unit,
    viewModel: PastGamesViewModel = viewModel(
        factory = PastGamesViewModel.provideFactory(
            GetPastGamesUseCase(SupabaseGameRepository(SupabaseProvider.client))
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = SupabaseProvider.client.auth.currentUserOrNull()

    LaunchedEffect(groupId) {
        viewModel.loadPastGamesForGroup(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Past Games") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (uiState.pastGames.isEmpty()) {
                Text(
                    text = "You haven't finished any games yet.",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.pastGames) { game ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToGame(game.id) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Game ID: ${game.id.take(8)}", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Rounds: ${game.settings.nbRound}", style = MaterialTheme.typography.bodyMedium)
                                
                                val userScore = game.scoreSheets.find { it.playerId == user?.id }?.score ?: 0
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Your Score: $userScore", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
