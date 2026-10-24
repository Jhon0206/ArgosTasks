package dev.jhon0206.argostasks.presentation.ui.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object ForgotPassword : Routes("forgot_password")
    object Profile : Routes("profile")
}