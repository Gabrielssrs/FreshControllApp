package com.example.freshcontroll.domain.usecase.auth

import com.example.freshcontroll.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para solicitar el restablecimiento de contraseña.
 * Delega directamente la operación al repositorio.
 */
class RecoverPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.sendPasswordResetEmail(email)
    }
}