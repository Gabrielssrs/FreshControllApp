package com.example.freshcontroll.domain.usecase.profile

import com.example.freshcontroll.domain.repository.AuthRepository
import javax.inject.Inject

class SyncEmployeesUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(storeId: String): Result<Unit> {
        return authRepository.syncEmployees(storeId)
    }
}