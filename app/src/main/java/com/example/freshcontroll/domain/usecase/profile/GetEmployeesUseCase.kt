package com.example.freshcontroll.domain.usecase.profile

import com.example.freshcontroll.domain.model.User
import com.example.freshcontroll.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Obtiene la lista reactiva de empleados que pertenecen al local.
 */
class GetEmployeesUseCase @Inject constructor(
    private val employeeRepository: EmployeeRepository
) {
    operator fun invoke(storeId: String): Flow<List<User>> {
        return employeeRepository.getEmployees(storeId)
    }
}