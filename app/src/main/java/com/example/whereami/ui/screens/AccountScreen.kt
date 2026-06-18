package com.example.whereami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.example.whereami.ui.components.ShimmerLogo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whereami.navigation.NavigationDestination
import com.example.whereami.ui.viewmodel.AccountViewModel
import io.github.jan.supabase.auth.user.UserInfo
import com.example.whereami.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import com.example.whereami.util.toAppError

object AccountDestination : NavigationDestination {
    override val route = "account"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    currentUser: UserInfo,
    onNavigateUp: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AccountViewModel = viewModel(factory = AccountViewModel.provideFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var newUsername by remember { mutableStateOf("") }
    var newFirstName by remember { mutableStateOf("") }
    var newLastName by remember { mutableStateOf("") }
    var newPhoneNumber by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }
    
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentUser.id) {
        viewModel.initialize(currentUser.id)
    }

    LaunchedEffect(uiState.currentUserData) {
        val user = uiState.currentUserData
        if (user != null && !isInitialized) {
            newUsername = user.username
            newFirstName = user.firstName ?: ""
            newLastName = user.lastName ?: ""
            newPhoneNumber = user.phoneNumber ?: ""
            isInitialized = true
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateToLogin()
        }
    }

    val isProfileChanged = uiState.currentUserData?.let { user ->
        newUsername != user.username ||
        newFirstName != (user.firstName ?: "") ||
        newLastName != (user.lastName ?: "") ||
        newPhoneNumber != (user.phoneNumber ?: "")
    } ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Crossfade(
                targetState = if (uiState.isLoading) "loading" else "content",
                label = "AccountScreenState",
                modifier = Modifier.fillMaxSize()
            ) { state ->
                if (state == "loading") {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ShimmerLogo()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Update Profile",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = newUsername,
                            onValueChange = { 
                                newUsername = it
                                viewModel.clearMessages()
                            },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = newFirstName,
                            onValueChange = { 
                                newFirstName = it
                                viewModel.clearMessages()
                            },
                            label = { Text("First Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newLastName,
                            onValueChange = { 
                                newLastName = it
                                viewModel.clearMessages()
                            },
                            label = { Text("Last Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newPhoneNumber,
                            onValueChange = { 
                                newPhoneNumber = it
                                viewModel.clearMessages()
                            },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { 
                                viewModel.updateProfile(newUsername, newFirstName, newLastName, newPhoneNumber)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving && newUsername.isNotBlank() && isProfileChanged
                        ) {
                            if (uiState.isSaving && !showDeleteConfirmation) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Save Changes")
                            }
                        }

                        if (uiState.successMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(uiState.successMessage!!, color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodyMedium)
                        }

                        val currentError = uiState.error
                        if (currentError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(currentError.toUserMessage(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                        
                        Text(
                            "Security",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        var isResettingPassword by remember { mutableStateOf(false) }
                        var resetMessage by remember { mutableStateOf<String?>(null) }
                        var resetError by remember { mutableStateOf<String?>(null) }
                        
                        Button(
                            onClick = {
                                val email = currentUser.email
                                if (email != null) {
                                    isResettingPassword = true
                                    resetMessage = null
                                    resetError = null
                                    coroutineScope.launch {
                                        try {
                                            SupabaseProvider.client.auth.resetPasswordForEmail(
                                                email = email,
                                                redirectUrl = "whereami://reset-password"
                                            )
                                            resetMessage = "Password reset email sent!"
                                        } catch (e: Exception) {
                                            resetError = e.toAppError().toUserMessage()
                                        } finally {
                                            isResettingPassword = false
                                        }
                                    }
                                } else {
                                    resetError = "No email associated with this account."
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            if (isResettingPassword) {
                                ShimmerLogo(modifier = Modifier.size(24.dp))
                            } else {
                                Text("Modify Password")
                            }
                        }
                        
                        if (resetMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(resetMessage!!, color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        if (resetError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(resetError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                        
                        Text(
                            "Danger Zone",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete Account")
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            if (showDeleteConfirmation) {
                com.example.whereami.ui.components.AnimatedDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = "Delete Account?",
                    text = { 
                        Text("Are you absolutely sure you want to delete your account? This action is irreversible. All your personal data will be anonymized and you will be removed from all groups.") 
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirmation = false
                                viewModel.deleteAccount()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
