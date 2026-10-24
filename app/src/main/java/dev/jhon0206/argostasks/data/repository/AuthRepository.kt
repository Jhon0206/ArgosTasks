package dev.jhon0206.argostasks.data.repository

import com.google.firebase.auth.FirebaseAuth
import dev.jhon0206.argostasks.data.mapper.toDomain
import dev.jhon0206.argostasks.domain.model.AuthUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val authStateFlow: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toDomain())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    suspend fun signInAnonymously(): Result<AuthUser> = safeAuthCall {
        val authResult = firebaseAuth.signInAnonymously().await()
        authResult.user?.toDomain()
            ?: throw Exception("Authentication failed: User is null")
    }

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<AuthUser> = safeAuthCall {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        authResult.user?.toDomain()
            ?: throw Exception("Authentication failed: User is null")
    }

    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Result<AuthUser> = safeAuthCall {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        authResult.user?.toDomain()
            ?: throw Exception("User creation failed: User is null")
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = safeAuthCall {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = safeAuthCall {
        val currentUser = firebaseAuth.currentUser
            ?: throw Exception("No user logged in to update password")
        currentUser.updatePassword(newPassword).await()
    }

    suspend fun sendEmailVerification(): Result<Unit> = safeAuthCall {
        val currentUser = firebaseAuth.currentUser
            ?: throw Exception("No user logged in to send verification email")
        currentUser.sendEmailVerification().await()
    }

    suspend fun reloadUser(): Result<AuthUser?> = safeAuthCall {
        val currentUser = firebaseAuth.currentUser ?: return@safeAuthCall null
        currentUser.reload().await()
        firebaseAuth.currentUser?.toDomain()
    }

    fun getCurrentUser(): AuthUser? {
        return firebaseAuth.currentUser?.toDomain()
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    private suspend inline fun <T> safeAuthCall(crossinline action: suspend () -> T): Result<T> {
        return try {
            Result.success(action())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}