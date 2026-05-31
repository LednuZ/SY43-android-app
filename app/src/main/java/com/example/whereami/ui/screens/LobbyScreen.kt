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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whereami.navigation.NavigationDestination
import com.example.whereami.ui.viewmodel.LobbyViewModel

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (uiState.group != null) {
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
                                    // TODO: Navigate to GameScreen
                                    // onNavigateToGame(uiState.activeGame!!.id)
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
                    onClick = { /* TODO: Navigate to Past Games */ },
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        membersToShow.forEach { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = user.username, style = MaterialTheme.typography.bodyLarge)
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
        }
        
        if (uiState.isAddMemberDialogVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.hideAddMemberDialog() },
                title = { Text("Add Member") },
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
