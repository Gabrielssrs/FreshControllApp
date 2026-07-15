package com.example.freshcontroll.domain.usecase.auth

import com.example.freshcontroll.domain.model.User
import com.example.freshcontroll.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para iniciar sesión.
 * Valida que las credenciales no estén vacías antes de delegar la operación al repositorio.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("El correo y la contraseña no pueden estar vacíos."))
        }
        return authRepository.login(email, password)
    }
}