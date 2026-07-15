package com.example.freshcontroll.domain.usecase.profile

import com.example.freshcontroll.domain.model.User
import com.example.freshcontroll.domain.model.UserRole
import com.example.freshcontroll.domain.repository.EmployeeRepository
import javax.inject.Inject

/**
 * Añade un nuevo empleado al sistema.
 * Genera de manera automática una contraseña temporal segura que el empleado utilizará
 * para ingresar la primera vez.
 */
class AddEmployeeUseCase @Inject constructor(
    private val employeeRepository: EmployeeRepository
) {
    suspend operator fun invoke(
        storeId: String,
        fullName: String,
        email: String,
        phone: String,
        role: UserRole
    ): Result<String> {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val segment1 = (1..4).map { allowedChars.random() }.joinToString("")
        val segment2 = (1..4).map { allowedChars.random() }.joinToString("")
        val temporaryPassword = "FC-$segment1-$segment2"

        val user = User(
            id = "", // Firebase Auth asignará el UID real dentro del repositorio
            storeId = storeId,
            fullName = fullName,
            email = email,
            phone = phone,
            role = role,
            hasAccess = true,
            photoUrl = null
        )

        val result = employeeRepository.addEmployee(user, temporaryPassword)
        return if (result.isSuccess) {
            Result.success(temporaryPassword)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido al crear empleado"))
        }
    }
}