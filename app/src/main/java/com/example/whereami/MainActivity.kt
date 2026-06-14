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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.runtime.getValue
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.collectAsState
import com.example.whereami.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.handleDeeplinks
import com.example.whereami.navigation.AppNavHost
import com.example.whereami.ui.screens.HomeDestination
import com.example.whereami.ui.screens.GroupsDestination
import com.example.whereami.ui.screens.FriendsDestination
import com.example.whereami.ui.screens.AccountDestination
import com.example.whereami.ui.theme.AppTheme

import android.content.Intent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SupabaseProvider.client.handleDeeplinks(intent)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    WhereAmI()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        SupabaseProvider.client.handleDeeplinks(intent)
    }
}

@Composable
fun WhereAmI(
    modifier : Modifier = Modifier,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hiddenBottomBarRoutes = listOf(
        "game",
        "round",
        "login",
        "reset_password"
    )
    val currentRouteBase = currentRoute?.substringBefore("/")
    val sessionStatus by SupabaseProvider.client.auth.sessionStatus.collectAsState()
    val isLoggedIn = sessionStatus is SessionStatus.Authenticated
    val showBottomBar = currentRouteBase != null && currentRouteBase !in hiddenBottomBarRoutes && isLoggedIn

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == HomeDestination.route,
                        onClick = {
                            if (currentRoute != HomeDestination.route) {
                                navController.navigate(HomeDestination.route) {
                                    popUpTo(HomeDestination.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == GroupsDestination.route,
                        onClick = {
                            if (currentRoute != GroupsDestination.route) {
                                navController.navigate(GroupsDestination.route) {
                                    popUpTo(HomeDestination.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Group, contentDescription = "Groups") },
                        label = { Text("Groups") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == FriendsDestination.route,
                        onClick = {
                            if (currentRoute != FriendsDestination.route) {
                                navController.navigate(FriendsDestination.route) {
                                    popUpTo(HomeDestination.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Friends") },
                        label = { Text("Friends") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == AccountDestination.route,
                        onClick = {
                            if (currentRoute != AccountDestination.route) {
                                navController.navigate(AccountDestination.route) {
                                    popUpTo(HomeDestination.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Account") },
                        label = { Text("Account") }
                    )
                }
            }
        }
    ) { paddingValues ->
        AppNavHost(navController = navController, modifier = Modifier.padding(paddingValues))
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTheme {
        WhereAmI()
    }
}