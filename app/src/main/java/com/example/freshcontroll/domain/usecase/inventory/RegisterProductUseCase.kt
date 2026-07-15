package com.example.freshcontroll.domain.usecase.inventory

import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.repository.ProductRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Caso de uso para guardar (crear o editar) un producto.
 * Valida la existencia de un ID para inyectar uno nuevo si se trata de una creación.
 */
class RegisterProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(product: Product): Result<Unit> {
        val productToSave = if (product.id.isBlank()) {
            product.copy(id = UUID.randomUUID().toString())
        } else {
            product
        }

        return productRepository.saveProduct(productToSave)
    }
}