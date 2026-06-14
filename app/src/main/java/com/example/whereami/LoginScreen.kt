package com.example.whereami

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import com.example.whereami.data.remote.SupabaseProvider
import com.example.whereami.navigation.NavigationDestination
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import com.example.whereami.util.toAppError

object LoginDestination : NavigationDestination {
    override val route= "login"
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onGoBackClick: () -> Unit,
    onLoginSuccess: () -> Unit
){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    val password = rememberTextFieldState("")
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

    if (context is ComponentActivity) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text("Welcome to WhereAmI", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                singleLine = true,
                label = { Text("Email") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SecureTextField(
                state = password,
                label = { Text("Password") },
                shape = RoundedCornerShape(12.dp),
                textObfuscationMode = if (isVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
                trailingIcon = {
                    IconButton(onClick = {isVisible = !isVisible}) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = "View"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ElevatedButton(
                        onClick = {
                            val passStr = password.text.toString()
                            if (email.isBlank() || passStr.isBlank()) {
                                errorMessage = "Please enter email and password"
                                return@ElevatedButton
                            }
                            isLoading = true
                            errorMessage = null
                            successMessage = null
                            coroutineScope.launch {
                                try {
                                    SupabaseProvider.client.auth.signInWith(Email) {
                                        this.email = email
                                        this.password = passStr
                                    }
                                    successMessage = "Logged in successfully!"
                                    kotlinx.coroutines.delay(500)
                                    onLoginSuccess()
                                } catch (e: Exception) {
                                    errorMessage = e.toAppError().toUserMessage()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        Text("Login")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            val passStr = password.text.toString()
                            if (email.isBlank() || passStr.isBlank()) {
                                errorMessage = "Please enter email and password"
                                return@OutlinedButton
                            }
                            isLoading = true
                            errorMessage = null
                            successMessage = null
                            coroutineScope.launch {
                                try {
                                    SupabaseProvider.client.auth.signUpWith(Email) {
                                        this.email = email
                                        this.password = passStr
                                    }
                                    successMessage = "Account created! Logging you in..."
                                } catch (e: Exception) {
                                    errorMessage = e.toAppError().toUserMessage()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        Text("Sign Up")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            var resetEmailDialogVisible by remember { mutableStateOf(false) }
            TextButton(onClick = { resetEmailDialogVisible = true }) {
                Text("Forgot Password?")
            }
            
            if (resetEmailDialogVisible) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { resetEmailDialogVisible = false },
                    title = { Text("Reset Password") },
                    text = {
                        Column {
                            Text("Enter your email address to receive a password reset link.")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                singleLine = true,
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (email.isNotBlank()) {
                                    isLoading = true
                                    resetEmailDialogVisible = false
                                    errorMessage = null
                                    successMessage = null
                                    coroutineScope.launch {
                                        try {
                                            SupabaseProvider.client.auth.resetPasswordForEmail(
                                                email = email,
                                                redirectUrl = "whereami://reset-password"
                                            )
                                            successMessage = "Password reset email sent!"
                                        } catch (e: Exception) {
                                            errorMessage = e.toAppError().toUserMessage()
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Send Link")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { resetEmailDialogVisible = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (successMessage != null) {
                Text(text = successMessage!!, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = onGoBackClick
            ) {
                Text("Go Back")
            }
        }
        }
    }
}