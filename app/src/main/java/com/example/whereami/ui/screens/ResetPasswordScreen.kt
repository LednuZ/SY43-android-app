package com.example.whereami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.navigation.NavigationDestination
import com.example.whereami.util.toAppError
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

object ResetPasswordDestination : NavigationDestination {
    override val route = "reset_password"
}

@Composable
fun ResetPasswordScreen(
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val newPassword = rememberTextFieldState("")
    var isVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text("Reset Your Password", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Enter your new password below.", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            SecureTextField(
                state = newPassword,
                label = { Text("New Password") },
                shape = RoundedCornerShape(12.dp),
                textObfuscationMode = if (isVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
                trailingIcon = {
                    IconButton(onClick = { isVisible = !isVisible }) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                ElevatedButton(
                    onClick = {
                        val passStr = newPassword.text.toString()
                        if (passStr.isBlank()) {
                            errorMessage = "Please enter a valid password"
                            return@ElevatedButton
                        }
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        coroutineScope.launch {
                            try {
                                SupabaseProvider.client.auth.updateUser {
                                    password = passStr
                                }
                                successMessage = "Password reset successfully!"
                                kotlinx.coroutines.delay(1000)
                                onNavigateToHome()
                            } catch (e: Exception) {
                                errorMessage = e.toAppError().toUserMessage()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Password")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (successMessage != null) {
                Text(text = successMessage!!, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
