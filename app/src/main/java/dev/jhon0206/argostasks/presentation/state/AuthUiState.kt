package dev.jhon0206.argostasks.presentation.state

import dev.jhon0206.argostasks.domain.model.AuthUser

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val resetEmail: String = "",
    val newPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isRegisterMode: Boolean = false,
    val isChangePasswordDialogOpen: Boolean = false,
    val currentUser: AuthUser? = null
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.length >= 6

    val isResetEmailValid: Boolean
        get() = resetEmail.isNotBlank()

    val isNewPasswordValid: Boolean
        get() = newPassword.length >= 6
}