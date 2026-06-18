package com.example.whereami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.animation.Crossfade
import androidx.compose.material3.CircularProgressIndicator
import com.example.whereami.ui.components.AnimatedDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whereami.domain.model.User
import com.example.whereami.navigation.NavigationDestination
import com.example.whereami.ui.viewmodel.FriendsViewModel
import com.example.whereami.ui.components.UserAvatar
import io.github.jan.supabase.auth.user.UserInfo

object FriendsDestination : NavigationDestination {
    override val route = "friends"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    currentUser: UserInfo,
    onNavigateUp: () -> Unit,
    viewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.provideFactory())
) {
    LaunchedEffect(currentUser.id) {
        viewModel.initialize(currentUser.id)
    }

    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var friendToDelete by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
            searchQuery = ""
            viewModel.searchUsers("")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.searchUsers(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search by username") },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Crossfade(
                targetState = when {
                    uiState.isLoading -> "loading"
                    uiState.error != null -> "error"
                    else -> "content"
                },
                label = "FriendsScreenState",
                modifier = Modifier.weight(1f).fillMaxWidth()
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
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    "content" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (searchQuery.isNotBlank() && uiState.searchResults.isNotEmpty()) {
                                item {
                                    Text("Search Results", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                                }
                                items(items = uiState.searchResults, key = { "search_${it.id}" }) { user ->
                                    UserRow(
                                        user = user,
                                        actionIcon = Icons.Default.PersonAdd,
                                        onActionClick = { viewModel.sendFriendRequest(user.id) },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }

                            item {
                                Text("My Friends", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            if (uiState.friends.isEmpty()) {
                                item {
                                    Text("No friends yet. Search to add some!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                items(items = uiState.friends, key = { "friend_${it.id}" }) { user ->
                                    UserRow(
                                        user = user,
                                        actionIcon = Icons.Default.Delete,
                                        onActionClick = { friendToDelete = user },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }

                            item {
                                Text("Pending Requests", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            if (uiState.pendingRequests.isEmpty()) {
                                item {
                                    Text("No pending request.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                items(items = uiState.pendingRequests, key = { "pending_${it.id}" }) { user ->
                                    UserRow(
                                        user = user,
                                        actionIcon = Icons.Default.Check,
                                        onActionClick = { viewModel.acceptFriendRequest(user.id) },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }

                            item {
                                Text("Sent Requests", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            if (uiState.sentRequests.isEmpty()) {
                                item {
                                    Text("No request sent.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                items(items = uiState.sentRequests, key = { "sent_${it.id}" }) { user ->
                                    UserRow(
                                        user = user,
                                        actionIcon = Icons.Default.AccessTime,
                                        onActionClick = { /* maybe cancel request later */ },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (friendToDelete != null) {
            AnimatedDialog(
                onDismissRequest = { friendToDelete = null },
                title = "Delete Friend",
                text = { Text("Are you sure you want to remove ${friendToDelete?.username} from your friends?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            friendToDelete?.let { viewModel.deleteFriend(it.id) }
                            friendToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { friendToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun UserRow(
    user: User,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector?,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.username, style = MaterialTheme.typography.bodyLarge)
            }
            if (actionIcon != null) {
                IconButton(onClick = onActionClick) {
                    Icon(actionIcon, contentDescription = "Action", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
