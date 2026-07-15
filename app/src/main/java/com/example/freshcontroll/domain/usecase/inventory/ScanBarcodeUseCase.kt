package com.example.freshcontroll.domain.usecase.inventory

import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Caso de uso para buscar un producto a partir de la lectura de su código de barras.
 * Operación asíncrona de un solo impacto (suspend).
 */
class ScanBarcodeUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(storeId: String, barcode: String): Product? {
        return productRepository.getProductByBarcode(storeId, barcode)
    }
}