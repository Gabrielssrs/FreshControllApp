package com.example.freshcontroll.domain.usecase.sales

import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.domain.model.SaleDetail
import java.util.UUID
import javax.inject.Inject

/**
 * Añade un producto al carrito de ventas o actualiza su cantidad si ya existe.
 * Es un caso de uso de pura lógica en memoria, no requiere repositorio.
 */
class AddProductToCartUseCase @Inject constructor() {
    operator fun invoke(cart: List<SaleDetail>, product: Product, qty: Double): List<SaleDetail> {
        val existingItem = cart.find { it.productId == product.id }

        return if (existingItem != null) {
            val newQty = existingItem.quantity + qty
            val newTotalPrice = newQty * existingItem.unitPrice
            cart.map {
                if (it.productId == product.id) it.copy(quantity = newQty, totalPrice = newTotalPrice) else it
            }
        } else {
            val newItem = SaleDetail(
                id = UUID.randomUUID().toString(),
                saleId = "", // Se asignará en ProcessSaleUseCase al confirmar la venta
                productId = product.id,
                productName = product.name,
                quantity = qty,
                unitPrice = product.price,
                totalPrice = product.price * qty
            )
            cart + newItem
        }
    }
}