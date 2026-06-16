package com.example.whereami.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whereami.navigation.NavigationDestination
import com.example.whereami.ui.viewmodel.CreateGroupViewModel
import io.github.jan.supabase.auth.user.UserInfo

object CreateGroupDestination : NavigationDestination {
    override val route = "create_group"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    currentUser: UserInfo,
    onNavigateUp: () -> Unit,
    viewModel: CreateGroupViewModel = viewModel(factory = CreateGroupViewModel.provideFactory())
) {
    LaunchedEffect(currentUser.id) {
        viewModel.initialize(currentUser.id)
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Group") },
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
        ) {
            OutlinedTextField(
                value = uiState.groupName,
                onValueChange = { viewModel.updateGroupName(it) },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Select Friends", style = MaterialTheme.typography.titleMedium)
            
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Crossfade(
                targetState = when {
                    uiState.isLoading -> "loading"
                    uiState.friends.isEmpty() -> "empty"
                    else -> "content"
                },
                label = "CreateGroupScreenState",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { state ->
                when (state) {
                    "loading" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    "empty" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "You don't have any friends to add yet!",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    "content" -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items = uiState.friends, key = { it.id }) { friend ->
                                val isSelected = uiState.selectedFriendIds.contains(friend.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                        .clickable { viewModel.toggleFriendSelection(friend.id) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { viewModel.toggleFriendSelection(friend.id) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(friend.username, style = MaterialTheme.typography.bodyLarge)
                                        Text(friend.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.createGroup() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = !uiState.isLoading
            ) {
                Text("Create Group")
            }
        }
    }
}
