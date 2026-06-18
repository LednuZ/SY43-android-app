package com.example.whereami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import com.example.whereami.ui.components.AnimatedDialog
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whereami.navigation.NavigationDestination
import com.example.whereami.ui.viewmodel.LobbyViewModel
import com.example.whereami.ui.components.UserAvatar

object LobbyDestination : NavigationDestination {
    override val route = "lobby/{groupId}"
    fun createRoute(groupId: String) = "lobby/$groupId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    groupId: String,
    onNavigateUp: () -> Unit,
    onCreateGameClick: () -> Unit,
    onNavigateToGame: (String) -> Unit,
    onNavigateToPastGames: (String) -> Unit,
    viewModel: LobbyViewModel = viewModel(factory = LobbyViewModel.provideFactory())
) {
    LaunchedEffect(groupId) {
        viewModel.initialize(groupId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.group?.name ?: "Loading Lobby...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Crossfade(
            targetState = when {
                uiState.isLoading -> "loading"
                uiState.error != null -> "error"
                uiState.group != null -> "content"
                else -> "empty"
            },
            label = "LobbyScreenState",
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) { state ->
            when (state) {
                "loading" -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                "error" -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                "content" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (uiState.activeGame != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Active Game Found!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Round: ${uiState.activeGame!!.currentRoundIndex + 1} / ${uiState.activeGame!!.settings.nbRound}", color = MaterialTheme.colorScheme.onPrimaryContainer)

                                    Button(
                                        onClick = {
                                            onNavigateToGame(uiState.activeGame!!.id)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("View Current Game")
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = onCreateGameClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Create New Game")
                            }
                        }

                        OutlinedButton(
                            onClick = { onNavigateToPastGames(groupId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text("View Past Games")
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Members", style = MaterialTheme.typography.titleLarge)
                            IconButton(onClick = { viewModel.showAddMemberDialog() }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Member")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        var membersExpanded by remember { mutableStateOf(false) }
                        val membersToShow = if (membersExpanded) uiState.members else uiState.members.take(3)

                        Column(
                            modifier = Modifier.fillMaxWidth().animateContentSize()
                        ) {
                            membersToShow.forEach { user ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        UserAvatar(
                                            profileUrl = user.profilePicture,
                                            username = user.username,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        val displayName = user.username + (if (user.id == uiState.currentUserId) " (Me)" else "")
                                        Text(text = displayName, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }

                    if (uiState.members.size > 3) {
                        TextButton(
                            onClick = { membersExpanded = !membersExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (membersExpanded) "See Less" else "See More (${uiState.members.size - 3})")
                        }
                    }
                }
            }
            "empty" -> {}
        }
    }
        
        if (uiState.isAddMemberDialogVisible) {
            AnimatedDialog(
                onDismissRequest = { viewModel.hideAddMemberDialog() },
                title = "Add Member",
                text = {
                    if (uiState.isAddingMember && uiState.availableFriendsToAdd.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    } else if (uiState.availableFriendsToAdd.isEmpty()) {
                        Text("You don't have any friends to add right now!")
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(uiState.availableFriendsToAdd) { friend ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.addMember(friend.id) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(
                                        profileUrl = friend.profilePicture,
                                        username = friend.username,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(friend.username, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.hideAddMemberDialog() }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
