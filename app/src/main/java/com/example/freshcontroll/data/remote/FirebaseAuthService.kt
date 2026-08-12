package com.example.freshcontroll.data.remote

import com.example.freshcontroll.util.FreshLogger
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Servicio remoto encargado de gestionar la autenticación de usuarios mediante Firebase Auth.
 * Envuelve las llamadas asíncronas del SDK (Task) en corrutinas suspendidas.
 */
class FirebaseAuthService(
    private val firebaseAuth: FirebaseAuth
) {

    /**
     * Inicia sesión con correo y contraseña.
     * @return Result con el UID del usuario si es exitoso, o una excepción si falla.
     */
    suspend fun login(email: String, password: String): Result<String> = runCatching {
        FreshLogger.network("Auth", "LOGIN", "Email: $email")
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("El UID devuelto por Firebase es nulo.")
        FreshLogger.d("Auth", "Login success for UID: $uid")
        uid
    }.onFailure {
        FreshLogger.e("AuthError", "Login failed for $email", it)
    }

    /**
     * Crea una nueva cuenta de usuario en Firebase Auth.
     * @return Result con el UID del nuevo usuario generado.
     */
    suspend fun register(email: String, password: String): Result<String> = runCatching {
        FreshLogger.network("Auth", "REGISTER", "Email: $email")
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("El UID generado por Firebase es nulo.")
        FreshLogger.d("Auth", "Registration success for UID: $uid")
        uid
    }.onFailure {
        FreshLogger.e("AuthError", "Registration failed for $email", it)
    }

    /**
     * Envía un correo electrónico para el restablecimiento de contraseña.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        FreshLogger.network("Auth", "RESET_PWD", "Email: $email")
        firebaseAuth.sendPasswordResetEmail(email).await()
        Unit
    }.onFailure {
        FreshLogger.e("AuthError", "Reset password email failed for $email", it)
    }

    /**
     * Obtiene el UID del usuario actualmente autenticado en la sesión local de Firebase.
     * @return El UID como String, o null si no hay sesión activa.
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Cierra la sesión activa en Firebase Auth.
     */
    fun logout() {
        firebaseAuth.signOut()
    }
}