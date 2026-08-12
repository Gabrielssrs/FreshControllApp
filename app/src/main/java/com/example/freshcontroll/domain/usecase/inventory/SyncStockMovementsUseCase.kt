package com.example.freshcontroll.domain.usecase.inventory

import com.example.freshcontroll.domain.repository.ProductRepository
import javax.inject.Inject

class SyncStockMovementsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: String): Result<Unit> {
        return productRepository.syncStockMovements(productId)
    }
}