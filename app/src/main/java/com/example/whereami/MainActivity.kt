package com.example.whereami

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.whereami.navigation.AppNavHost
import com.example.whereami.ui.theme.WhereAmITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhereAmITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        WhereAmI()
                    }
                }
            }
        }
    }
}

// Application
@Composable
fun WhereAmI(
    modifier : Modifier = Modifier,
){
//    val context = LocalContext.current
//    Column(
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ){
//        Text("Welcome to WhereAmI", fontSize = 24.sp)
//        Spacer(modifier = modifier.height(32.dp))
//        ElevatedButton(
//            onClick = {
//                val intent = Intent(context, LoginActivity::class.java)
//                context.startActivity(intent)
//            }
//        ) {
//            Text("Login")
//        }
//    }
    AppNavHost(navController = rememberNavController())
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WhereAmITheme {
        WhereAmI()
    }
}