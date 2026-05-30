package com.example.whereami.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.whereami.LoginDestination
import com.example.whereami.LoginScreen
import com.example.whereami.ui.screens.HomeDestination
import com.example.whereami.ui.screens.HomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier : Modifier = Modifier,
)
{
    NavHost(
        navController=navController,
        startDestination = HomeDestination.route,
        modifier = Modifier
    ){
        composable (route = HomeDestination.route)
        {
            HomeScreen(onLoginClick = {
                navController.navigate(LoginDestination.route)
            })
        }

        composable (route = LoginDestination.route)
        {
            LoginScreen(onGoBackClick = {
                navController.popBackStack()
            })
        }
    }
}