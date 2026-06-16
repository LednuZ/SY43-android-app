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
import com.example.whereami.ui.screens.GameDestination
import com.example.whereami.ui.screens.GameScreen
import com.example.whereami.ui.screens.RoundDestination
import com.example.whereami.ui.screens.RoundScreen
import com.example.whereami.ui.screens.AccountDestination
import com.example.whereami.ui.screens.AccountScreen
import com.example.whereami.ui.screens.ResetPasswordDestination
import com.example.whereami.ui.screens.ResetPasswordScreen
import com.example.whereami.ui.screens.PastGamesDestination
import com.example.whereami.ui.screens.PastGamesScreen
import com.example.whereami.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import androidx.navigation.navDeepLink
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier : Modifier = Modifier,
)
{
    NavHost(
        navController=navController,
        startDestination = HomeDestination.route,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ){
        composable (route = HomeDestination.route)
        {
            HomeScreen(
                onLoginClick = { 
                    navController.navigate(LoginDestination.route) {
                        popUpTo(HomeDestination.route) { inclusive = true }
                    }
                },
                onNavigateToRound = { gameId, roundId -> navController.navigate(RoundDestination.createRoute(gameId, roundId)) },
                onNavigateToAccount = { navController.navigate(AccountDestination.route) }
            )
        }

        composable (route = LoginDestination.route)
        {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(0) { inclusive = true }
                    }
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
                    onCreateGameClick = { navController.navigate(CreateGameDestination.createRoute(groupId)) },
                    onNavigateToGame = { gameId -> navController.navigate(GameDestination.createRoute(gameId)) },
                    onNavigateToPastGames = { gId -> navController.navigate(PastGamesDestination.createRoute(gId)) }
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
        composable(
            route = GameDestination.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            if (gameId != null) {
                GameScreen(
                    gameId = gameId,
                    onNavigateUp = { navController.popBackStack() },
                    onNavigateToRound = { gId, rId -> navController.navigate(RoundDestination.createRoute(gId, rId)) }
                )
            }
        }
        composable(
            route = RoundDestination.route,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType },
                navArgument("roundId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            val roundId = backStackEntry.arguments?.getString("roundId")
            if (gameId != null && roundId != null) {
                RoundScreen(
                    gameId = gameId,
                    roundId = roundId,
                    onNavigateUp = { navController.popBackStack() },
                    onNavigateToRound = { gId, rId ->
                        navController.navigate(RoundDestination.createRoute(gId, rId)) {
                            popUpTo(GameDestination.createRoute(gameId)) {
                                inclusive = false
                            }
                        }
                    }
                )
            }
        }
        composable(route = AccountDestination.route) {
            val session = SupabaseProvider.client.auth.currentSessionOrNull()
            if (session?.user != null) {
                AccountScreen(
                    currentUser = session.user!!,
                    onNavigateUp = { navController.popBackStack() },
                    onNavigateToLogin = { 
                        navController.navigate(LoginDestination.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable(
            route = ResetPasswordDestination.route,
            deepLinks = listOf(navDeepLink { uriPattern = "whereami://reset-password" })
        ) {
            ResetPasswordScreen(
                onNavigateToHome = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = PastGamesDestination.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                PastGamesScreen(
                    groupId = groupId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGame = { gameId -> navController.navigate(GameDestination.createRoute(gameId)) }
                )
            }
        }
    }
}