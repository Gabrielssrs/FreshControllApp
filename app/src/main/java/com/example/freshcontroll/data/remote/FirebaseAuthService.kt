package com.example.freshcontroll.data.remote

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
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        authResult.user?.uid ?: throw Exception("El UID devuelto por Firebase es nulo.")
    }

    /**
     * Crea una nueva cuenta de usuario en Firebase Auth.
     * @return Result con el UID del nuevo usuario generado.
     */
    suspend fun register(email: String, password: String): Result<String> = runCatching {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        authResult.user?.uid ?: throw Exception("El UID generado por Firebase es nulo.")
    }

    /**
     * Envía un correo electrónico para el restablecimiento de contraseña.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        firebaseAuth.sendPasswordResetEmail(email).await()
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