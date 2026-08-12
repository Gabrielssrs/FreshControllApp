package com.example.freshcontroll.domain.usecase.sales

import com.example.freshcontroll.domain.repository.SaleRepository
import javax.inject.Inject

class SyncSalesUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) {
    suspend operator fun invoke(storeId: String): Result<Unit> {
        return saleRepository.syncSales(storeId)
    }
}