package com.example.whereami.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.example.whereami.ui.components.ShimmerLogo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.navigation.NavigationDestination
import com.example.whereami.ui.viewmodel.HomeViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

object HomeDestination : NavigationDestination {
    override val route = "home"
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onNavigateToRound: (String, String) -> Unit,
    onNavigateToAccount: () -> Unit = {}
) {
    val sessionStatus by SupabaseProvider.client.auth.sessionStatus.collectAsState(initial = SessionStatus.Initializing)
    
    Crossfade(
        targetState = sessionStatus, 
        label = "SessionCrossfade",
        modifier = modifier.fillMaxSize()
    ) { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                val user = (status as SessionStatus.Authenticated).session.user
                DashboardScreen(
                    user = user,
                    onNavigateToRound = onNavigateToRound,
                )
            }
            is SessionStatus.Initializing -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ShimmerLogo()
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LaunchedEffect(Unit) {
                        onLoginClick()
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: UserInfo?,
    onNavigateToRound: (String, String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.provideFactory())
) {
    val coroutineScope = rememberCoroutineScope()
    var isSigningOut by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(user?.id) {
        if (user != null) {
            viewModel.fetchActiveGames(user.id)
        }
    }

    val userDisplay = user?.email ?: "Player"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhereAmI") },
                actions = {
                    if (isSigningOut) {
                        ShimmerLogo(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp)
                        )
                    } else {
                        IconButton(onClick = {
                            isSigningOut = true
                            coroutineScope.launch {
                                try {
                                    SupabaseProvider.client.auth.signOut()
                                } finally {
                                    isSigningOut = false
                                }
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Welcome back, $userDisplay!",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Active Games",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Crossfade(
                targetState = when {
                    uiState.isLoading -> "loading"
                    uiState.error != null -> "error"
                    uiState.activeGames.isEmpty() -> "empty"
                    else -> "content"
                },
                label = "DashboardContent",
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { state ->
                when (state) {
                    "loading" -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            ShimmerLogo()
                        }
                    }

                    "error" -> {
                        Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    }

                    "empty" -> {
                        Text(
                            "You have no active games right now.",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    "content" -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items = uiState.activeGames, key = { it.gameId }) { game ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().animateItem().clickable {
                                        onNavigateToRound(
                                            game.gameId,
                                            game.currentRoundId
                                        )
                                    },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                        Text(
                                            "Game in ${game.groupName}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        if (game.needsUpload) {
                                            Button(
                                                onClick = {
                                                    onNavigateToRound(
                                                        game.gameId,
                                                        game.currentRoundId
                                                    )
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("You need to upload a picture!")
                                            }
                                        } else {
                                            Text(
                                                "Picture uploaded",
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (game.picturesToGuessCount > 0) {
                                            Button(
                                                onClick = {
                                                    onNavigateToRound(
                                                        game.gameId,
                                                        game.currentRoundId
                                                    )
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("${game.picturesToGuessCount} picture(s) waiting for your guess!")
                                            }
                                        } else {
                                            Text(
                                                "No pictures waiting for guesses",
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
