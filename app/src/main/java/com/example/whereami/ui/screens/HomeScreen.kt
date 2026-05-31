package com.example.whereami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.navigation.NavigationDestination
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch

object HomeDestination : NavigationDestination {
    override val route = "home"
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onGroupsClick : () -> Unit
) {
    val sessionStatus by SupabaseProvider.client.auth.sessionStatus.collectAsState(initial = SessionStatus.Initializing)
    
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                val user = (sessionStatus as SessionStatus.Authenticated).session.user
                DashboardScreen(user = user, onFriendsClick = onFriendsClick, onGroupsClick)
            }
            is SessionStatus.Initializing -> {
                CircularProgressIndicator()
            }
            else -> {
                WelcomeScreen(onLoginClick = onLoginClick)
            }
        }
    }
}

@Composable
fun WelcomeScreen(onLoginClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Welcome to WhereAmI", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))
        ElevatedButton(
            onClick = onLoginClick
        ) {
            Text("Login to Play")
        }
    }
}

@Composable
fun DashboardScreen(
    user: UserInfo?,
    onFriendsClick: () -> Unit,
    onGroupsClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isSigningOut by remember { mutableStateOf(false) }

    val userDisplay = user?.email ?: "Player"

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Dashboard", fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Welcome back, $userDisplay!", fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Button(
            onClick = onGroupsClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Create New Game Group", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onFriendsClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("My Friends", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (isSigningOut) {
            CircularProgressIndicator()
        } else {
            TextButton(
                onClick = {
                    isSigningOut = true
                    coroutineScope.launch {
                        try {
                            SupabaseProvider.client.auth.signOut()
                        } finally {
                            isSigningOut = false
                        }
                    }
                }
            ) {
                Text("Sign Out", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}