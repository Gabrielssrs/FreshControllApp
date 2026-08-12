package com.example.freshcontroll.domain.usecase.profile

import com.example.freshcontroll.domain.repository.CashRepository
import javax.inject.Inject

class SyncCashClosesUseCase @Inject constructor(
    private val cashRepository: CashRepository
) {
    suspend operator fun invoke(storeId: String): Result<Unit> {
        return cashRepository.syncCashCloses(storeId)
    }
}