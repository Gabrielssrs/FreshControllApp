package com.example.freshcontroll.domain.usecase.profile

import com.example.freshcontroll.domain.repository.CashRepository
import javax.inject.Inject

/**
 * Calcula la suma total esperada en caja con base en las ventas concretadas en el día.
 */
class GetSystemCashBalanceUseCase @Inject constructor(
    private val cashRepository: CashRepository
) {
    suspend operator fun invoke(storeId: String): Double {
        return cashRepository.calculateCurrentSystemAmount(storeId)
    }
}