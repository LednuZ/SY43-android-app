package com.example.whereami.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.whereami.LoginDestination
import com.example.whereami.LoginScreen
import com.example.whereami.ui.screens.HomeDestination
import com.example.whereami.ui.screens.HomeScreen
import com.example.whereami.ui.screens.FriendsDestination
import com.example.whereami.ui.screens.FriendsScreen
import com.example.whereami.ui.screens.GroupsDestination
import com.example.whereami.ui.screens.GroupsScreen
import com.example.whereami.ui.screens.CreateGroupDestination
import com.example.whereami.ui.screens.CreateGroupScreen
import com.example.whereami.ui.screens.LobbyDestination
import com.example.whereami.ui.screens.LobbyScreen
import com.example.whereami.ui.screens.CreateGameDestination
import com.example.whereami.ui.screens.CreateGameScreen
import com.example.whereami.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth

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
            HomeScreen(
                onLoginClick = { navController.navigate(LoginDestination.route) },
                onFriendsClick = { navController.navigate(FriendsDestination.route) },
                onGroupsClick = { navController.navigate(GroupsDestination.route) }
            )
        }

        composable (route = LoginDestination.route)
        {
            LoginScreen(
                onGoBackClick = {
                    navController.popBackStack()
                },
                onLoginSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable (route = FriendsDestination.route) {
            val session = SupabaseProvider.client.auth.currentSessionOrNull()
            if (session?.user != null) {
                FriendsScreen(
                    currentUser = session.user!!,
                    onNavigateUp = { navController.popBackStack() }
                )
            }
        }
        composable (route = GroupsDestination.route) {
            val session = SupabaseProvider.client.auth.currentSessionOrNull()
            if (session?.user != null) {
                GroupsScreen(
                    currentUser = session.user!!,
                    onNavigateUp = { navController.popBackStack() },
                    onCreateGroupClick = { navController.navigate(CreateGroupDestination.route) },
                    onGroupClick = { groupId -> navController.navigate(LobbyDestination.createRoute(groupId)) }
                )
            }
        }
        composable (route = CreateGroupDestination.route) {
            val session = SupabaseProvider.client.auth.currentSessionOrNull()
            if (session?.user != null) {
                CreateGroupScreen(
                    currentUser = session.user!!,
                    onNavigateUp = { navController.popBackStack() }
                )
            }
        }
        composable(
            route = LobbyDestination.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                LobbyScreen(
                    groupId = groupId,
                    onNavigateUp = { navController.popBackStack() },
                    onCreateGameClick = { navController.navigate(CreateGameDestination.createRoute(groupId)) }
                )
            }
        }
        composable(
            route = CreateGameDestination.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                CreateGameScreen(
                    groupId = groupId,
                    onNavigateUp = { navController.popBackStack() }
                )
            }
        }
    }
}