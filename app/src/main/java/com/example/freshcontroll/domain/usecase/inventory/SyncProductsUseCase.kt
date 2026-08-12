package com.example.freshcontroll.domain.usecase.inventory

import com.example.freshcontroll.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Caso de uso para sincronizar el catálogo de productos desde Firestore hacia Room.
 * Esencial para el primer inicio en dispositivos nuevos.
 */
class SyncProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(storeId: String): Result<Unit> {
        return productRepository.syncProducts(storeId)
    }
}