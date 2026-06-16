package com.example.whereami.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whereami.domain.model.Group
import com.example.whereami.navigation.NavigationDestination
import com.example.whereami.ui.viewmodel.GroupsViewModel
import io.github.jan.supabase.auth.user.UserInfo

object GroupsDestination : NavigationDestination {
    override val route = "groups"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    currentUser: UserInfo,
    onNavigateUp: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onGroupClick: (String) -> Unit,
    viewModel: GroupsViewModel = viewModel(factory = GroupsViewModel.provideFactory())
) {
    LaunchedEffect(currentUser.id) {
        viewModel.initialize(currentUser.id)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Groups") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateGroupClick) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        }
    ) { paddingValues ->
        Crossfade(
            targetState = when {
                uiState.isLoading -> "loading"
                uiState.error != null -> "error"
                uiState.groups.isEmpty() -> "empty"
                else -> "content"
            },
            label = "GroupsScreenState",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
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
                "empty" -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "You are not in any groups yet. Create one!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                "content" -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items = uiState.groups, key = { it.id }) { group ->
                            GroupRow(
                                group = group,
                                onClick = { onGroupClick(group.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupRow(group: Group, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = group.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${group.memberIds.size} members", 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
