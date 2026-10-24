package dev.jhon0206.argostasks.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.jhon0206.argostasks.presentation.ui.screens.ForgotPasswordScreen
import dev.jhon0206.argostasks.presentation.ui.screens.LoginScreen
import dev.jhon0206.argostasks.presentation.ui.screens.ProfileScreen
import dev.jhon0206.argostasks.presentation.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val uiState by authViewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != Routes.Login.route && currentRoute != Routes.ForgotPassword.route) {
                navController.navigate(Routes.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == Routes.Login.route || currentRoute == Routes.ForgotPassword.route || currentRoute == null) {
                navController.navigate(Routes.Profile.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) Routes.Profile.route else Routes.Login.route
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onForgotPasswordClick = {
                    navController.navigate(Routes.ForgotPassword.route)
                }
            )
        }

        composable(Routes.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.Profile.route) {
            ProfileScreen(
                viewModel = authViewModel
            )
        }
    }
}