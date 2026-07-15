package com.example.freshcontroll.domain.model

/**
 * Representa una línea de detalle (ítem específico) dentro de un ticket de venta.
 */
data class SaleDetail(
    val id: String,
    val saleId: String,
    val productId: String,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double
)