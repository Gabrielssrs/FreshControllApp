package com.example.freshcontroll.domain.usecase.profile

import com.example.freshcontroll.domain.repository.EmployeeRepository
import javax.inject.Inject

/**
 * Caso de uso para actualizar los datos básicos y la foto del perfil del usuario.
 */
class UpdateProfileUseCase @Inject constructor(
    private val employeeRepository: EmployeeRepository
) {
    suspend operator fun invoke(
        userId: String,
        fullName: String,
        email: String,
        phone: String,
        photoUrl: String?
    ): Result<Unit> {
        return employeeRepository.updateProfile(userId, fullName, email, phone, photoUrl)
    }
}
