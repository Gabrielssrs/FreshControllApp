package com.example.freshcontroll.domain.usecase.inventory

import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener el detalle reactivo de un producto específico mediante su ID.
 */
class GetProductDetailUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(productId: String): Flow<Product?> {
        return productRepository.getProductById(productId)
    }
}