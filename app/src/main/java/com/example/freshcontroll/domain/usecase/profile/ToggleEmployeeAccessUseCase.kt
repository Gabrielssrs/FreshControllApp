package com.example.freshcontroll.domain.usecase.profile

import com.example.freshcontroll.domain.repository.EmployeeRepository
import javax.inject.Inject

/**
 * Alterna los permisos de un empleado para permitirle o denegarle acceso al sistema.
 */
class ToggleEmployeeAccessUseCase @Inject constructor(
    private val employeeRepository: EmployeeRepository
) {
    suspend operator fun invoke(userId: String, hasAccess: Boolean): Result<Unit> {
        return employeeRepository.updateEmployeeAccess(userId, hasAccess)
    }
}