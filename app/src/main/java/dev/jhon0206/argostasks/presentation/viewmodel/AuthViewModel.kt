package dev.jhon0206.argostasks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jhon0206.argostasks.data.repository.AuthRepository
import dev.jhon0206.argostasks.presentation.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authStateFlow.collect { user ->
                _uiState.update { currentState ->
                    currentState.copy(
                        currentUser = user,
                        isLoading = false,
                        email = "",
                        password = "",
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onResetEmailChanged(resetEmail: String) {
        _uiState.update { it.copy(resetEmail = resetEmail, errorMessage = null) }
    }

    fun onNewPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(newPassword = newPassword, errorMessage = null) }
    }

    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun openChangePasswordDialog() {
        _uiState.update {
            it.copy(
                isChangePasswordDialogOpen = true,
                newPassword = "",
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun closeChangePasswordDialog() {
        _uiState.update {
            it.copy(
                isChangePasswordDialogOpen = false,
                newPassword = "",
                errorMessage = null
            )
        }
    }

    fun submitEmailAuth() {
        val currentState = _uiState.value
        if (!currentState.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = if (currentState.isRegisterMode) {
                authRepository.createUserWithEmailAndPassword(
                    email = currentState.email,
                    password = currentState.password
                )
            } else {
                authRepository.signInWithEmailAndPassword(
                    email = currentState.email,
                    password = currentState.password
                )
            }

            result.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "Authentication error"
                    )
                }
            }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = authRepository.signInAnonymously()

            result.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "Anonymous login fail"
                    )
                }
            }
        }
    }

    fun sendPasswordResetEmail(emailToSend: String? = null) {
        val email = emailToSend ?: _uiState.value.resetEmail.ifBlank { _uiState.value.email }
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter a valid Email") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = authRepository.sendPasswordResetEmail(email)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Recovery mail send to $email"
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "Error at send email recovery"
                    )
                }
            }
        }
    }

    fun updatePassword() {
        val newPassword = _uiState.value.newPassword
        if (newPassword.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must have 6 characters at least") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = authRepository.updatePassword(newPassword)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isChangePasswordDialogOpen = false,
                        newPassword = "",
                        successMessage = "Successfully updated password"
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "Error at update password"
                    )
                }
            }
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = authRepository.sendEmailVerification()

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Validation mail send"
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "Error at send email verification"
                    )
                }
            }
        }
    }

    fun reloadUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.reloadUser()
            result.onSuccess { updatedUser ->
                _uiState.update {
                    it.copy(
                        currentUser = updatedUser,
                        isLoading = false
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage
                    )
                }
            }
        }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                email = "",
                password = "",
                resetEmail = "",
                newPassword = "",
                errorMessage = null,
                successMessage = null
            )
        }
        authRepository.logout()
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }
}