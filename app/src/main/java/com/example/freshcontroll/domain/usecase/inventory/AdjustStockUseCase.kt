package com.example.freshcontroll.domain.usecase.inventory

import com.example.freshcontroll.domain.model.MovementReason
import com.example.freshcontroll.domain.model.StockMovement
import com.example.freshcontroll.domain.repository.ProductRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Caso de uso para ajustar las existencias de un producto.
 * Construye el objeto StockMovement calculando automáticamente la diferencia.
 */
class AdjustStockUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        productId: String,
        storeId: String,
        previousQuantity: Double,
        newQuantity: Double,
        reason: MovementReason,
        userId: String,
        userName: String,
        productName: String
    ): Result<Unit> {
        val movement = StockMovement(
            id = UUID.randomUUID().toString(),
            storeId = storeId,
            productId = productId,
            productName = productName,
            previousQuantity = previousQuantity,
            newQuantity = newQuantity,
            adjustment = newQuantity - previousQuantity,
            reason = reason,
            timestamp = System.currentTimeMillis(),
            userId = userId,
            userName = userName
        )

        return productRepository.adjustStock(movement)
    }
}