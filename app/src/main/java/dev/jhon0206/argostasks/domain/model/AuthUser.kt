package dev.jhon0206.argostasks.domain.model

data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String? = null,
    val isEmailVerified: Boolean = false,
    val isAnonymous: Boolean = false
)