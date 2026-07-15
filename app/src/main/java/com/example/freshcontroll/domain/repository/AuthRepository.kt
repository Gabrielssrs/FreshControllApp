package com.example.freshcontroll.domain.repository

import com.example.freshcontroll.domain.model.Store
import com.example.freshcontroll.domain.model.User

/**
 * Contrato para la gestión de autenticación y sesión de usuarios.
 * Maneja el registro inicial del negocio y el acceso al sistema.
 */
interface AuthRepository {

    suspend fun login(email: String, password: String): Result<User>

    suspend fun registerStoreAndOwner(store: Store, user: User, password: String): Result<Unit>

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    suspend fun getCurrentUser(): User?

    fun logout()
}