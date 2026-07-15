package com.example.freshcontroll.domain.usecase.inventory

import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener el catálogo de productos reactivo.
 * Delega directamente al repositorio soportando búsquedas opcionales.
 */
class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(storeId: String, query: String? = null): Flow<List<Product>> {
        return productRepository.getProducts(storeId, query)
    }
}