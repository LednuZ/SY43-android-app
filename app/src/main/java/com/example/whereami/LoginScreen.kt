package com.example.whereami

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import com.example.whereami.navigation.NavigationDestination

object LoginDestination : NavigationDestination {
    override val route= "login"
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
){
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    val password = rememberTextFieldState("")
    var isVisible by remember { mutableStateOf(false) }
    if (context is ComponentActivity) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text("User Login", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                singleLine = true,
                label = { Text("Username") },
                shape = RoundedCornerShape(12.dp),
            )
            SecureTextField(
                password,
                label = { Text("Password") },
                shape = RoundedCornerShape(12.dp),
                textObfuscationMode = if (isVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
                trailingIcon = {IconButton(onClick = {isVisible = !isVisible})
                    {Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = "View"
                    )}}
            )
            Spacer(modifier = Modifier.weight(1f))
            ElevatedButton(
                onClick = {
                    context.finish()
                }
            ) {
                Text("Go Back")
            }
        }
    }
}