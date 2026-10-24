package dev.jhon0206.argostasks.data.mapper

import com.google.firebase.auth.FirebaseUser
import dev.jhon0206.argostasks.domain.model.AuthUser

fun FirebaseUser.toDomain(): AuthUser {
    return AuthUser(
        id = uid,
        email = email,
        displayName = displayName,
        isEmailVerified = isEmailVerified,
        isAnonymous = isAnonymous
    )
}