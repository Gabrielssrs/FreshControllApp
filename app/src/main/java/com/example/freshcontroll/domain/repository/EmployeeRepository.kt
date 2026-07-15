package com.example.freshcontroll.domain.repository

import com.example.freshcontroll.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para la gestión del personal y perfiles.
 * Permite al dueño administrar accesos y a los usuarios actualizar sus datos.
 */
interface EmployeeRepository {

    fun getEmployees(storeId: String): Flow<List<User>>

    suspend fun addEmployee(user: User, temporaryPassword: String): Result<Unit>

    suspend fun updateEmployeeAccess(userId: String, hasAccess: Boolean): Result<Unit>

    suspend fun updateProfile(userId: String, fullName: String, email: String, phone: String, photoUrl: String?): Result<Unit>
}