package com.example.freshcontroll.domain.usecase.home

import com.example.freshcontroll.domain.model.Store
import com.example.freshcontroll.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoreUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(storeId: String): Flow<Store?> {
        return authRepository.getStore(storeId)
    }
}